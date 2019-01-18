package com.netshoes.athena.domains;

import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class DependencyManagementDescriptorAnalyzeExecution {
  private final String id;
  private final EmbeddedProject project;
  private final String descriptorPath;
  private final AnalyzeExecution execution;

  public DependencyManagementDescriptorAnalyzeExecution(
      String projectId, String projectName, String descriptorPath, AnalyzeExecution execution) {
    this.id = UUID.randomUUID().toString();
    this.project = new EmbeddedProject(projectId, projectName);
    this.descriptorPath = descriptorPath;
    this.execution = execution;
  }

  public DependencyManagementDescriptorAnalyzeExecution(
      Project project, String descriptorPath, AnalyzeExecution execution) {
    this.id = UUID.randomUUID().toString();
    this.project = new EmbeddedProject(project.getId(), project.getName());
    this.descriptorPath = descriptorPath;
    this.execution = execution;
  }

  @RequiredArgsConstructor
  @Data
  public static class EmbeddedProject {
    final String id;
    final String name;
  }
}
