package com.netshoes.athena.gateways.http.jsons;

import com.netshoes.athena.domains.ScmRepository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@ApiModel(value = "SourceControlManagerRepository")
public class ScmRepositoryJson {

  @ApiModelProperty(value = "Id of repository", required = true)
  private final String id;

  @ApiModelProperty(value = "Name of repository", required = true)
  private final String name;

  @ApiModelProperty(value = "Description of repository", required = true)
  private final String description;

  @ApiModelProperty(value = "URL of repository", required = true)
  private final String url;

  @ApiModelProperty(value = "Master branch of repository", required = true)
  private final String masterBranch;

  @ApiModelProperty(value = "Date of last push to repository")
  private final OffsetDateTime lastPushDate;

  public ScmRepositoryJson(ScmRepository domain) {
    this.id = domain.getId();
    this.name = domain.getName();
    this.description = domain.getDescription();
    this.url = domain.getUrl().toString();
    this.masterBranch = domain.getMasterBranch();
    this.lastPushDate = domain.getLastPushDate().orElse(null);
  }
}
