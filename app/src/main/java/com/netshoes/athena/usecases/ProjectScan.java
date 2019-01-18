package com.netshoes.athena.usecases;

import com.netshoes.athena.conf.FeatureProperties;
import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeResult;
import com.netshoes.athena.domains.File;
import com.netshoes.athena.domains.PendingProjectAnalyze;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ProjectAnalyzeRequest;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryContent;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import com.netshoes.athena.gateways.CouldNotGetRepositoryContentException;
import com.netshoes.athena.gateways.DependencyManagementDescriptorAnalyzeExecutionGateway;
import com.netshoes.athena.gateways.DependencyManagerGateway;
import com.netshoes.athena.gateways.FileStorageGateway;
import com.netshoes.athena.gateways.PendingProjectAnalyzeGateway;
import com.netshoes.athena.gateways.ProjectGateway;
import com.netshoes.athena.gateways.ScmApiGatewayRateLimitExceededException;
import com.netshoes.athena.gateways.ScmGateway;
import com.netshoes.athena.usecases.exceptions.ProjectNotFoundException;
import com.netshoes.athena.usecases.exceptions.ProjectScanException;
import com.netshoes.athena.usecases.exceptions.ScmApiRateLimitExceededException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectScan {
  private static final String[] VALID_DESCRIPTORS = new String[] {"pom.xml"};
  private static final int MAX_DIRECTORY_DEPTH = 1;
  private final ScmGateway scmGateway;
  private final FileStorageGateway fileStorageGateway;
  private final DependencyManagerGateway dependencyManagerGateway;
  private final ProjectGateway projectGateway;
  private final PendingProjectAnalyzeGateway pendingProjectAnalyzeGateway;
  private final DependencyManagementDescriptorAnalyzeExecutionGateway
      dependencyManagementDescriptorAnalyzeExecutionGateway;
  private final AnalyzeProjectDependencies analyzeProjectDependencies;
  private final FeatureProperties featureProperties;

  public Mono<Project> execute(String projectId, String repositoryId, String branch) {
    final Mono<Project> project =
        projectGateway
            .findById(projectId)
            .switchIfEmpty(createProjectFromScm(repositoryId, branch));

    return project
        .flatMap(this::execute)
        .then(project)
        .onErrorResume(CouldNotGetRepositoryContentException.class, exception -> Mono.empty())
        .onErrorResume(
            Exception.class,
            exception ->
                scheduleToLater(repositoryId, branch, exception)
                    .map(PendingProjectAnalyze::getProject));
  }

  private Mono<PendingProjectAnalyze> scheduleToLater(
      String repositoryId, String branch, Exception e) {
    return scmGateway
        .getRateLimit()
        .onErrorResume(
            throwable -> {
              log.warn("Could not get rate limit from SCM API.", throwable);
              return Mono.empty();
            })
        .map(rateLimit -> rateLimit.getCore().getReset().toLocalDateTime())
        .defaultIfEmpty(LocalDateTime.now().plusMinutes(5))
        .map(
            scheduledDate -> {
              final ScmRepository scmRepository = new ScmRepository();
              scmRepository.setId(repositoryId);

              final Project project = new Project(scmRepository, branch);
              final PendingProjectAnalyze pendingProjectAnalyze =
                  new PendingProjectAnalyze(project);
              pendingProjectAnalyze.setException(e);
              pendingProjectAnalyze.setScheduledDate(scheduledDate);
              return pendingProjectAnalyze;
            })
        .flatMap(pendingProjectAnalyzeGateway::save)
        .doOnSuccess(
            pendingProjectAnalyze ->
                log.warn(
                    "Project analyze scheduled to {}. Id: {}, Name: {}",
                    pendingProjectAnalyze.getScheduledDate(),
                    pendingProjectAnalyze.getProject().getId(),
                    pendingProjectAnalyze.getProject().getName(),
                    e));
  }

  private Mono<Project> createProjectFromScm(String repositoryId, String branch) {
    return scmGateway
        .getRepository(repositoryId)
        .onErrorMap(ProjectScanException::new)
        .map(repository -> new Project(repository, branch));
  }

  public Mono<Project> execute(String projectId) {
    return projectGateway
        .findById(projectId)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new ProjectNotFoundException(projectId))))
        .flatMap(this::execute);
  }

  public Mono<Project> execute(Project project) {
    final Mono<Project> runAnalyses =
        findDependencyManagerDescriptors(project)
            .doOnNext(this::logRepositoryContent)
            .collectList()
            .map(list -> new ProjectAnalyzeRequest(project, list))
            .flatMapMany(this::analyzeContentsOnErrorResume)
            .flatMap(
                result ->
                    dependencyManagementDescriptorAnalyzeExecutionGateway
                        .save(result.getExecution())
                        .thenReturn(result))
            .map(DependencyManagementDescriptorAnalyzeResult::getDependencyManagementDescriptor)
            .collect(() -> project, Project::addDependencyManagerDescriptor)
            .flatMap(this::saveProjectOnErrorResume)
            .map(Project::getId)
            .flatMap(analyzeProjectDependencies::analyzeProject);

    final Mono<Project> logAnalysisStart = Mono.fromCallable(() -> this.logAnalyzesStart(project));
    final Mono<Void> deletePendingAnalyzes = pendingProjectAnalyzeGateway.delete(project.getId());
    final Mono<Project> clearProjectDescriptor =
        Mono.fromCallable(project::clearDependencyManagerDescriptors);

    return logAnalysisStart
        .thenMany(deletePendingAnalyzes)
        .then(clearProjectDescriptor)
        .then(runAnalyses);
  }

  private Project logAnalyzesStart(Project p) {
    log.info(
        "Starting analysis of repository {} in branch {} ...",
        p.getScmRepository().getId(),
        p.getBranch());
    return p;
  }

  private void logRepositoryContent(ScmRepositoryContentData data) {
    if (log.isDebugEnabled()) {
      final ScmRepositoryContent content = data.getScmRepositoryContent();
      final String name = content.getName();
      final String path = content.getPath().equals(name) ? name : content.getPath();
      log.debug("Found {} in {}.", path, content.getRepository().getId());
    }
  }

  private Flux<ScmRepositoryContentData> findDependencyManagerDescriptors(Project project) {
    final ScmRepository scmRepository = project.getScmRepository();
    final String branch = project.getBranch();
    return scmGateway
        .getContents(scmRepository, branch, "/")
        .expandDeep(
            content -> {
              if (content.isDirectory() && content.getDepth() <= MAX_DIRECTORY_DEPTH) {
                final String path = content.getPathWithoutRootSlash();
                log.debug(
                    "Searching dependency manager descriptor in {} (depth: {}) for {} ...",
                    "".equals(path) ? "root" : path,
                    content.getDepth(),
                    scmRepository.getId());

                return scmGateway
                    .getContents(scmRepository, branch, path)
                    .onErrorResume(
                        CouldNotGetRepositoryContentException.class, exception -> Mono.empty())
                    .onErrorMap(
                        ScmApiGatewayRateLimitExceededException.class,
                        this::createScmApiRateLimitException);
              } else {
                return Flux.empty();
              }
            },
            MAX_DIRECTORY_DEPTH + 1)
        .filter(this::isValidDescriptor)
        .doOnNext(this::logDescriptorFound)
        .flatMap(scmGateway::retrieveContentData)
        .flatMap(this::storeFile)
        .onErrorResume(CouldNotGetRepositoryContentException.class, exception -> Mono.empty())
        .onErrorMap(
            ScmApiGatewayRateLimitExceededException.class, this::createScmApiRateLimitException);
  }

  private Mono<ScmRepositoryContentData> storeFile(ScmRepositoryContentData contentData) {
    final ScmRepositoryContent scmRepositoryContent = contentData.getScmRepositoryContent();
    final String storagePath = scmRepositoryContent.getStoragePath();

    final byte[] bytes = contentData.getData().getBytes(StandardCharsets.UTF_8);
    return fileStorageGateway.store(new File(storagePath, bytes), true).thenReturn(contentData);
  }

  private void logDescriptorFound(ScmRepositoryContent descriptor) {
    log.debug(
        "Dependency manager descriptor {} found for {}",
        "".equals(descriptor.getPath()) ? "root" : descriptor.getPath(),
        descriptor.getRepository().getId());
  }

  private boolean isValidDescriptor(ScmRepositoryContent content) {
    return ArrayUtils.contains(VALID_DESCRIPTORS, content.getName());
  }

  private ScmApiRateLimitExceededException createScmApiRateLimitException(
      ScmApiGatewayRateLimitExceededException e) {
    return new ScmApiRateLimitExceededException(e, e.getMinutesToReset());
  }

  private Flux<DependencyManagementDescriptorAnalyzeResult> analyzeContentsOnErrorResume(
      ProjectAnalyzeRequest project) {
    Flux<DependencyManagementDescriptorAnalyzeResult> results;
    try {
      final boolean enabled = featureProperties.getDependencyResolution().isEnabled();
      results =
          enabled
              ? dependencyManagerGateway.resolveDependenciesAnalyse(project)
              : dependencyManagerGateway.staticAnalyse(project);
    } catch (Exception e) {
      results = Flux.empty();
      log.error(e.getMessage(), e);
    }
    return results;
  }

  private Mono<? extends Project> saveProjectOnErrorResume(Project p) {
    Mono<Project> mono;
    try {
      mono = projectGateway.save(p);
    } catch (Exception e) {
      mono = Mono.empty();
      log.error(e.getMessage(), e);
    }
    return mono;
  }
}
