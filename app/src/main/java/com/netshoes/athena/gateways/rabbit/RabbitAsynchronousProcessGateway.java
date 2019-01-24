package com.netshoes.athena.gateways.rabbit;

import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.gateways.AsynchronousProcessGateway;
import com.netshoes.athena.gateways.rabbit.jsons.ProjectDependenciesAnalyzeRequestJson;
import com.netshoes.athena.gateways.rabbit.jsons.ProjectScanRequestJson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class RabbitAsynchronousProcessGateway implements AsynchronousProcessGateway {

  private final RabbitTemplate rabbitTemplate;
  private final Binding projectScanBinding;
  private final Binding projectDependenciesAnalyzeBinding;

  @Override
  public Mono<Void> requestProjectScan(Project project) {
    return Mono.just(project)
        .map(RabbitAsynchronousProcessGateway::toProjectScanRequestJson)
        .flatMap(this::enqueue)
        .then();
  }

  @Override
  public Mono<Void> requestProjectDependencyAnalyze(Project project) {
    return Mono.just(project)
        .map(Project::getId)
        .map(ProjectDependenciesAnalyzeRequestJson::new)
        .flatMap(this::enqueue)
        .then();
  }

  private static ProjectScanRequestJson toProjectScanRequestJson(Project p) {
    final ScmRepository repository = p.getScmRepository();
    return new ProjectScanRequestJson(p.getId(), repository.getId(), p.getBranch().getName());
  }

  private Mono<ProjectDependenciesAnalyzeRequestJson> enqueue(
      ProjectDependenciesAnalyzeRequestJson request) {
    rabbitTemplate.convertAndSend(
        projectDependenciesAnalyzeBinding.getExchange(),
        projectDependenciesAnalyzeBinding.getRoutingKey(),
        request);
    log.debug("Analyze for project {} requested", request.getProjectId());

    return Mono.just(request);
  }

  private Mono<ProjectScanRequestJson> enqueue(ProjectScanRequestJson request) {
    rabbitTemplate.convertAndSend(
        projectScanBinding.getExchange(), projectScanBinding.getRoutingKey(), request);
    log.debug("Scan for project {} requested", request.getProjectId());

    return Mono.just(request);
  }
}
