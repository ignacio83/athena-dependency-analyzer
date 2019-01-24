package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryBranch;
import com.netshoes.athena.gateways.AsynchronousProcessGateway;
import com.netshoes.athena.gateways.GetRepositoryException;
import com.netshoes.athena.gateways.ScmApiGatewayRateLimitExceededException;
import com.netshoes.athena.gateways.ScmGateway;
import com.netshoes.athena.usecases.exceptions.ProjectNotFoundException;
import com.netshoes.athena.usecases.exceptions.RequestScanException;
import com.netshoes.athena.usecases.exceptions.ScmApiRateLimitExceededException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
@Slf4j
public class RequestProjectScan {

  private final ScmGateway scmGateway;
  private final GetProjects getProjects;
  private final AsynchronousProcessGateway asynchronousProcessGateway;

  public Flux<Project> forMasterBranchToAllProjectsFromConfiguredOrganization() {
    final Flux<ScmRepository> repositories =
        scmGateway
            .getRepositoriesFromConfiguredOrganization()
            .onErrorMap(GetRepositoryException.class, RequestScanException::new)
            .onErrorMap(
                ScmApiGatewayRateLimitExceededException.class,
                ScmApiRateLimitExceededException::new);
    return repositories.flatMap(
        repository -> forBranchOfRepository(repository.getMasterBranch(), repository));
  }

  public Mono<Project> forBranchOfRepository(ScmRepositoryBranch scmRepositoryBranch) {
    final Mono<Project> projectMono = Mono.just(scmRepositoryBranch).map(Project::new).cache();
    return projectMono.flatMap(asynchronousProcessGateway::requestProjectScan).then(projectMono);
  }

  public Mono<Project> forBranchOfRepository(String branch, ScmRepository repository) {
    return forBranchOfRepository(ScmRepositoryBranch.offline(branch, repository));
  }

  public Mono<Project> refresh(String projectId) throws ProjectNotFoundException {
    final Mono<Project> projectMono = getProjects.byId(projectId);
    return projectMono.flatMap(asynchronousProcessGateway::requestProjectScan).then(projectMono);
  }
}
