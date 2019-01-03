package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.MavenDependencyManagementDescriptor;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DependencyManagementDescriptorDoc implements Serializable {
  private ArtifactDoc project;
  private String storagePath;
  private List<DependencyArtifactDoc> artifacts;

  public DependencyManagementDescriptorDoc(DependencyManagementDescriptor domain) {

    final List<com.netshoes.athena.domains.Artifact> domainArtifacts = domain.getArtifacts();
    final List<DependencyArtifactDoc> artifacts =
        domainArtifacts.stream().map(DependencyArtifactDoc::new).collect(Collectors.toList());

    this.project = new ArtifactDoc(domain.getProject());
    this.storagePath = domain.getStoragePath().orElse(null);
    this.artifacts = artifacts;
  }

  public DependencyManagementDescriptor toDomain() {
    final Artifact projectDomain = project.toDomain();
    final MavenDependencyManagementDescriptor domain =
        new MavenDependencyManagementDescriptor(projectDomain, storagePath);

    Optional<Artifact> parentArtifact = Optional.empty();

    for (DependencyArtifactDoc artifactDoc : artifacts) {
      switch (artifactDoc.getOrigin()) {
        case PARENT:
          parentArtifact = Optional.of(artifactDoc.toDomain());
          break;
        case DEPENDENCIES:
          domain.addDependencyArtifact(artifactDoc.toDomain());
          break;
        case DEPENDENCIES_MANAGEMENT:
          domain.addDependencyManagementArtifact(artifactDoc.toDomain());
          break;
      }
    }

    domain.setParentArtifact(parentArtifact);

    return domain;
  }
}
