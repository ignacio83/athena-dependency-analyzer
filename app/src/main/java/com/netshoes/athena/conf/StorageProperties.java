package com.netshoes.athena.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.storage")
@Getter
@Setter
public class StorageProperties {
  private final Local local = new Local();

  @Getter
  @Setter
  public static class Local {
    private String path = System.getProperty("java.io.tmpdir") + "/athena";
  }
}
