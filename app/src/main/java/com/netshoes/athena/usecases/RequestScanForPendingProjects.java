package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.PendingProjectAnalyze;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmApiRateLimit;
import com.netshoes.athena.gateways.PendingProjectAnalyzeGateway;
import com.netshoes.athena.gateways.ScmApiGetRateLimitException;
import com.netshoes.athena.gateways.ScmGateway;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestScanForPendingProjects {

  public static final int MINIMUM_REQUESTS_REQUIRED = 100;
  private final PendingProjectAnalyzeGateway pendingProjectAnalyzeGateway;
  private final ScmGateway scmGateway;
  private final RequestProjectScan requestProjectScan;

  public Flux<Project> execute() {
    return scmGateway
        .getRateLimit()
        .filter(this::hasSufficientRemainingRequests)
        .flatMapMany(rateLimit -> pendingProjectAnalyzeGateway.findAll())
        .onErrorResume(
            ScmApiGetRateLimitException.class,
            e -> {
              log.error(e.getMessage(), e);
              return Mono.empty();
            })
        .flatMap(this::executeForPendingProject);
  }

  private Mono<Project> executeForPendingProject(PendingProjectAnalyze pendingProjectAnalyze) {
    final Project project = pendingProjectAnalyze.getProject();
    return requestProjectScan.forBranchOfRepository(project.getBranch());
  }

  private boolean hasSufficientRemainingRequests(ScmApiRateLimit rateLimit) {
    final int remainingRequests = rateLimit.getCore().getRemaining();
    final boolean run = remainingRequests >= MINIMUM_REQUESTS_REQUIRED;
    if (!run) {
      log.info(
          "There are {} requests remaining, ignoring this scan for pending projects",
          remainingRequests);
    }
    return run;
  }
}
