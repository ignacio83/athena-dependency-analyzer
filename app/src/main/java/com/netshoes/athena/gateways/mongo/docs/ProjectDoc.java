package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryBranch;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects")
@TypeAlias("project")
@CompoundIndexes(
    value = {
      @CompoundIndex(
          name = "ix_artifacts",
          def =
              "{'descriptors.artifacts.groupId':1,"
                  + "'descriptors.artifacts.artifactId':1,"
                  + "'descriptors.artifacts.version':1}")
    })
@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class ProjectDoc implements Serializable {

  @Id private String id;
  @Indexed private String name;
  private String branch;
  private String lastCommitSha;
  private ScmRepositoryDoc scmRepository;
  private List<DependencyManagementDescriptorDoc> descriptors;
  @LastModifiedDate private LocalDateTime lastCollectDate;

  public ProjectDoc(Project domain) {
    final ScmRepositoryBranch domainBranch = domain.getBranch();
    this.id = domain.getId();
    this.name = domain.getName();
    this.branch = domainBranch.getName();
    this.lastCommitSha = domainBranch.getLastCommitSha();

    final Set<DependencyManagementDescriptor> domainDescriptors = domain.getDescriptors();
    this.descriptors =
        domainDescriptors.stream()
            .map(DependencyManagementDescriptorDoc::new)
            .collect(Collectors.toList());
    this.scmRepository = new ScmRepositoryDoc(domain.getScmRepository());
  }

  public Project toDomain(boolean includeDescriptors) {
    final ScmRepository scmRepositoryDomain = scmRepository.toDomain();
    final ScmRepositoryBranch scmRepositoryBranch =
        new ScmRepositoryBranch(branch, lastCommitSha, scmRepositoryDomain);
    final Project project = new Project(scmRepositoryBranch, lastCollectDate);
    if (includeDescriptors) {
      descriptors.stream()
          .map(DependencyManagementDescriptorDoc::toDomain)
          .forEach(d -> project.addDependencyManagerDescriptor(d));
    }
    return project;
  }
}
