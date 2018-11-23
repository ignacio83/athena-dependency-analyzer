package com.netshoes.athena.gateways;

import lombok.Getter;

public class ScmApiGatewayRateLimitExceededException extends RuntimeException {
  private static final String MESSAGE = "SCM API Rate limit exceeded, try again in %d minutes";
  @Getter private final Long minutesToReset;

  public ScmApiGatewayRateLimitExceededException(Throwable cause, Long minutesToReset) {
    super(String.format(MESSAGE, minutesToReset), cause);
    this.minutesToReset = minutesToReset;
  }

  public ScmApiGatewayRateLimitExceededException(Long minutesToReset) {
    super(String.format(MESSAGE, minutesToReset));
    this.minutesToReset = minutesToReset;
  }
}
