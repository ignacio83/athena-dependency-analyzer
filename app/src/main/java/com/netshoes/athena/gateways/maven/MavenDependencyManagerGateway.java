package com.netshoes.athena.gateways.maven;

import com.netshoes.athena.domains.AnalyzeExecution;
import com.netshoes.athena.domains.AnalyzeType;
import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.ArtifactOrigin;
import com.netshoes.athena.domains.DependencyArtifact;
import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeExecution;
import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeResult;
import com.netshoes.athena.domains.DependencyScope;
import com.netshoes.athena.domains.MavenDependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ProjectCollectDependenciesRequest;
import com.netshoes.athena.domains.ScmRepositoryContent;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import com.netshoes.athena.gateways.DependencyManagerGateway;
import com.netshoes.athena.gateways.InvalidDependencyManagerDescriptorException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@Component
@Slf4j
@RequiredArgsConstructor
public class MavenDependencyManagerGateway implements DependencyManagerGateway {
  private static final String DEPENDENCY_COLLECT_GOAL =
      "org.apache.maven.plugins:maven-dependency-plugin:3.1.1:collect";
  private final MavenXpp3Reader mavenReader;
  private final Invoker invoker;
  private final Scheduler mavenInvokerScheduler;

  @Override
  public Flux<DependencyManagementDescriptorAnalyzeResult> staticAnalyse(
      ProjectCollectDependenciesRequest request) {
    final List<ScmRepositoryContentDataPom> poms = writePomsForMavenInvoker(request);
    return Flux.fromIterable(poms).flatMap(this::staticAnalyse);
  }

  @Override
  public Flux<DependencyManagementDescriptorAnalyzeResult> resolveDependenciesAnalyse(
      ProjectCollectDependenciesRequest request) {
    final List<ScmRepositoryContentDataPom> poms = writePomsForMavenInvoker(request);
    final Flux<ScmRepositoryContentDataPom> flux = Flux.fromIterable(poms);

    return flux.publishOn(mavenInvokerScheduler)
        .flatMapSequential(this::resolveDependenciesAnalyzeFallbackToStatic, 1)
        .doOnComplete(() -> deleteWorkingDirectoryMavenInvoker(request));
  }

  private Mono<DependencyManagementDescriptorAnalyzeResult>
      resolveDependenciesAnalyzeFallbackToStatic(ScmRepositoryContentDataPom contentDataPom) {
    try {
      return this.resolveDependenciesAnalyse(contentDataPom);
    } catch (CouldNotResolveDependenciesException e) {
      log.error("Maven invocation failed, running static analyse...", e);
      return this.staticAnalyse(
          e.getScmRepositoryContentDataPom(), AnalyzeType.DEPENDENCY_RESOLUTION, e);
    }
  }

  private Mono<DependencyManagementDescriptorAnalyzeResult> staticAnalyse(
      ScmRepositoryContentDataPom contentDataPom) {
    return staticAnalyse(contentDataPom, null, null);
  }

  private Mono<DependencyManagementDescriptorAnalyzeResult> staticAnalyse(
      ScmRepositoryContentDataPom contentDataPom,
      AnalyzeType originalAnalyzeType,
      Throwable error) {
    final ScmRepositoryContentData scmRepositoryContentData = contentDataPom.getContentData();
    final ScmRepositoryContent content = scmRepositoryContentData.getScmRepositoryContent();
    final String repositoryId = content.getRepository().getId();

    final String path = content.getPath();
    log.trace("Reading contentData from {} in {} ...", path, repositoryId);

    final AnalyzeExecution execution =
        Optional.ofNullable(error)
            .map(this::getErrorLog)
            .map(
                errorLog ->
                    AnalyzeExecution.fallback(originalAnalyzeType, AnalyzeType.STATIC, errorLog))
            .orElse(AnalyzeExecution.success(AnalyzeType.STATIC));

    final Model model = getModel(scmRepositoryContentData);
    final MavenDependencyManagementDescriptor descriptor =
        buildMavenDependencyManagementDescriptor(
            model, Collections.emptyList(), content.getStoragePath(), execution);

    log.debug(
        "Content {} in {} read with success. Project: {}",
        path,
        repositoryId,
        descriptor.getProject());

    final Project project = contentDataPom.getProject();
    return Mono.just(execution)
        .map(exec -> new DependencyManagementDescriptorAnalyzeExecution(project, path, exec))
        .map(dExec -> new DependencyManagementDescriptorAnalyzeResult(dExec, descriptor));
  }

