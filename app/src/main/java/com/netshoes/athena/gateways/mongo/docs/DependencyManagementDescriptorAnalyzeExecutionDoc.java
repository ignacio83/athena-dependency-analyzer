package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.AnalyzeExecution;
import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeExecution;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dependencyManagementDescriptorAnalyzeExecution")
@TypeAlias("dependencyManagementDescriptorAnalyzeExecution")
@NoArgsConstructor
@Data
public class DependencyManagementDescriptorAnalyzeExecutionDoc {
  @Id private String id;
  private EmbeddedProject project;
  private String descriptorPath;
  private AnalyzeExecutionDoc execution;
  @LastModifiedDate private LocalDateTime lastModifiedDate;

  public DependencyManagementDescriptorAnalyzeExecutionDoc(
      DependencyManagementDescriptorAnalyzeExecution domain) {
    this.id = domain.getId();
    this.project = new EmbeddedProject(domain.getProject());
    this.descriptorPath = domain.getDescriptorPath();
    this.execution = new AnalyzeExecutionDoc(domain.getExecution());
  }

  public DependencyManagementDescriptorAnalyzeExecution toDomain() {
    final AnalyzeExecution execution = this.execution.toDomain();
    return new DependencyManagementDescriptorAnalyzeExecution(
        this.id, this.project.toDomain(), this.descriptorPath, execution);
  }

  @Data
  @RequiredArgsConstructor
  public static class EmbeddedProject {
    @Indexed final String id;

    @Indexed final String name;

    public EmbeddedProject(DependencyManagementDescriptorAnalyzeExecution.EmbeddedProject domain) {
      this.id = domain.getId();
      this.name = domain.getName();
    }

    public DependencyManagementDescriptorAnalyzeExecution.EmbeddedProject toDomain() {
      return new DependencyManagementDescriptorAnalyzeExecution.EmbeddedProject(id, name);
    }
  }
}
