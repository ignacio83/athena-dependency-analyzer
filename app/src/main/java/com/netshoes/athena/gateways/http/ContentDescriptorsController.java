package com.netshoes.athena.gateways.http;

import com.netshoes.athena.usecases.GetDescriptors;
import com.netshoes.athena.usecases.exceptions.DomainNotFoundException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/descriptors/{id}/content")
@AllArgsConstructor
@Slf4j
@Api(
    value = "/api/v1/projects/{projectId}/descriptors",
    description = "Operations in descriptors of projects",
    tags = "projects descriptors")
public class ContentDescriptorsController {
  private final GetDescriptors getDescriptors;

  @RequestMapping(produces = "text/plain", method = RequestMethod.GET)
  @ApiOperation(value = "Get descriptor content of project by id", produces = "text/plain")
  @ApiResponses(
      value = {
        @ApiResponse(code = 200, message = "Success", response = String.class),
        @ApiResponse(code = 404, message = "Descriptor content not found")
      })
  public Mono<String> getDescriptorContentById(
      @ApiParam(value = "Id of Project", required = true) @PathVariable("projectId")
          String projectId,
      @ApiParam(value = "Id of Descriptor", required = true) @PathVariable("id") String id) {
    return getDescriptors.contentById(projectId, id);
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(DomainNotFoundException ex) {
    log.error(ex.getMessage(), ex);
    return ResponseEntity.notFound().build();
  }
}
