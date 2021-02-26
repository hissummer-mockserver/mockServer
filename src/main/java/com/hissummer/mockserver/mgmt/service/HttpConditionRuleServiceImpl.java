package com.hissummer.mockserver.mgmt.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.service.jpa.HttpConditionRuleMongoRepository;

public class HttpConditionRuleServiceImpl {

	@Autowired
	HttpConditionRuleMongoRepository httpConditionRuleMongoRepository;

	public HttpConditionRule addHttpConditionRule(HttpConditionRule conditionRules) {
		HttpConditionRule rule = getHttpConditionRulesByHttpMockRuleId(conditionRules.getHttpMockRuleId());
		if(rule == null)
			return httpConditionRuleMongoRepository.insert(conditionRules);
		else {
			throw ServiceException.builder().status(0).serviceMessage("conditionrule already exist.").build();

		}

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

	public boolean deleteWholeHttpConditionRules(HttpConditionRule conditionRules) {
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

	public HttpConditionRule getHttpConditionRulesByHttpMockRuleId(String mockRuleId) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findByHttpMockRuleId(mockRuleId);

		if (httpConditionRule.isPresent()) {

			return httpConditionRule.get();

		} else
			return null;
	}

}
