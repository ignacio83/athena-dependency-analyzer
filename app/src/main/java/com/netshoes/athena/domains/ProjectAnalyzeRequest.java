package com.netshoes.athena.domains;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProjectAnalyzeRequest {
  private final Project project;
  private final List<ScmRepositoryContentData> list;
}
