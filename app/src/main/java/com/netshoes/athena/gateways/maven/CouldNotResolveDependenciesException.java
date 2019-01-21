package com.netshoes.athena.gateways.maven;

import lombok.Getter;

public class CouldNotResolveDependenciesException extends RuntimeException {
  @Getter private final String mavenErrorLog;
  @Getter private final ScmRepositoryContentDataPom scmRepositoryContentDataPom;

  public CouldNotResolveDependenciesException(
      Throwable cause, ScmRepositoryContentDataPom scmRepositoryContentDataPom) {
    super(cause);
    this.scmRepositoryContentDataPom = scmRepositoryContentDataPom;
    this.mavenErrorLog = null;
  }

  public CouldNotResolveDependenciesException(
      String message,
      ScmRepositoryContentDataPom scmRepositoryContentDataPom,
      String mavenErrorLog) {
    super(message);
    this.scmRepositoryContentDataPom = scmRepositoryContentDataPom;
    this.mavenErrorLog = mavenErrorLog;
  }

  public CouldNotResolveDependenciesException(
      String message, Throwable cause, ScmRepositoryContentDataPom scmRepositoryContentDataPom) {
    super(message, cause);
    this.scmRepositoryContentDataPom = scmRepositoryContentDataPom;
    this.mavenErrorLog = null;
  }

  public CouldNotResolveDependenciesException(
      String message,
      ScmRepositoryContentDataPom scmRepositoryContentDataPom,
      String mavenErrorLog,
      Throwable cause) {
    super(message, cause);
    this.scmRepositoryContentDataPom = scmRepositoryContentDataPom;
    this.mavenErrorLog = mavenErrorLog;
  }
}
