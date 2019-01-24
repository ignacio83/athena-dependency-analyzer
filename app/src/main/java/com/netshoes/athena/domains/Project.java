package com.netshoes.athena.domains;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Base64Utils;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"scmRepository.id", "branch"})
public class Project {

  private final String id;
  private final String name;
  private final ScmRepositoryBranch branch;
  private final LocalDateTime lastCollectDate;
  private final Set<DependencyManagementDescriptor> descriptors;

  public Project(ScmRepositoryBranch scmRepositoryBranch) {
    this.branch = scmRepositoryBranch;
    this.id = generateId(scmRepositoryBranch);
    this.name = scmRepositoryBranch.getScmRepository().getName();
    this.lastCollectDate = null;
    this.descriptors = new TreeSet<>();
  }

  public Project(ScmRepositoryBranch scmRepositoryBranch, LocalDateTime lastCollectDate) {
    this.branch = scmRepositoryBranch;
    this.id = generateId(scmRepositoryBranch);
    this.name = scmRepositoryBranch.getScmRepository().getName();
    this.lastCollectDate = lastCollectDate;
    this.descriptors = new TreeSet<>();
  }

  private static String generateId(ScmRepositoryBranch scmRepositoryBranch) {
    final ScmRepository scmRepository = scmRepositoryBranch.getScmRepository();
    final String branchName = scmRepositoryBranch.getName();
    final String baseId = MessageFormat.format("{0}${1}", scmRepository.getId(), branchName);
    return Base64Utils.encodeToUrlSafeString(baseId.getBytes(StandardCharsets.UTF_8));
  }

  public ScmRepository getScmRepository() {
    return branch.getScmRepository();
  }

  public Project scmRepositoryBranch(ScmRepositoryBranch scmRepositoryBranch) {
    return new Project(
        this.id, this.name, scmRepositoryBranch, this.lastCollectDate, this.descriptors);
  }

  public Project addDependencyManagerDescriptor(DependencyManagementDescriptor descriptor) {
    this.descriptors.add(descriptor);
    return this;
  }

  public Project clearDependencyManagerDescriptors() {
    this.descriptors.clear();
    return this;
  }

  public boolean neverCollected() {
    return lastCollectDate == null;
  }

  @Override
  public String toString() {
    return name;
  }
}
