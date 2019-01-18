package com.netshoes.athena.gateways;

import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeResult;
import com.netshoes.athena.domains.ProjectAnalyzeRequest;
import reactor.core.publisher.Flux;

public interface DependencyManagerGateway {

  Flux<DependencyManagementDescriptorAnalyzeResult> staticAnalyse(ProjectAnalyzeRequest request);

  Flux<DependencyManagementDescriptorAnalyzeResult> resolveDependenciesAnalyse(
      ProjectAnalyzeRequest request);
}
