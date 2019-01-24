package com.netshoes.athena.domains;

import java.io.Serializable;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.Data;

@Data
public class ScmRepository implements Serializable {

  private String id;
  private String name;
  private String description;
  private URL url;
  private String masterBranch;
  private Optional<OffsetDateTime> lastPushDate = Optional.empty();
}
