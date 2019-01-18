package com.netshoes.athena.gateways.http.jsons;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.netshoes.athena.domains.AnalyzeExecution;
import com.netshoes.athena.domains.Artifact;
import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.gateways.http.DescriptorsController;
import com.netshoes.athena.gateways.http.ProjectsController;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
@ApiModel(value = "Project")
@Slf4j
public class ProjectJson extends ResourceSupport {

  @ApiModelProperty(value = "Id of this project", required = true)
  private final String projectId;

  @ApiModelProperty(value = "Name of this project", required = true)
  private final String name;

  @ApiModelProperty(value = "Branch where scan was done", required = true)
  private final String branch;

  @ApiModelProperty(value = "Info about Source Control Management repository", required = true)
  private final ScmRepositoryJson scmRepository;

  @ApiModelProperty(value = "Date of last scan", required = true)
  private final OffsetDateTime lastCollectDate;

  @ApiModelProperty(value = "Related technologies of this project")
  private final Set<TechnologyJson> relatedTechnologies;

  @ApiModelProperty("Quantity of unstable artifacts")
  private final Integer unstableArtifactsCount;

  @ApiModelProperty("Quantity of unique unstable artifacts")
  private final Integer unstableArtifactsUniqueCount;

  @ApiModelProperty("One or more fallbacks are executed in the last analyze")
  private final boolean lastAnalyzeExecutionHasFallback;

  public ProjectJson(Project domain) {
    final ScmRepository domainScmRepository = domain.getScmRepository();
    this.name = domain.getName();
    this.branch = domain.getBranch();
    this.scmRepository = new ScmRepositoryJson(domainScmRepository);
    this.projectId = domain.getId();
    this.lastCollectDate =
        OffsetDateTime.of(
            domain.getLastCollectDate(),
            ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));

    this.relatedTechnologies =
        domain
            .getDescriptors()
            .parallelStream()
            .flatMap(d -> d.getRelatedTechnologies().stream())
            .map(tech -> TechnologyJson.valueOf(tech.name()))
            .collect(Collectors.toSet());

    this.unstableArtifactsCount =
        domain
            .getDescriptors()
            .parallelStream()
            .map(DependencyManagementDescriptor::getUnstableArtifacts)
            .mapToInt(Set::size)
            .sum();

    final long unstableCount =
        domain
            .getDescriptors()
            .parallelStream()
            .flatMap(d -> d.getUnstableArtifacts().stream())
            .filter(distinctByKey(Artifact::getArtifactId))
            .count();

    this.lastAnalyzeExecutionHasFallback =
        domain
            .getDescriptors()
            .parallelStream()
            .map(DependencyManagementDescriptor::getLastExecution)
            .anyMatch(AnalyzeExecution::isFallback);

    unstableArtifactsUniqueCount = (int) unstableCount;

    buildLinks();
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }

  private void buildLinks() {
    try {
      final Link link = linkTo(methodOn(ProjectsController.class).get(projectId)).withSelfRel();
      add(link);

      final Link descriptorsLink =
          linkTo(methodOn(DescriptorsController.class).list(projectId)).withRel("descriptors");
      add(descriptorsLink);
    } catch (Exception e) {
      log.error("Unable to create links", e);
    }
  }
}
