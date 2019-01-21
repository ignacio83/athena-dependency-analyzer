package com.netshoes.athena.gateways.maven;

import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class ScmRepositoryContentDataPom {
  private final Project project;
  private final Path storagePath;
  private final String path;
  private final ScmRepositoryContentData contentData;
}
