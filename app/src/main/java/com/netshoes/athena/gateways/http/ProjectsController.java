package com.netshoes.athena.gateways.http;

import com.netshoes.athena.domains.ProjectFilter;
import com.netshoes.athena.domains.RequestOfPage;
import com.netshoes.athena.gateways.http.jsons.ErrorJson;
import com.netshoes.athena.gateways.http.jsons.ProjectJson;
import com.netshoes.athena.usecases.GetProjects;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/projects")
@AllArgsConstructor
@Api(value = "/api/v1/projects", description = "Operations in projects", tags = "projects")
public class ProjectsController {
  private final GetProjects getProjects;

  @RequestMapping(produces = "application/json", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "List projects", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 200,
            message = "Success",
            responseContainer = "List",
            response = ProjectJson.class),
      })
  public Flux<ProjectJson> list(
      @ApiParam(value = "Number of page", required = true) @RequestParam final Integer pageNumber,
      @ApiParam(value = "Size of page", defaultValue = "20")
          @RequestParam(required = false, defaultValue = "20")
          final Integer pageSize,
      @ApiParam(value = "Partial or complete name of project") @RequestParam(required = false)
          Optional<String> name,
      @ApiParam(value = "Only projects with a dependency manager") @RequestParam(required = false)
          boolean onlyWithDependencyManager) {

    return getProjects
        .search(
            new RequestOfPage(pageNumber, pageSize),
            new ProjectFilter(name, onlyWithDependencyManager))
        .map(ProjectJson::new);
  }

  @RequestMapping(value = "/count", produces = "application/json", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Count projects", produces = "application/json")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Success", response = Long.class)})
  public Mono<Long> count(
      @RequestParam(required = false) Optional<String> name,
      @ApiParam(value = "Only projects with a dependency manager") @RequestParam(required = false)
          boolean onlyWithDependencyManager) {
    return getProjects.countSearch(new ProjectFilter(name, onlyWithDependencyManager));
  }

  @RequestMapping(path = "/{id}", produces = "application/json", method = RequestMethod.GET)
  @ApiOperation(value = "Get project by id", produces = "application/json")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Success", response = ProjectJson.class),
        @ApiResponse(code = 404, message = "Project not found", response = ErrorJson.class)
      })
  public Mono<ProjectJson> get(@ApiParam(value = "Id of Project") @PathVariable("id") String id) {
    return getProjects.byId(id).map(ProjectJson::new);
  }
}
