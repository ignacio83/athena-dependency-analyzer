package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.AnalyzeType;

public enum AnalyzeTypeDoc {
  STATIC,
  DEPENDENCY_RESOLUTION;

  AnalyzeType toDomain() {
    AnalyzeType analyzeType = null;
    switch (this) {
      case STATIC:
        analyzeType = AnalyzeType.STATIC;
        break;
      case DEPENDENCY_RESOLUTION:
        analyzeType = AnalyzeType.DEPENDENCY_RESOLUTION;
        break;
    }
    return analyzeType;
  }

  public static AnalyzeTypeDoc valueOf(AnalyzeType analyzeType) {
    return analyzeType != null ? AnalyzeTypeDoc.valueOf(analyzeType.name()) : null;
  }
}
