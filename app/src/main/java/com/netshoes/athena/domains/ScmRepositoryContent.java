package com.netshoes.athena.domains;

import lombok.Data;

@Data
public class ScmRepositoryContent {

  private final ScmRepository repository;
  private final String path;
  private final String name;
  private final ContentType type;
  private final long size;
  private final long depth;

  public ScmRepositoryContent(
      ScmRepository repository, String path, String name, ContentType type, long size) {
    this.repository = repository;
    this.path = path;
    this.name = name;
    this.type = type;
    this.size = size;
    this.depth = path.chars().filter(c -> c == '/').count();
  }

  public String getStoragePath() {
    return String.format("/%s/%s", repository.getName(), path);
  }

  public String getPathWithoutRootSlash() {
    return "/".equals(path) ? "" : path;
  }

  public boolean isDirectory() {
    return ContentType.DIRECTORY.equals(type);
  }
}
