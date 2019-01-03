package com.netshoes.athena.gateways.maven;

import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.ArtifactOrigin;
import com.netshoes.athena.domains.DependencyArtifact;
import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.DependencyScope;
import com.netshoes.athena.domains.MavenDependencyManagementDescriptor;
import com.netshoes.athena.domains.ScmRepositoryContent;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import com.netshoes.athena.gateways.DependencyManagerGateway;
import com.netshoes.athena.gateways.InvalidDependencyManagerDescriptorException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class MavenDependencyManagerGateway implements DependencyManagerGateway {
  private final MavenXpp3Reader mavenReader;

  @Override
  public Flux<DependencyManagementDescriptor> analyze(List<ScmRepositoryContentData> contents) {
    return Flux.fromIterable(contents).flatMap(this::analyze);
  }

  @Override
  public Mono<DependencyManagementDescriptor> analyze(
      ScmRepositoryContentData scmRepositoryContentData) {
    final ScmRepositoryContent content = scmRepositoryContentData.getScmRepositoryContent();
    final String path = content.getPath();
    final String repositoryId = content.getRepository().getId();

    // TODO new MavenCli().doMain(new String[] {"dependency:resolve"}, "", System.out, System.out);

    log.trace("Reading scmRepositoryContentData from {} in {} ...", path, repositoryId);

    final StringReader reader = new StringReader(scmRepositoryContentData.getData());
    final Model model = getModel(reader);
    final MavenDependencyManagementDescriptor descriptor =
        buildMavenDependencyManagementDescriptor(model, content.getStoragePath());

    log.debug(
        "Content {} in {} read with success. Project: {}",
        path,
        repositoryId,
        descriptor.getProject());
    return Mono.just(descriptor);
  }

  private Model getModel(StringReader stringReader) {
    Model model;
    try {
      model = mavenReader.read(stringReader);
    } catch (Exception e) {
      throw new InvalidDependencyManagerDescriptorException(e);
    }
    return model;
  }

  private Artifact buildParentArtifact(Parent parent) {
    Artifact parentArtifact = null;
    if (parent != null) {
      final String parentGroupId = parent.getGroupId();
      final String parentArtifactId = parent.getArtifactId();
      final String parentVersion = parent.getVersion();

      parentArtifact = Artifact.ofParent(parentGroupId, parentArtifactId, parentVersion);
    }
    return parentArtifact;
  }

  private Artifact buildProjectArtifact(Model model, Artifact parentArtifact) {
    Artifact project;
    final String groupId = model.getGroupId();
    final String artifactId = model.getArtifactId();
    final String version = model.getVersion();
    if (parentArtifact != null) {
      project = Artifact.ofProject(groupId, artifactId, version, parentArtifact);
    } else {
      project = Artifact.ofProject(groupId, artifactId, version);
    }
    return project;
  }

  private MavenDependencyManagementDescriptor buildMavenDependencyManagementDescriptor(
      Model model, String storagePath) {
    final Parent parent = model.getParent();
    final Artifact parentArtifact = buildParentArtifact(parent);
    final Artifact project = buildProjectArtifact(model, parentArtifact);

    final MavenDependencyManagementDescriptor descriptor =
        new MavenDependencyManagementDescriptor(project, storagePath);

    descriptor.setParentArtifact(Optional.ofNullable(parentArtifact));

    final Properties properties = model.getProperties();

    convertDependencies(
        model.getDependencies(),
        properties,
        ArtifactOrigin.DEPENDENCIES,
        descriptor::addDependencyArtifact);

    final DependencyManagement dependencyManagement = model.getDependencyManagement();
    if (dependencyManagement != null) {

      convertDependencies(
          dependencyManagement.getDependencies(),
          properties,
          ArtifactOrigin.DEPENDENCIES_MANAGEMENT,
          descriptor::addDependencyManagementArtifact);
    }
    return descriptor;
  }

  public void convertDependencies(
      List<Dependency> dependencies,
      Properties properties,
      ArtifactOrigin artifactOrigin,
      Consumer<DependencyArtifact> consumer) {
    dependencies
        .stream()
        .map(
            dependency -> {
              final String dependencyVersion = dependency.getVersion();
              String version;
              if (dependencyVersion == null) {
                version = "managed";
              } else {
                version = replacePropertyIfNecessary(dependency.getVersion(), properties);
              }
              final String scope = dependency.getScope();
              final DependencyScope dependencyScope =
                  scope == null ? DependencyScope.MANAGED : DependencyScope.fromString(scope);
              final String groupId = dependency.getGroupId();
              final String artifactId = dependency.getArtifactId();
              final DependencyArtifact dependencyArtifact =
                  new DependencyArtifact(
                      groupId, artifactId, version, dependencyScope, artifactOrigin);
              return dependencyArtifact;
            })
        .forEach(consumer);
  }

  public String replacePropertyIfNecessary(String value, Properties properties) {
    if (value.startsWith("${")) {
      final String propertyBinding = value.replaceFirst("\\$\\{", "").replaceFirst("}", "");
      value = (String) properties.get(propertyBinding);
    }
    return value;
  }
}
