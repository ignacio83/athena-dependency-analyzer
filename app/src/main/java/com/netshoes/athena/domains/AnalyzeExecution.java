package com.netshoes.athena.domains;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AnalyzeExecution {
  private final AnalyzeType analyzeTypeRequested;
  private final AnalyzeType analyzeTypeExecuted;
  private final boolean fallback;
  private final String errorLog;
  private final LocalDateTime date;

  public AnalyzeExecution(
      AnalyzeType analyzeTypeRequested, AnalyzeType analyzeTypeExecuted, String errorLog) {
    this.analyzeTypeRequested = analyzeTypeRequested;
    this.analyzeTypeExecuted = analyzeTypeExecuted;
    this.fallback = !analyzeTypeRequested.equals(analyzeTypeExecuted);
    this.errorLog = errorLog;
    this.date = LocalDateTime.now();
  }

  public static AnalyzeExecution success(AnalyzeType analyzeType) {
    return new AnalyzeExecution(analyzeType, analyzeType, null);
  }

  public static AnalyzeExecution fallback(
      AnalyzeType analyzeTypeRequested, AnalyzeType analyzeTypeExecuted, String errorLog) {
    return new AnalyzeExecution(analyzeTypeRequested, analyzeTypeExecuted, errorLog);
  }
}
