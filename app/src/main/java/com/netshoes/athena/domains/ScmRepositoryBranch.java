package com.netshoes.athena.domains;

import java.io.Serializable;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ScmRepositoryBranch implements Serializable {
  private final String name;
  private final String lastCommitSha;
  private final ScmRepository scmRepository;

  private ScmRepositoryBranch(String name, ScmRepository scmRepository) {
    this.scmRepository = scmRepository;
    this.name = name;
    this.lastCommitSha = null;
  }

  public static ScmRepositoryBranch offline(String name, String repositoryId) {
    final ScmRepository scmRepository = new ScmRepository();
    scmRepository.setId(repositoryId);
    return new ScmRepositoryBranch(name, scmRepository);
  }

  public static ScmRepositoryBranch offline(String name, ScmRepository scmRepository) {
    return new ScmRepositoryBranch(name, scmRepository);
  }

  public boolean modified(ScmRepositoryBranch other) {
    boolean updated;
    final String lastCommitShaOther = other.getLastCommitSha();
    if (lastCommitShaOther == null) {
      updated = this.lastCommitSha != null;
    } else {
      updated = !lastCommitShaOther.equals(this.lastCommitSha);
    }
    return updated;
  }
}
