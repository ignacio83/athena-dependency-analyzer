package com.netshoes.athena.gateways.job;

import com.netshoes.athena.usecases.RequestScanForPendingProjects;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingProjectAnalyzeJob {
  private final RequestScanForPendingProjects requestScanForPendingProjects;

  @Scheduled(fixedDelay = 1000 * 60 * 60)
  public void run() {
    requestScanForPendingProjects.execute().blockLast();
  }
}
