package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.Technology;
import com.netshoes.athena.gateways.ProjectGateway;
import com.netshoes.athena.usecases.exceptions.ProjectNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class AnalyzeProjectDependencies {
  private final AnalyzeArtifact analyzeArtifact;
  private final ProjectGateway projectGateway;

  public Mono<Project> analyzeProject(String projectId) {
    return projectGateway
        .findById(projectId)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new ProjectNotFoundException(projectId))))
        .flatMap(this::analyzeProject);
  }

  public Mono<Project> analyzeProject(Project project) {
    return Mono.just(project)
        .flatMapIterable(Project::getDescriptors)
        .flatMapIterable(DependencyManagementDescriptor::getArtifacts)
        .flatMap(this::analyzeArtifact)
        .filter(Artifact::isModified)
        .collectList()
        .flatMap(
            modifiedArtifacts -> {
              if (modifiedArtifacts.isEmpty()) {
                return Mono.empty();
              } else {
                return projectGateway.save(project);
              }
            })
        .defaultIfEmpty(project);
  }

  private Mono<Artifact> analyzeArtifact(Artifact artifact) {
    final Mono<Artifact> discoverTechnologies =
        Mono.just(artifact)
            .doOnNext(this::logStartingAnalyze)
            .flatMapMany(this::discoverTechnologies)
            .collect((Supplier<HashSet<Technology>>) HashSet::new, Set::add)
            .map(artifact::addRelatedTechnologies)
            .doOnNext(this::logTechnologiesIfModified)
            .cache();

    final Mono<Artifact> analyzeArtifact =
        this.analyzeArtifact
            .execute(artifact)
            .map(artifact::setReport)
            .doOnNext(this::logReportIfModified);

    return discoverTechnologies.then(analyzeArtifact).switchIfEmpty(discoverTechnologies);
  }

  private Flux<Technology> discoverTechnologies(Artifact artifact) {
    return Flux.fromIterable(Technology.discover(artifact));
  }

  private void logStartingAnalyze(Artifact artifact) {
    log.trace("Starting analyze of artifact {} ...", artifact);
  }

  private void logTechnologiesIfModified(Artifact artifact) {
    if (artifact.isModified()) {
      final Set<Technology> relatedTechnologies = artifact.getRelatedTechnologies();
      if (!relatedTechnologies.isEmpty()) {
        log.debug(
            "Technologies discovered for artifact {}. Technologies: {}",
            artifact,
            relatedTechnologies);
      }
    }
  }

  private void logReportIfModified(Artifact artifact) {
    if (artifact.isModified()) {
      artifact
          .getReport()
          .ifPresent(
              t ->
                  log.info(
                      "Report generated for artifact {}. Summary: {}", artifact, t.getSummary()));
    }
  }
}
