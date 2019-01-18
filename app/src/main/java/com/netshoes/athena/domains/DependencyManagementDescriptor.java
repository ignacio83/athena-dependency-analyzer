package com.netshoes.athena.domains;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DependencyManagementDescriptor extends Comparable {

  String getName();

  String getDependencyDescriptorId();

  Optional<String> getStoragePath();

  AnalyzeExecution getLastExecution();

  Optional<Artifact> getParentArtifact();

  Artifact getProject();

  void addDependencyArtifact(DependencyArtifact dependencyArtifact);

  void addDependencyManagementArtifact(DependencyArtifact dependencyArtifact);

  Set<DependencyArtifact> getDependencyArtifacts();

  Set<DependencyArtifact> getDependencyManagementArtifacts();

  List<Artifact> getArtifacts();

  Set<Technology> getRelatedTechnologies();

  Set<Artifact> getUnstableArtifacts();
}
