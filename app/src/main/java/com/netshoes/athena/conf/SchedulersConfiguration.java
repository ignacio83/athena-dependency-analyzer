package com.netshoes.athena.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulersConfiguration {

  @Bean
  public Scheduler githubApiScheduler() {
    return Schedulers.newElastic("github-api");
  }

  @Bean
  public Scheduler mavenInvokerScheduler() {
    return Schedulers.newElastic("maven-invoker");
  }
}
