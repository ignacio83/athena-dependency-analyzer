package com.netshoes.athena.domains;

import static br.com.six2six.fixturefactory.Fixture.from;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import com.netshoes.athena.domains.ScmApiRateLimit.Resource;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScmApiRateLimitTest {

  @BeforeClass
  public static void loadTemplates() {
    FixtureFactoryLoader.loadTemplates("com.netshoes.athena");
  }

  @Test
  public void hundredPercentAvailableNoConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.ONE_HUNDRED_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT);

    validateOneHundredPercentUsedNoConfiguredLimit(scmApiRateLimit.getCore());
    validateOneHundredPercentUsedNoConfiguredLimit(scmApiRateLimit.getSearch());
    validateOneHundredPercentUsedNoConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void tenPercentAvailableNoConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.TEN_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT);

    validateTenPercentUsedNoConfiguredLimit(scmApiRateLimit.getCore());
    validateTenPercentUsedNoConfiguredLimit(scmApiRateLimit.getSearch());
    validateTenPercentUsedNoConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void requestsUnavailableNoConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.REQUESTS_UNAVAILABLE_NO_CONFIGURED_LIMIT);

    validateUnavailableNoConfiguredLimit(scmApiRateLimit.getCore());
    validateUnavailableNoConfiguredLimit(scmApiRateLimit.getSearch());
    validateUnavailableNoConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void oneHundredPercentAvailableForConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(
                ScmApiRateLimitTemplateLoader.ONE_HUNDRED_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT);

    validateOneHundredPercentUsedForConfiguredLimit(scmApiRateLimit.getCore());
    validateOneHundredPercentUsedForConfiguredLimit(scmApiRateLimit.getSearch());
    validateOneHundredPercentUsedForConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void tenPercentAvailableForConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.TEN_PERCENT_AVAILABLE_FOR_CONFIGURED_LIMIT);

    validateTenPercentUsedForConfiguredLimit(scmApiRateLimit.getCore());
    validateTenPercentUsedForConfiguredLimit(scmApiRateLimit.getSearch());
    validateTenPercentUsedForConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void requestsUnavailableForConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.REQUESTS_UNAVAILABLE_FOR_CONFIGURED_LIMIT);

    validateUnavailableForConfiguredLimit(scmApiRateLimit.getCore());
    validateUnavailableForConfiguredLimit(scmApiRateLimit.getSearch());
    validateUnavailableForConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  @Test
  public void requestsVeryUnavailableForConfiguredLimit() {
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.REQUESTS_VERY_UNAVAILABLE_FOR_CONFIGURED_LIMIT);

    validateUnavailableForConfiguredLimit(scmApiRateLimit.getCore());
    validateUnavailableForConfiguredLimit(scmApiRateLimit.getSearch());
    validateUnavailableForConfiguredLimit(scmApiRateLimit.getGraphql());
  }

  private void validateOneHundredPercentUsedNoConfiguredLimit(Resource resource) {
    assertThat(resource.getRemainingForConfiguredLimit().isPresent()).isFalse();
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(0f);
    assertThat(resource.getPercentageUsed()).isEqualTo(0f);
  }

  private void validateTenPercentUsedNoConfiguredLimit(Resource resource) {
    assertThat(resource.getRemainingForConfiguredLimit().isPresent()).isFalse();
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(0.1f);
    assertThat(resource.getPercentageUsed()).isEqualTo(0.1f);
  }

  private void validateUnavailableNoConfiguredLimit(Resource resource) {
    assertThat(resource.getRemainingForConfiguredLimit().isPresent()).isFalse();
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(1f);
    assertThat(resource.getPercentageUsed()).isEqualTo(1f);
  }

  private void validateOneHundredPercentUsedForConfiguredLimit(Resource resource) {
    final Optional<Integer> remainingForConfiguredLimit = resource.getRemainingForConfiguredLimit();
    assertThat(remainingForConfiguredLimit.isPresent()).isTrue();
    assertThat(remainingForConfiguredLimit.get()).isEqualTo(500);
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(0f);
    assertThat(resource.getPercentageUsed()).isEqualTo(0f);
  }

  private void validateTenPercentUsedForConfiguredLimit(Resource resource) {
    final Optional<Integer> remainingForConfiguredLimit = resource.getRemainingForConfiguredLimit();
    assertThat(remainingForConfiguredLimit.isPresent()).isTrue();
    assertThat(remainingForConfiguredLimit.get()).isEqualTo(100);
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(0.4f);
    assertThat(resource.getPercentageUsed()).isEqualTo(0.8f);
  }

  private void validateUnavailableForConfiguredLimit(Resource resource) {
    final Optional<Integer> remainingForConfiguredLimit = resource.getRemainingForConfiguredLimit();
    assertThat(remainingForConfiguredLimit.isPresent()).isTrue();
    assertThat(remainingForConfiguredLimit.get()).isEqualTo(0);
    assertThat(resource.getPercentageUsedIgnoringConfiguredLimit()).isEqualTo(0.5f);
    assertThat(resource.getPercentageUsed()).isEqualTo(1f);
  }
}
