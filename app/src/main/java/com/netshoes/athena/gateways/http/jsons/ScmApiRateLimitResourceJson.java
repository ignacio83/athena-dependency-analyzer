package com.netshoes.athena.gateways.http.jsons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.netshoes.athena.domains.ScmApiRateLimit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@ApiModel(value = "ScmApiRateResourceLimit")
public class ScmApiRateLimitResourceJson {

  @ApiModelProperty(value = "Request limit", required = true)
  private final Integer limit;

  @ApiModelProperty(value = "Configured request limit", required = true)
  private final Integer configuredLimit;

  @ApiModelProperty(value = "Remaining requests", required = true)
  private final Integer remaining;

  @ApiModelProperty(value = "Configured limit in percentage", required = true)
  private final Float configuredLimitPercentage;

  @ApiModelProperty(value = "Remaining requests for configured percentage limit", required = true)
  private final Integer remainingForConfiguredLimit;

  @ApiModelProperty(value = "Percentage used of requests", required = true)
  private final Float percentageUsed;

  @ApiModelProperty(value = "Next reset for request counter", required = true)
  private final OffsetDateTime reset;

  public ScmApiRateLimitResourceJson(ScmApiRateLimit.Resource domain) {
    this.limit = domain.getLimit();
    this.configuredLimit = domain.getConfiguredLimit().orElse(null);
    this.remaining = domain.getRemaining();
    this.configuredLimitPercentage = domain.getConfiguredLimitPercentage().orElse(null);
    this.remainingForConfiguredLimit = domain.getRemainingForConfiguredLimit().orElse(null);
    this.percentageUsed = domain.getPercentageUsed();
    this.reset = domain.getReset();
  }
}
