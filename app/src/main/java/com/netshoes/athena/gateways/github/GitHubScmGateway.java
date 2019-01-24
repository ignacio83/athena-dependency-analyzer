package com.netshoes.athena.gateways.github;

import com.netshoes.athena.conf.GitHubClientProperties;
import com.netshoes.athena.domains.ContentType;
import com.netshoes.athena.domains.ScmApiRateLimit;
import com.netshoes.athena.domains.ScmApiRateLimit.Resource;
import com.netshoes.athena.domains.ScmApiUser;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryBranch;
import com.netshoes.athena.domains.ScmRepositoryContent;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import com.netshoes.athena.gateways.CouldNotGetRepositoryContentException;
import com.netshoes.athena.gateways.GetRepositoryException;
import com.netshoes.athena.gateways.ScmApiGatewayRateLimitExceededException;
import com.netshoes.athena.gateways.ScmApiGetRateLimitException;
import com.netshoes.athena.gateways.ScmGateway;
import com.netshoes.athena.gateways.github.jsons.RateLimitResponseJson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.ShaResource;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.util.EncodingUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubScmGateway implements ScmGateway {
  private static final float FLOAT_ONE = 1f;
  private final GitHubClient gitHubClient;
  private final RepositoryService repositoryService;
  private final ContentsService contentsService;
  private final UserService userService;
  private final GitHubClientProperties gitHubClientProperties;
  private final Scheduler githubApiScheduler;
  private Mono<ScmApiRateLimit> rateLimitCache;

  @PostConstruct
  public void setup() {
    rateLimitCache =
        Mono.fromCallable(this::getRateLimitBlocking)
            .publishOn(githubApiScheduler)
            .map(
                scmApiRateLimitJson ->
                    scmApiRateLimitJson.toDomain(
                        gitHubClientProperties.getCore().getRateLimitPercentage(),
                        gitHubClientProperties.getSearch().getRateLimitPercentage(),
                        gitHubClientProperties.getGraphql().getRateLimitPercentage()))
            .cache(Duration.ofSeconds(5));
  }

  @Override
  public Mono<ScmRepository> getRepository(String id) {
    final RepositoryId repositoryId = RepositoryId.createFromId(id);

    return this.getRateLimit()
        .doOnNext(this::throwExceptionIfRateLimitIsNotAvailable)
        .publishOn(githubApiScheduler)
        .map(rateLimit -> this.getRepositoryBlocking(RepositoryId.createFromId(id)))
        .map(this::toScmRepository);
  }

  @Override
  public Mono<ScmRepositoryBranch> getBranch(ScmRepository repository, String branchName) {
    final RepositoryId repositoryId = RepositoryId.createFromId(repository.getId());

    return this.getRateLimit()
        .doOnNext(this::throwExceptionIfRateLimitIsNotAvailable)
        .publishOn(githubApiScheduler)
        .map(rateLimit -> getBranchesBlocking(repositoryId))
        .flatMap(
            branches ->
                Flux.fromStream(branches.stream())
                    .filter(b -> b.getName().equals(branchName))
                    .singleOrEmpty())
        .map(branch -> this.toScmRepositoryBranch(branch, repository));
  }

  @Override
  public Flux<ScmRepository> getRepositoriesFromConfiguredOrganization() {
    return this.getRateLimit()
        .doOnNext(this::throwExceptionIfRateLimitIsNotAvailable)
        .publishOn(githubApiScheduler)
        .map(rateLimit -> this.getRepositoriesFromConfiguredOrganizationBlocking())
        .flatMapMany(Flux::fromIterable)
        .map(this::toScmRepository);
  }

  private List<RepositoryBranch> getBranchesBlocking(RepositoryId repositoryId) {
    try {
      final List<RepositoryBranch> branches = repositoryService.getBranches(repositoryId);

      log.trace("{} branch(es) retrieved in {}", branches.size(), repositoryId);

      return branches;
    } catch (RequestException e) {
      ifRateLimitExceededThrowException(e);
      throw new CouldNotGetRepositoryContentException(e);
    } catch (Exception e) {
      throw new CouldNotGetRepositoryContentException(e);
    }
  }

  private void throwExceptionIfRateLimitIsNotAvailable(ScmApiRateLimit scmApiRateLimit) {
    final Resource core = scmApiRateLimit.getCore();
    core.getConfiguredLimitPercentage()
        .ifPresent(
            configuredLimitPercentage -> {
              final float percentageUsed = core.getPercentageUsed();
              if (percentageUsed >= FLOAT_ONE) {
                final OffsetDateTime reset = core.getReset();
                final long minutesToReset = OffsetDateTime.now().until(reset, ChronoUnit.MINUTES);
                throw new ScmApiGatewayRateLimitExceededException(minutesToReset);
              }
            });
  }

  private List<Repository> getRepositoriesFromConfiguredOrganizationBlocking() {
    try {
      final List<Repository> list =
          repositoryService.getOrgRepositories(gitHubClientProperties.getOrganization());
      log.trace("{} repositories found", list != null ? list.size() : 0);
      return list;
    } catch (RequestException e) {
      ifRateLimitExceededThrowException(e);
      throw new GetRepositoryException(e);
    } catch (Exception e) {
      throw new GetRepositoryException(e);
    }
  }

  private Repository getRepositoryBlocking(RepositoryId repositoryId) {
    try {
      return repositoryService.getRepository(repositoryId);
    } catch (IOException e) {
      throw new GetRepositoryException(e);
    }
  }

  private void ifRateLimitExceededThrowException(RequestException requestException)
      throws ScmApiGatewayRateLimitExceededException {
    if (requestException.getMessage().contains("API rate limit exceeded for")) {
      Long minutesToReset = null;
      try {
        final ScmApiRateLimit rateLimit =
            getRateLimitBlocking()
                .toDomain(
                    gitHubClientProperties.getCore().getRateLimitPercentage(),
                    gitHubClientProperties.getSearch().getRateLimitPercentage(),
                    gitHubClientProperties.getGraphql().getRateLimitPercentage());
        final OffsetDateTime reset = rateLimit.getCore().getReset();
        minutesToReset = OffsetDateTime.now().until(reset, ChronoUnit.MINUTES);
      } catch (ScmApiGetRateLimitException e) {
        log.warn("Unable to get rate limit from GitHub Api", e);
      }
      throw new ScmApiGatewayRateLimitExceededException(requestException, minutesToReset);
    }
  }

  @Override
  public Flux<ScmRepositoryContent> getContents(
      ScmRepository repository, String branch, String path) {
    return this.getRateLimit()
        .doOnNext(this::throwExceptionIfRateLimitIsNotAvailable)
        .map(rateLimit -> RepositoryId.createFromId(repository.getId()))
        .publishOn(githubApiScheduler)
        .map(repositoryId -> getRepositoryContentsBlocking(repositoryId, path, branch))
        .flatMapMany(Flux::fromIterable)
        .map(repositoryContents -> toScmRepositoryContent(repository, repositoryContents));
  }

  private List<RepositoryContents> getRepositoryContentsBlocking(
      RepositoryId repositoryId, String path, String branch) {
    try {
      final List<RepositoryContents> list = contentsService.getContents(repositoryId, path, branch);
      log.trace("{} contents found in {} for {}", list.size(), path, repositoryId.generateId());
      return list;
    } catch (RequestException e) {
      ifRateLimitExceededThrowException(e);
      throw new CouldNotGetRepositoryContentException(e);
    } catch (Exception e) {
      throw new CouldNotGetRepositoryContentException(e);
    }
  }

  public Mono<ScmRepositoryContentData> retrieveContentData(ScmRepositoryContent content) {
    return Mono.fromCallable(() -> retrieveContentDataBlocking(content))
        .publishOn(githubApiScheduler)
        .map(repositoryContent -> toScmRepositoryContentData(content, repositoryContent.get(0)));
  }

  private List<RepositoryContents> retrieveContentDataBlocking(ScmRepositoryContent content) {
    final ScmRepository repository = content.getRepository();
    final RepositoryId repositoryId = RepositoryId.createFromId(repository.getId());
    try {
      final String path = content.getPath();
      final List<RepositoryContents> contents = contentsService.getContents(repositoryId, path);

      log.trace("{} content(s) retrieved for {} in {}", contents.size(), path, repository.getId());

      return contents;
    } catch (RequestException e) {
      ifRateLimitExceededThrowException(e);
      throw new CouldNotGetRepositoryContentException(e);
    } catch (Exception e) {
      throw new CouldNotGetRepositoryContentException(e);
    }
  }

  @Override
  public Mono<ScmApiUser> getApiUser() {
    return Mono.fromCallable(this::getUserBlocking)
        .publishOn(githubApiScheduler)
        .map(this::toScmApiUser);
  }

  private ScmApiUser toScmApiUser(User user) {
    ScmApiUser response;
    try {
      response =
          ScmApiUser.ofAuthenticatedUser(
              user.getLogin(),
              user.getName(),
              gitHubClient.getRequestLimit(),
              gitHubClient.getRemainingRequests());
    } catch (GitHubAuthenticationException e) {
      response =
          ScmApiUser.ofInvalidUser(
              user.getLogin(),
              e,
              gitHubClient.getRequestLimit(),
              gitHubClient.getRemainingRequests());
    }
    return response;
  }

  private User getUserBlocking() {
    User user = null;
    try {
      user = userService.getUser();
    } catch (RequestException e) {
      ifRateLimitExceededThrowException(e);
      log.error(e.getMessage(), e);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new GitHubAuthenticationException(e.getMessage(), e);
    }
    return user;
  }

  @Override
  public Mono<ScmApiRateLimit> getRateLimit() {
    return rateLimitCache;
  }

  private RateLimitResponseJson getRateLimitBlocking() {
    final GitHubRequest request = new GitHubRequest();
    request.setUri("/rate_limit");
    request.setType(RateLimitResponseJson.class);

    try {
      final GitHubResponse gitHubResponse = gitHubClient.get(request);
      return (RateLimitResponseJson) gitHubResponse.getBody();
    } catch (IOException e) {
      throw new ScmApiGetRateLimitException(e);
    }
  }

  private ScmRepository toScmRepository(Repository repository) {
    ScmRepository scmRepository = null;
    if (repository != null) {
      final String masterBranch = repository.getMasterBranch();

      scmRepository = new ScmRepository();
      scmRepository.setId(repository.generateId());
      scmRepository.setName(repository.getName());
      scmRepository.setDescription(repository.getDescription());
      scmRepository.setMasterBranch(masterBranch);

      final OffsetDateTime lastPushDate =
          OffsetDateTime.ofInstant(repository.getPushedAt().toInstant(), ZoneId.systemDefault());
      scmRepository.setLastPushDate(Optional.of(lastPushDate));
      try {
        scmRepository.setUrl(new URL(repository.getCloneUrl()));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return scmRepository;
  }

  private ScmRepositoryBranch toScmRepositoryBranch(
      RepositoryBranch repositoryBranch, ScmRepository scmRepository) {
    ScmRepositoryBranch domain = null;
    if (repositoryBranch != null) {
      final TypedResource commit = repositoryBranch.getCommit();
      final String commitSha = Optional.ofNullable(commit).map(ShaResource::getSha).orElse(null);
      domain = new ScmRepositoryBranch(repositoryBranch.getName(), commitSha, scmRepository);
    }
    return domain;
  }

  private ScmRepositoryContentData toScmRepositoryContentData(
      ScmRepositoryContent content, RepositoryContents contents) {
    final String dataInBase64 = contents.getContent();
    final String data =
        Optional.ofNullable(dataInBase64)
            .map(EncodingUtils::fromBase64)
            .map(String::new)
            .orElse(null);

    final ScmRepositoryContentData scmRepositoryContentData =
        new ScmRepositoryContentData(content, data, contents.getSize(), contents.getSha());
    log.trace(
        "Content {} ({} bytes) mapped to ScmRepositoryContentData for {}",
        contents.getName(),
        contents.getSize(),
        contents.getPath());

    return scmRepositoryContentData;
  }

  private ScmRepositoryContent toScmRepositoryContent(
      ScmRepository scmRepository, RepositoryContents repositoryContents) {

    final long size = repositoryContents.getSize();
    final String path = repositoryContents.getPath();
    final String name = repositoryContents.getName();
    final String branch = scmRepository.getMasterBranch();
    final ContentType type = ContentType.fromString(repositoryContents.getType());

    return new ScmRepositoryContent(scmRepository, branch, path, name, type, size);
  }
}
