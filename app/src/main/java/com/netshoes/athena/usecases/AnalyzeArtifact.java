package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.ArtifactVersionReport;
import com.netshoes.athena.domains.VersionMapping;
import com.netshoes.athena.gateways.VersionMappingGateway;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class AnalyzeArtifact {
  private final VersionMappingGateway versionMappingGateway;

  public Mono<ArtifactVersionReport> execute(final Artifact artifact) {
    return versionMappingGateway
        .findByArtifact(artifact)
        .flatMapIterable(VersionMapping::getPatterns)
        .map(pattern -> pattern.check(artifact))
        .filter(ArtifactVersionReport::isMatched)
        .singleOrEmpty()
        .doOnNext(report -> logArtifactAnalyzed(artifact, report));
  }

  private void logArtifactAnalyzed(Artifact artifact, ArtifactVersionReport report) {
    log.trace("Artifact {} analyzed. Result {}", artifact, report);
  }
}
