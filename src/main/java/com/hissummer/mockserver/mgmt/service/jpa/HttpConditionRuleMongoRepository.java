package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;

public interface HttpConditionRuleMongoRepository extends MongoRepository<HttpConditionRule, String> {

	Optional<HttpConditionRule> findById(String id);

	Optional<HttpConditionRule> findByHttpMockRuleId(String httpMockRuleId);

	void deleteById(String id);

	void deleteByHttpMockRuleId(String httpMockRuleId);

}
