package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;

public interface HttpConditionRuleMongoRepository extends MongoRepository<HttpConditionRule, String> {

	@NotNull
	Optional<HttpConditionRule> findById(@NotNull String id);

	Optional<HttpConditionRule> findByHttpMockRuleId(String httpMockRuleId);

	void deleteById(@NotNull String id);

	void deleteByHttpMockRuleId(String httpMockRuleId);

}
