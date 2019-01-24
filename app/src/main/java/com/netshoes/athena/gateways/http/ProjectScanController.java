package com.netshoes.athena.gateways.http;

import com.netshoes.athena.gateways.http.jsons.ErrorJson;
import com.netshoes.athena.gateways.http.jsons.RequestProjectScanJson;
import com.netshoes.athena.gateways.http.jsons.ScanResultJson;
import com.netshoes.athena.usecases.ProjectScan;
import com.netshoes.athena.usecases.RequestProjectScan;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/projects")
@AllArgsConstructor
@Api(
    value = "/api/v1/projects",
    description = "Operations for scan projects",
    tags = "projects scan")
public class ProjectScanController {
  private final RequestProjectScan requestProjectScan;
  private final ProjectScan projectScan;

  @RequestMapping(path = "scan", produces = "application/json", method = RequestMethod.POST)
  @ApiOperation(
      value = "Request scan of dependencies for all projects for configured Source Control Manager",
      produces = "application/json")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiResponses(
      value = {
        @ApiResponse(
            code = 202,
            message = "Process started",
            response = RequestProjectScanJson.class,
            responseContainer = "List"),
        @ApiResponse(code = 422, message = "Fail on start process", response = ErrorJson.class)
      })
  @SuppressWarnings("unused")
  public Flux<RequestProjectScanJson> scan(@RequestHeader String authorization) {
    return requestProjectScan
        .forMasterBranchToAllProjectsFromConfiguredOrganization()
        .map(RequestProjectScanJson::new);
  }

  @RequestMapping(
      path = "/{projectId}/refresh",
      produces = "application/json",
      method = RequestMethod.POST)
  @ApiOperation(
      value = "Request a new scan for dependencies of project",
      produces = "application/json")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiResponses(value = {@ApiResponse(code = 202, message = "Process started")})
  @SuppressWarnings("unused")
  public Mono<RequestProjectScanJson> refresh(
      @ApiParam(value = "Id of Project") @PathVariable("projectId") String projectId,
      @RequestHeader String authorization) {
    return requestProjectScan.refresh(projectId).map(RequestProjectScanJson::new);
  }

  @RequestMapping(
      path = "/{projectId}/refreshNow",
      produces = "application/json",
      method = RequestMethod.POST)
  @ApiOperation(
      value = "Do a synchronous new scan for dependencies of project",
      produces = "application/json")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Scan finished"),
        @ApiResponse(code = 422, message = "Scan failed", response = ErrorJson.class)
      })
  @SuppressWarnings("unused")
  public Mono<ScanResultJson> refreshNow(
      @ApiParam(value = "Id of Project") @PathVariable("projectId") String projectId,
      @RequestHeader String authorization) {

    return projectScan.execute(projectId).map(ScanResultJson::new);
  }
}
