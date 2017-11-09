package com.netshoes.athena.usecases.exceptions;

public class ProjectNotFoundException extends DomainNotFoundException {
  private final String projectId;

  public ProjectNotFoundException(String projectId) {
    super("Project not found");
    this.projectId = projectId;
  }
}