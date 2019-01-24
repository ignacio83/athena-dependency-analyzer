package com.netshoes.athena.gateways.http.jsons;

import com.netshoes.athena.domains.Project;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Request of project scan")
public class RequestProjectScanJson {

  @ApiModelProperty(value = "Id of project repository", required = true)
  private final String id;

  @ApiModelProperty(value = "URL of repository", required = true)
  private final String url;

  @ApiModelProperty(value = "Branch of scan process", required = true)
  private final String branch;

  public RequestProjectScanJson(Project project) {
    this.id = project.getId();
    this.url = project.getScmRepository().getUrl().toString();
    this.branch = project.getBranch().getName();
  }
}
