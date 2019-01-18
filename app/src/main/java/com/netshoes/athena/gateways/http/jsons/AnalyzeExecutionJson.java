package com.netshoes.athena.gateways.http.jsons;

import com.netshoes.athena.domains.AnalyzeExecution;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AnalyzeExecutionJson {

  @ApiModelProperty(value = "Type of analyze requested", required = true)
  private final AnalyzeType analyzeTypeRequested;

  @ApiModelProperty(value = "Type of analyze executed", required = true)
  private final AnalyzeType analyzeTypeExecuted;

  @ApiModelProperty(value = "Indicate if is a fallback", required = true)
  private final boolean fallback;

  @ApiModelProperty(value = "Error log")
  private final String errorLog;

  AnalyzeExecutionJson(AnalyzeExecution execution) {
    this.analyzeTypeRequested = AnalyzeType.valueOf(execution.getAnalyzeTypeRequested().name());
    this.analyzeTypeExecuted = AnalyzeType.valueOf(execution.getAnalyzeTypeExecuted().name());
    this.fallback = execution.isFallback();
    this.errorLog = execution.getErrorLog();
  }
}
