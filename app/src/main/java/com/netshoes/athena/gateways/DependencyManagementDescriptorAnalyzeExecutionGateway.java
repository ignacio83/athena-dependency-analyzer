package com.netshoes.athena.gateways;

import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeExecution;
import reactor.core.publisher.Mono;

public interface DependencyManagementDescriptorAnalyzeExecutionGateway {

  Mono<DependencyManagementDescriptorAnalyzeExecution> save(
      DependencyManagementDescriptorAnalyzeExecution execution);
}
