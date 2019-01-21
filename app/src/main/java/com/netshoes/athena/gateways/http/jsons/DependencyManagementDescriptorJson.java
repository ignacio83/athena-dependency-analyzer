package com.netshoes.athena.gateways.http.jsons;

import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.DependencyManagementDescriptor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;

@Data
@ApiModel(value = "DependencyManagementDescriptor")
public class DependencyManagementDescriptorJson {

  @ApiModelProperty(value = "Id of dependency management descriptor", required = true)
  private final String id;

  @ApiModelProperty(value = "Name of dependency management descriptor", required = true)
  private final String name;

  @ApiModelProperty(value = "Project artifact of dependency management descriptor", required = true)
  private final ArtifactJson project;

  @ApiModelProperty("Parent artifact of dependency management descriptor")
  private final ArtifactJson parent;

  @ApiModelProperty("Last execution of analyses")
  private final AnalyzeExecutionJson lastExecution;

  @ApiModelProperty(value = "Indicate if content was stored", required = true)
  private final boolean contentStored;

  @ApiModelProperty("List of dependencies")
  private final List<DependencyArtifactJson> artifacts;

  @ApiModelProperty("List of unstable artifacts")
  private final List<ArtifactJson> unstableArtifacts;

  public DependencyManagementDescriptorJson(DependencyManagementDescriptor domain) {
    this.project = new ArtifactJson(domain.getProject());

    final Optional<Artifact> parentArtifact = domain.getParentArtifact();
    this.parent = parentArtifact.map(ArtifactJson::new).orElse(null);

    this.id = project.getId();
    this.name = domain.getName();
    this.contentStored = domain.getStoragePath().isPresent();
    this.lastExecution = new AnalyzeExecutionJson(domain.getLastExecution());

    final List<Artifact> domainArtifacts = domain.getArtifacts();
    this.artifacts =
        domainArtifacts.stream().map(DependencyArtifactJson::new).collect(Collectors.toList());

    final Set<Artifact> unstableArtifactsDomain = domain.getUnstableArtifacts();
    this.unstableArtifacts =
        unstableArtifactsDomain.stream().map(ArtifactJson::new).collect(Collectors.toList());
  }
}
