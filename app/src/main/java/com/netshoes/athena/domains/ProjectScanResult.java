package com.netshoes.athena.domains;

import lombok.Data;

@Data
public class ProjectScanResult {
  private final Project project;
  private final boolean dependenciesCollected;
  private final boolean analysisExecuted;

  public ProjectScanResult(
      Project project, boolean dependenciesCollected, boolean analysisExecuted) {
    this.project = project;
    this.dependenciesCollected = dependenciesCollected;
    this.analysisExecuted = analysisExecuted;
  }

  public static ProjectScanResult noExecution(Project project) {
    return new ProjectScanResult(project, false, false);
  }
}
