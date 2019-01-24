package com.netshoes.athena.gateways.http.jsons;

import com.netshoes.athena.domains.ScmRepositoryBranch;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@Data
@ApiModel(value = "Branch")
public class ScmRepositoryBranchJson implements Serializable {

  @ApiModelProperty(value = "Name of branch", required = true)
  private String name;

  @ApiModelProperty(value = "Last commit ", required = true)
  private String lastCommitSha;

  @ApiModelProperty(value = "Info about Source Control Management repository", required = true)
  private ScmRepositoryJson repository;

  public ScmRepositoryBranchJson(ScmRepositoryBranch domain) {
    this.name = domain.getName();
    this.lastCommitSha = domain.getLastCommitSha();
    this.repository = new ScmRepositoryJson(domain.getScmRepository());
  }
}
