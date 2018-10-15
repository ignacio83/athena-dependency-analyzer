package com.netshoes.athena.gateways.http;

import com.netshoes.athena.domains.ArtifactFilter;
import com.netshoes.athena.domains.RequestOfPage;
import com.netshoes.athena.gateways.http.jsons.ArtifactUsageJson;
import com.netshoes.athena.gateways.http.jsons.ProjectJson;
import com.netshoes.athena.usecases.GetArtifactsUsage;
import com.netshoes.athena.usecases.GetProjects;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/artifacts")
@RequiredArgsConstructor
@Api(value = "/api/v1/artifacts", description = "Operations in artifacts", tags = "artifacts")
public class ArtifactsController {
  private final GetArtifactsUsage getArtifactsUsage;
  private final GetProjects getProjects;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "List artifacts usage", produces = "application/json")
  public Flux<ArtifactUsageJson> list(
      @ApiParam(value = "Number of page", required = true) @RequestParam final Integer pageNumber,
      @ApiParam(value = "Size of page", defaultValue = "20")
          @RequestParam(required = false, defaultValue = "20")
          final Integer pageSize,
      @ApiParam(value = "GroupId") @RequestParam(required = false) Optional<String> groupId,
      @ApiParam(value = "ArtifactId") @RequestParam(required = false) Optional<String> artifactId,
      @ApiParam(value = "Version") @RequestParam(required = false) Optional<String> version) {

    String teste = null;
    String teste2 = "";

    return getArtifactsUsage
        .all(
            new RequestOfPage(pageNumber, pageSize),
            new ArtifactFilter(groupId, artifactId, version))
        .map(ArtifactUsageJson::new);
  }

  @GetMapping("/count")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Count artifacts usage", produces = "application/json")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Success")})
  public Mono<Long> count(
      @ApiParam(value = "GroupId") @RequestParam(required = false) Optional<String> groupId,
      @ApiParam(value = "ArtifactId") @RequestParam(required = false) Optional<String> artifactId,
      @ApiParam(value = "Version") @RequestParam(required = false) Optional<String> version) {

    return getArtifactsUsage.count(new ArtifactFilter(groupId, artifactId, version));
  }

  @GetMapping("/{groupId}/{artifactId}/{version}")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "List artifact usage", produces = "application/json")
  public Mono<ArtifactUsageJson> list(
      @ApiParam(value = "GroupId", required = true) @PathVariable String groupId,
      @ApiParam(value = "ArtifactId", required = true) @PathVariable String artifactId,
      @ApiParam(value = "Version", required = true) @PathVariable String version) {

    return getArtifactsUsage
        .one(new ArtifactFilter(groupId, artifactId, version))
        .map(ArtifactUsageJson::new);
  }

  @GetMapping("/{groupId}/{artifactId}/{version}/projects")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "List projects who uses a artifact", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Success", response = ProjectJson.class),
      })
  public Flux<ProjectJson> listProjects(
      @ApiParam(value = "GroupId", required = true) @PathVariable String groupId,
      @ApiParam(value = "ArtifactId", required = true) @PathVariable String artifactId,
      @ApiParam(value = "Version", required = true) @PathVariable String version) {

    return getProjects
        .byArtifact(new ArtifactFilter(groupId, artifactId, version))
        .map(ProjectJson::new);
  }
}
