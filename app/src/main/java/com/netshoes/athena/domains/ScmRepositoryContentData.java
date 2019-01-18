package com.netshoes.athena.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ScmRepositoryContentData {
  private final ScmRepositoryContent scmRepositoryContent;
  private final String data;
  private final long size;
}
