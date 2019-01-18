package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.AnalyzeExecution;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnalyzeExecutionDoc {
  private AnalyzeTypeDoc analyzeTypeRequested;
  private AnalyzeTypeDoc analyzeTypeExecuted;
  private boolean fallback;
  private String errorLog;
  private LocalDateTime date;

  public AnalyzeExecutionDoc(AnalyzeExecution domain) {
    this.analyzeTypeRequested = AnalyzeTypeDoc.valueOf(domain.getAnalyzeTypeRequested());
    this.analyzeTypeExecuted = AnalyzeTypeDoc.valueOf(domain.getAnalyzeTypeExecuted());
    this.fallback = domain.isFallback();
    this.errorLog = domain.getErrorLog();
    this.date = domain.getDate();
  }

  public AnalyzeExecution toDomain() {
    return new AnalyzeExecution(
        analyzeTypeRequested.toDomain(), analyzeTypeExecuted.toDomain(), fallback, errorLog, date);
  }

  public static AnalyzeExecutionDoc withoutDataDefault() {
    final AnalyzeExecutionDoc execution = new AnalyzeExecutionDoc();
    execution.setAnalyzeTypeRequested(AnalyzeTypeDoc.STATIC);
    execution.setAnalyzeTypeExecuted(AnalyzeTypeDoc.STATIC);
    execution.setFallback(false);
    return execution;
  }
}
