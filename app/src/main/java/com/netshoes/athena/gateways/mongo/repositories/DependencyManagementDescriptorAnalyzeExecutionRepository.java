package com.netshoes.athena.gateways.mongo.repositories;

import com.netshoes.athena.gateways.mongo.docs.DependencyManagementDescriptorAnalyzeExecutionDoc;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DependencyManagementDescriptorAnalyzeExecutionRepository
    extends ReactiveCrudRepository<DependencyManagementDescriptorAnalyzeExecutionDoc, String> {}
