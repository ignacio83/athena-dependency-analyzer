package com.netshoes.athena.gateways;

import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeResult;
import com.netshoes.athena.domains.ProjectCollectDependenciesRequest;
import reactor.core.publisher.Flux;

public interface DependencyManagerGateway {

  Flux<DependencyManagementDescriptorAnalyzeResult> staticAnalyse(
      ProjectCollectDependenciesRequest request);

  Flux<DependencyManagementDescriptorAnalyzeResult> resolveDependenciesAnalyse(
      ProjectCollectDependenciesRequest request);
}
