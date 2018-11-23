package com.netshoes.athena.gateways.github.jsons;

import com.netshoes.athena.domains.ScmApiRateLimit;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RateLimitResponseJson {
  private Resources resources;
  private Resource rate;

  public ScmApiRateLimit toDomain(
      Float coreLimitPercentage, Float searchLimitPercentage, Float graphqlLimitPercentage) {

    final ScmApiRateLimit.Resource coreDomain = resources.getCore().toDomain(coreLimitPercentage);
    final ScmApiRateLimit.Resource searchDomain =
        resources.getSearch().toDomain(searchLimitPercentage);
    final ScmApiRateLimit.Resource graphqlDomain =
        resources.getSearch().toDomain(graphqlLimitPercentage);

    final ScmApiRateLimit.Resource rateDomain = rate.toDomain();

    return new ScmApiRateLimit(coreDomain, searchDomain, graphqlDomain, rateDomain);
  }

  @Getter
  @Setter
  public static class Resources {
    private Resource core;
    private Resource search;
    private Resource graphql;
  }

  @Getter
  @Setter
  public static class Resource {
    private int limit;
    private int remaining;
    private long reset;

    public ScmApiRateLimit.Resource toDomain() {
      return toDomain(null);
    }

    public ScmApiRateLimit.Resource toDomain(Float limitPercentage) {
      final OffsetDateTime offsetDateTime =
          OffsetDateTime.ofInstant(Instant.ofEpochSecond(reset), ZoneId.of("UTC"));
      return new ScmApiRateLimit.Resource(limit, remaining, limitPercentage, offsetDateTime);
    }
  }
}