  private String getErrorLog(Throwable throwable) {
    String errorLog;
    if (throwable instanceof CouldNotResolveDependenciesException) {
      errorLog = ((CouldNotResolveDependenciesException) throwable).getMavenErrorLog();
    } else {
      errorLog = Optional.ofNullable(throwable.getMessage()).orElse("null");
    }
    return errorLog;
  }

  private DependencyManagementDescriptorAnalyzeResult runDependencyCollectGoal(
      ScmRepositoryContentDataPom contentDataPom) throws MavenInvocationException {

    final Path pom = contentDataPom.getStoragePath();
    final InvocationRequest request = new DefaultInvocationRequest();
    request.setBaseDirectory(pom.getParent().toAbsolutePath().toFile());
    request.setPomFile(pom.toFile());
    request.setGoals(Collections.singletonList(DEPENDENCY_COLLECT_GOAL));
    request.setBatchMode(true);

    final DependencyResolverOutputHandler output = new DependencyResolverOutputHandler();
    request.setOutputHandler(output);

    MavenDependencyManagementDescriptor descriptor = null;

    log.debug("Running maven goal {} for {}", DEPENDENCY_COLLECT_GOAL, pom.toFile().toString());
    final InvocationResult result = invoker.execute(request);
    final boolean success = result.getExitCode() == 0;
    final AnalyzeExecution analyzeExecution =
        AnalyzeExecution.success(AnalyzeType.DEPENDENCY_RESOLUTION);
    if (success) {
      final List<Dependency> resolvedDependencies = output.getDependencies();
      log.debug("{} dependencies resolved.", resolvedDependencies.size());

      final ScmRepositoryContentData scmRepositoryContentData = contentDataPom.getContentData();
      final ScmRepositoryContent content = scmRepositoryContentData.getScmRepositoryContent();
      final Model model = getModel(scmRepositoryContentData);

      descriptor =
          buildMavenDependencyManagementDescriptor(
              model, resolvedDependencies, content.getStoragePath(), analyzeExecution);
    } else {
      throwCouldNotResolveDependenciesException(result, contentDataPom, output.getErrorLog());
    }

    final DependencyManagementDescriptorAnalyzeExecution execution =
        new DependencyManagementDescriptorAnalyzeExecution(
            contentDataPom.getProject(), contentDataPom.getPath(), analyzeExecution);

    return new DependencyManagementDescriptorAnalyzeResult(execution, descriptor);
  }

  private void throwCouldNotResolveDependenciesException(
      InvocationResult result, ScmRepositoryContentDataPom contentDataPom, String mavenError) {
    final CommandLineException executionException = result.getExecutionException();
    if (executionException == null) {
      throw new CouldNotResolveDependenciesException(
          "Maven execution failed.", contentDataPom, mavenError);
    } else {
      throw new CouldNotResolveDependenciesException(
          "Maven execution failed.", contentDataPom, mavenError, executionException);
    }
  }

  private Mono<DependencyManagementDescriptorAnalyzeResult> resolveDependenciesAnalyse(
      ScmRepositoryContentDataPom contentDataPom) {
    DependencyManagementDescriptorAnalyzeResult result;
    try {
      result = this.runDependencyCollectGoal(contentDataPom);
    } catch (MavenInvocationException e) {
      throw new CouldNotResolveDependenciesException("Maven execution failed.", e, contentDataPom);
    }
    return Mono.just(result);
  }

