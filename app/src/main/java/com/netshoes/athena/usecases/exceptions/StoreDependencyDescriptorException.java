package com.netshoes.athena.usecases.exceptions;

import com.netshoes.athena.domains.ScmRepositoryContent;

public class StoreDependencyDescriptorException extends RuntimeException {
  private static final String MESSAGE_FORMAT = "Fail on store dependency descriptor for %s";

  public StoreDependencyDescriptorException(
      ScmRepositoryContent scmRepositoryContent, Throwable cause) {
    super(String.format(MESSAGE_FORMAT, scmRepositoryContent.getPath()), cause);
  }
}
