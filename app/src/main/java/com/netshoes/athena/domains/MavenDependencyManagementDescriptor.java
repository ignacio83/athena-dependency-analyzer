package com.netshoes.athena.domains;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "project.id")
public class MavenDependencyManagementDescriptor implements DependencyManagementDescriptor {

  private final Artifact project;
  private final String dependencyDescriptorId;
  private Optional<Artifact> parentArtifact;
  private Optional<String> storagePath;
  private final Set<DependencyArtifact> dependencyArtifacts = new TreeSet<>();
  private final Set<DependencyArtifact> dependencyManagementArtifacts = new TreeSet<>();

  public MavenDependencyManagementDescriptor(Artifact project, String storagePath) {
    this.project = project;
    this.dependencyDescriptorId = project.getId();
    this.storagePath = Optional.ofNullable(storagePath);
  }

  @Override
  public void addDependencyArtifact(DependencyArtifact dependencyArtifact) {
    dependencyArtifacts.add(dependencyArtifact);
  }

  @Override
  public void addDependencyManagementArtifact(DependencyArtifact dependencyArtifact) {
    dependencyManagementArtifacts.add(dependencyArtifact);
  }

  public List<Artifact> getArtifacts() {
    final List<Artifact> list = new ArrayList<>();

    if (parentArtifact.isPresent()) {
      list.add(parentArtifact.get());
    }
    if (dependencyArtifacts != null) {
      list.addAll(dependencyArtifacts);
    }
    if (dependencyManagementArtifacts != null) {
      list.addAll(dependencyManagementArtifacts);
    }

    return list;
  }

  @Override
  public Set<Technology> getRelatedTechnologies() {
    return getArtifacts()
        .stream()
        .flatMap(a -> a.getRelatedTechnologies().stream())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Artifact> getUnstableArtifacts() {
    return getArtifacts()
        .stream()
        .filter(a -> a.getReport().isPresent() && !a.getReport().get().isStable())
        .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public int compareTo(Object o) {
    final MavenDependencyManagementDescriptor other = (MavenDependencyManagementDescriptor) o;
    final Optional<Artifact> opParentArtifact = parentArtifact;
    final Optional<Artifact> opParentArtifactOther = other.parentArtifact;
    if (opParentArtifact.isPresent() && opParentArtifactOther.isPresent()) {
      return project.compareTo(other.project);
    } else if (opParentArtifact.isPresent()) {
      return -1;
    } else {
      return 1;
    }
  }
}
