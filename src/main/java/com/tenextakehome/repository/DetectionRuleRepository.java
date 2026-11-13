package com.tenextakehome.repository;

import com.tenextakehome.model.DetectionRule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionRuleRepository extends CrudRepository<DetectionRule, Long> {
}
