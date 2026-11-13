package com.tenextakehome.service;

import com.tenextakehome.dto.DetectionRuleRequest;
import com.tenextakehome.exception.ResourceNotFoundException;
import com.tenextakehome.model.DetectionRule;
import com.tenextakehome.repository.DetectionRuleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DetectionRuleService {

    private static final String RULE_ID_SEQUENCE_KEY = "detection-rules:sequence";
    private static final long FIRST_RULE_ID = 1000;
    private static final long LAST_RULE_ID = 9999;

    private final DetectionRuleRepository detectionRuleRepository;
    private final StringRedisTemplate redisTemplate;

    public DetectionRuleService(
            DetectionRuleRepository detectionRuleRepository,
            StringRedisTemplate redisTemplate) {
        this.detectionRuleRepository = detectionRuleRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<DetectionRule> getAllRules() {
        List<DetectionRule> rules = new ArrayList<>();
        detectionRuleRepository.findAll().forEach(rules::add);
        return rules;
    }

    public DetectionRule getRule(Long id) {
        return detectionRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detection rule not found with id: " + id));
    }

    public DetectionRule createRule(DetectionRuleRequest request) {
        DetectionRule rule = new DetectionRule(
                request.getName(),
                nextRuleId(),
                request.getRuleLogic(),
                request.getDescription());
        return detectionRuleRepository.save(rule);
    }

    public DetectionRule updateRule(Long id, DetectionRuleRequest request) {
        DetectionRule rule = getRule(id);
        rule.setName(request.getName());
        rule.setRuleLogic(request.getRuleLogic());
        rule.setDescription(request.getDescription());
        return detectionRuleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        if (!detectionRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Detection rule not found with id: " + id);
        }
        detectionRuleRepository.deleteById(id);
    }

    private Long nextRuleId() {
        long highestExistingId = getAllRules().stream()
                .map(DetectionRule::getId)
                .filter(id -> id != null)
                .mapToLong(Long::longValue)
                .max()
                .orElse(FIRST_RULE_ID - 1);
        redisTemplate.opsForValue().setIfAbsent(RULE_ID_SEQUENCE_KEY, String.valueOf(highestExistingId));

        Long nextId = redisTemplate.opsForValue().increment(RULE_ID_SEQUENCE_KEY);
        if (nextId == null) {
            throw new IllegalStateException("Unable to allocate detection rule ID from Redis");
        }
        if (nextId > LAST_RULE_ID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "All four-digit detection rule IDs have been used");
        }
        return nextId;
    }
}
