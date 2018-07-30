package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepository;
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
              + "'descriptors.artifacts.version':1}"
    )
  }
)
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class ProjectDoc implements Serializable {

  @Id private String id;
  @Indexed private String name;
  private String branch;
  private ScmRepositoryDoc scmRepository;
  private List<DependencyManagementDescriptorDoc> descriptors;
  @LastModifiedDate private LocalDateTime lastCollectDate;

  public ProjectDoc(Project domain) {
    final ScmRepository domainScmRepository = domain.getScmRepository();
    this.id = domain.getId();
    this.name = domain.getName();
    this.branch = domain.getBranch();

    final Set<DependencyManagementDescriptor> domainDescriptors = domain.getDescriptors();
    this.descriptors =
        domainDescriptors
            .stream()
            .map(DependencyManagementDescriptorDoc::new)
            .collect(Collectors.toList());
    this.scmRepository = new ScmRepositoryDoc(domainScmRepository);
  }

  public Project toDomain(boolean includeDescriptors) {
    final ScmRepository scmRepositoryDomain = scmRepository.toDomain();
    final Project project = new Project(scmRepositoryDomain, branch, lastCollectDate);
    if (includeDescriptors) {
      descriptors
          .stream()
          .map(DependencyManagementDescriptorDoc::toDomain)
          .forEach(d -> project.addDependencyManagerDescriptor(d));
    }
    return project;
  }
}
