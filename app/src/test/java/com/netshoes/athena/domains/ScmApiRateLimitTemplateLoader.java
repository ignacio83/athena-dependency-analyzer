package com.netshoes.athena.domains;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.Rule;
import br.com.six2six.fixturefactory.loader.TemplateLoader;
import java.time.OffsetDateTime;

public class ScmApiRateLimitTemplateLoader implements TemplateLoader {
  public static final String ONE_HUNDRED_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT =
      "one_hundred_percent_available_no_configured_limit";
  public static final String ONE_HUNDRED_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT =
      "one_hundred_percent_available_for_configured_limit";
  public static final String TEN_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT =
      "ten_percent_available_no_configured_limit";
  public static final String TEN_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT =
      "ten_percent_available_for_configured_limit";
  public static final String REQUESTS_UNAVAILABLE_NO_CONFIGURED_LIMIT =
      "requests_unavailable_no_configured_limit";
  public static final String REQUESTS_UNAVAILABLE_FOR_CONFIGURED_LIMIT =
      "requests_unavailable_for_configured_limit";
  public static final String REQUESTS_VERY_UNAVAILABLE_FOR_CONFIGURED_LIMIT =
      "requests_more_unavailable_for_configured_limit";

  @Override
  public void load() {
    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            ONE_HUNDRED_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(1000, 1000, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(1000, 1000, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(1000, 1000, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(1000, 1000, OffsetDateTime.now().plusHours(1)));
              }
            });

    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            TEN_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(1000, 900, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(1000, 900, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(1000, 900, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(1000, 900, OffsetDateTime.now().plusHours(1)));
              }
            });

    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            REQUESTS_UNAVAILABLE_NO_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(1000, 0, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(1000, 0, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(1000, 0, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(1000, 0, OffsetDateTime.now().plusHours(1)));
              }
            });

    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            ONE_HUNDRED_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(
                        1000, 1000, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(
                        1000, 1000, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(
                        1000, 1000, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(
                        1000, 1000, 0.5f, OffsetDateTime.now().plusHours(1)));
              }
            });

    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            TEN_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(
                        1000, 600, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(
                        1000, 600, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(
                        1000, 600, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(
                        1000, 600, 0.5f, OffsetDateTime.now().plusHours(1)));
              }
            });
    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            REQUESTS_UNAVAILABLE_FOR_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(
                        1000, 500, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(
                        1000, 500, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(
                        1000, 500, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(
                        1000, 500, 0.5f, OffsetDateTime.now().plusHours(1)));
              }
            });
    Fixture.of(ScmApiRateLimit.class)
        .addTemplate(
            REQUESTS_VERY_UNAVAILABLE_FOR_CONFIGURED_LIMIT,
            new Rule() {
              {
                add(
                    "core",
                    new ScmApiRateLimit.Resource(
                        1000, 450, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "search",
                    new ScmApiRateLimit.Resource(
                        1000, 450, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "graphql",
                    new ScmApiRateLimit.Resource(
                        1000, 450, 0.5f, OffsetDateTime.now().plusHours(1)));
                add(
                    "summary",
                    new ScmApiRateLimit.Resource(
                        1000, 450, 0.5f, OffsetDateTime.now().plusHours(1)));
              }
            });
  }
}
