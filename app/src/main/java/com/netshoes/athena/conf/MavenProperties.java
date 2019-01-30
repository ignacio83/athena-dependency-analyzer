package com.netshoes.athena.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("application.maven")
@Getter
@Setter
public class MavenProperties {
  private String home = System.getenv("MAVEN_HOME");
  private String localRepositoryDirectory = System.getenv("MAVEN_LOCAL_REPOSITORY");
}