  private Model getModel(ScmRepositoryContentData scmRepositoryContentData) {
    final StringReader reader = new StringReader(scmRepositoryContentData.getData());
    return getModel(reader);
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
      Model model,
      List<Dependency> resolvedDependencies,
      String storagePath,
      AnalyzeExecution execution) {
    final Parent parent = model.getParent();
    final Artifact parentArtifact = buildParentArtifact(parent);
    final Artifact project = buildProjectArtifact(model, parentArtifact);

    final MavenDependencyManagementDescriptor descriptor =
        new MavenDependencyManagementDescriptor(project, storagePath, execution);

    descriptor.setParentArtifact(Optional.ofNullable(parentArtifact));

    final Properties properties = model.getProperties();
    if (!resolvedDependencies.isEmpty()) {
      convertDependencies(
          resolvedDependencies,
          properties,
          ArtifactOrigin.DEPENDENCIES,
          descriptor::addDependencyArtifact);

    } else {
      convertDependencies(
          model.getDependencies(),
          properties,
          ArtifactOrigin.DEPENDENCIES,
          descriptor::addDependencyArtifact);
    }

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

  private void convertDependencies(
      List<Dependency> dependencies,
      Properties properties,
      ArtifactOrigin artifactOrigin,
      Consumer<DependencyArtifact> consumer) {
    dependencies.stream()
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
              return new DependencyArtifact(
                  groupId, artifactId, version, dependencyScope, artifactOrigin);
            })
        .forEach(consumer);
  }

  private String replacePropertyIfNecessary(final String value, final Properties properties) {
    String result = value;
    if (result.startsWith("${")) {
      final String propertyBinding = result.replaceFirst("\\$\\{", "").replaceFirst("}", "");
      result = (String) properties.get(propertyBinding);
    }
    return result;
  }

  private List<ScmRepositoryContentDataPom> writePomsForMavenInvoker(
      ProjectCollectDependenciesRequest request) {
    final List<ScmRepositoryContentDataPom> list = new LinkedList<>();
    for (ScmRepositoryContentData contentData : request.getList()) {
      final Path storagePath =
          createWorkingDirectoryMavenInvoker(request.getProject(), contentData);
      final String path = contentData.getScmRepositoryContent().getPath();
      list.add(
          new ScmRepositoryContentDataPom(request.getProject(), storagePath, path, contentData));
    }
    return list;
  }

  private Path createWorkingDirectoryMavenInvoker(
      Project project, ScmRepositoryContentData contentData) {
    Path newFile;
    try {
      final Path path =
          buildWorkingDirectoryPath(contentData.getScmRepositoryContent().getStoragePath());
      Files.createDirectories(path.getParent());
      if (Files.deleteIfExists(path)) {
        log.debug("File {} deleted.", path.toAbsolutePath());
      }
      newFile = Files.createFile(path);
      Files.write(newFile, contentData.getData().getBytes(StandardCharsets.UTF_8));
      log.debug("File {} wrote.", newFile.toAbsolutePath());
    } catch (IOException e) {
      final String path = contentData.getScmRepositoryContent().getPath();
      throw new CouldNotResolveDependenciesException(
          e, new ScmRepositoryContentDataPom(project, null, path, contentData));
    }
    return newFile;
  }

  private void deleteWorkingDirectoryMavenInvoker(ProjectCollectDependenciesRequest request) {
    final String projectName = request.getProject().getScmRepository().getName();
    final Path projectRoot = buildWorkingDirectoryPath(projectName);

    try {
      //noinspection ResultOfMethodCallIgnored
      Files.walk(projectRoot)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .peek(file -> log.debug("Deleting {} ...", file))
          .forEach(File::delete);
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
    }
  }

  private Path buildWorkingDirectoryPath(String filePath) {
    return Paths.get(
        String.format(
            "%s/%s/%s",
            System.getProperty("java.io.tmpdir"), "athena/m2/workingDirectory", filePath));
  }
}
