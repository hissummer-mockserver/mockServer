package com.hissummer.mockserver.mgmt.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.service.jpa.HttpConditionRuleMongoRepository;

public class HttpConditionRuleServiceImpl {

	@Autowired
	HttpConditionRuleMongoRepository httpConditionRuleMongoRepository;

	public HttpConditionRule addHttpConditionRule(HttpConditionRule conditionRules) {

		return httpConditionRuleMongoRepository.insert(conditionRules);

	}

	public boolean updateHttpConditionRule(HttpConditionRule conditionRules) {

		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRules.getId());

		if (httpConditionRule.isPresent()) {

			conditionRules.getConditionRules().forEach((order, condition) ->

			httpConditionRule.get().getConditionRules().put(order, condition)

			);

			httpConditionRuleMongoRepository.save(httpConditionRule.get());
			return true;
		}

		return false;

	}

	public boolean deleteAllHttpConditionRules(HttpConditionRule conditionRules) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRules.getId());
		if (httpConditionRule.isPresent()) {

			httpConditionRuleMongoRepository.delete(httpConditionRule.get());
			;
			return true;

		} else
			return false;
	}

	public boolean deleteHttpConditionRules(HttpConditionRule conditionRules) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRules.getId());
		if (httpConditionRule.isPresent()) {

			conditionRules.getConditionRules().forEach((order, condition) ->

			httpConditionRule.get().getConditionRules().remove(order)

			);
			httpConditionRuleMongoRepository.save(httpConditionRule.get());

			return true;

		} else
			return false;
	}

	public HttpConditionRule getHttpConditionRules(String mockRuleId) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findByHttpMockRuleId(mockRuleId);

		if (httpConditionRule.isPresent()) {

			return httpConditionRule.get();

		} else
			return null;
	}

}
