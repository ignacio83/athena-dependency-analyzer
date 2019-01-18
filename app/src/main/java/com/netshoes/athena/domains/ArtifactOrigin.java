package com.netshoes.athena.domains;

import lombok.Getter;

public enum ArtifactOrigin {
  PROJECT(1),
  PARENT(2),
  DEPENDENCIES_MANAGEMENT(3),
  DEPENDENCIES(4);

  @Getter private int order;

  ArtifactOrigin(int order) {
    this.order = order;
  }
}
