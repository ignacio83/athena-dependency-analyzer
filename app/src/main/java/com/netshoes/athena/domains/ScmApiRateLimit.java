package com.netshoes.athena.domains;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ScmApiRateLimit {
  private final Resource core;
  private final Resource search;
  private final Resource graphql;
  private final Resource summary;

  @Getter
  @Setter
  public static class Resource {
    private final int limit;
    private final int remaining;
    private final Optional<Float> configuredLimitPercentage;
    private final OffsetDateTime reset;

    public Resource(int limit, int remaining, OffsetDateTime reset) {
      this.limit = limit;
      this.remaining = remaining;
      this.configuredLimitPercentage = Optional.empty();
      this.reset = reset;
    }

    public Resource(
        int limit, int remaining, Float configuredLimitPercentage, OffsetDateTime reset) {
      this.limit = limit;
      this.remaining = remaining;
      this.configuredLimitPercentage = Optional.ofNullable(configuredLimitPercentage);
      this.reset = reset;
    }

    public float getPercentageUsedIgnoringConfiguredLimit() {
      float percentageUsed = calculatePercentageUsed(remaining);
      if (configuredLimitPercentage.isPresent()) {
        final Float value = configuredLimitPercentage.get();
        if (percentageUsed > value) {
          percentageUsed = value;
        }
      }
      return percentageUsed;
    }

    public int getUsed() {
      return limit - remaining;
    }

    public float getPercentageUsed() {
      final float percentageUsed =
          getConfiguredLimit()
              .map(configuredLimit -> ((float) getUsed()) / (float) configuredLimit)
              .orElse(getPercentageUsedIgnoringConfiguredLimit());
      return percentageUsed > 1f ? 1f : percentageUsed;
    }

    private float calculatePercentageUsed(int remaining) {
      final BigDecimal remainingBigDecimal = BigDecimal.valueOf((float) remaining);
      final BigDecimal limitBigDecimal = BigDecimal.valueOf((float) limit);
      final BigDecimal availableBigDecimal =
          remainingBigDecimal.divide(limitBigDecimal, 3, RoundingMode.UP);
      return BigDecimal.ONE.subtract(availableBigDecimal).floatValue();
    }

    public Optional<Integer> getConfiguredLimit() {
      return configuredLimitPercentage
          .map(value -> BigDecimal.valueOf((double) (limit * value)))
          .map(BigDecimal::intValueExact);
    }

    public Optional<Integer> getRemainingForConfiguredLimit() {
      return getConfiguredLimit()
          .map(
              configuredLimit -> {
                int remainingForConfiguredLimit = configuredLimit - getUsed();
                if (remainingForConfiguredLimit < 0) {
                  remainingForConfiguredLimit = 0;
                }
                return remainingForConfiguredLimit;
              });
    }
  }
}
