package com.netshoes.athena.gateways.http.jsons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.netshoes.athena.domains.ScmApiRateLimit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@ApiModel(value = "ScmApiRateLimit")
public class ScmApiRateLimitJson {

  @ApiModelProperty(value = "Limit rate", required = true)
  private final ScmApiRateLimitResourceJson summary;

  @ApiModelProperty(value = "Limit for each core resource", required = true)
  private final ScmApiRateLimitResourceJson core;

  @ApiModelProperty(value = "Limit for each search resource", required = true)
  private final ScmApiRateLimitResourceJson search;

  @ApiModelProperty(value = "Limit for each graphql resource", required = true)
  private final ScmApiRateLimitResourceJson graphql;

  public ScmApiRateLimitJson(ScmApiRateLimit domain) {
    this.summary = new ScmApiRateLimitResourceJson(domain.getSummary());
    this.core = new ScmApiRateLimitResourceJson(domain.getCore());
    this.search = new ScmApiRateLimitResourceJson(domain.getSearch());
    this.graphql = new ScmApiRateLimitResourceJson(domain.getGraphql());
  }
}
