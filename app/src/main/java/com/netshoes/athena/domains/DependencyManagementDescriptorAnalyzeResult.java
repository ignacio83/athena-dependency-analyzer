package com.netshoes.athena.domains;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class DependencyManagementDescriptorAnalyzeResult {
  private final DependencyManagementDescriptorAnalyzeExecution execution;
  private final DependencyManagementDescriptor dependencyManagementDescriptor;
}
