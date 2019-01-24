package com.netshoes.athena.gateways.http.jsons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.netshoes.athena.domains.ProjectScanResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@JsonInclude(Include.NON_NULL)
@ApiModel(value = "Scan Result")
@Slf4j
public class ScanResultJson {

  @ApiModelProperty(value = "Project", required = true)
  private final ProjectJson project;

  @ApiModelProperty(value = "Indicate if dependencies was collect", required = true)
  private final boolean dependenciesCollected;

  @ApiModelProperty(value = "Indicate if analysis was executed", required = true)
  private final boolean analysisExecuted;

  public ScanResultJson(ProjectScanResult domain) {
    this.project = new ProjectJson(domain.getProject());
    this.dependenciesCollected = domain.isDependenciesCollected();
    this.analysisExecuted = domain.isAnalysisExecuted();
  }
}
