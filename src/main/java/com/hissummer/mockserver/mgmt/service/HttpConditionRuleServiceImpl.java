package com.hissummer.mockserver.mgmt.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.pojo.HttpCondition;
import com.hissummer.mockserver.mgmt.service.jpa.HttpConditionRuleMongoRepository;

@Service
public class HttpConditionRuleServiceImpl {

	@Autowired
	HttpConditionRuleMongoRepository httpConditionRuleMongoRepository;

	public HttpConditionRule addHttpConditionRule(HttpConditionRule conditionRules) {
		HttpConditionRule rule = getHttpConditionRulesByHttpMockRuleId(conditionRules.getHttpMockRuleId());
		if (rule == null)
			return httpConditionRuleMongoRepository.insert(conditionRules);
		else {
			throw ServiceException.builder().status(0).serviceMessage("conditionrule already exist.").build();

		}

	}

	public boolean updateHttpConditionRule(HttpConditionRule conditionRules) {

		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRules.getId());

		if (httpConditionRule.isPresent()) {

			httpConditionRule.get().setConditionRules(conditionRules.getConditionRules());
			httpConditionRuleMongoRepository.save(httpConditionRule.get());
			return true;
		}

		return false;

	}

	public boolean deleteWholeHttpConditionRules(HttpConditionRule conditionRule) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRule.getId());
		if (httpConditionRule.isPresent()) {

			httpConditionRuleMongoRepository.delete(httpConditionRule.get());
			return true;

		} else {
			return false;
		}
	}

	public boolean deleteHttpConditionRuleById(String id) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository.findById(id);
		if (httpConditionRule.isPresent()) {
			httpConditionRuleMongoRepository.deleteById(id);
			return true;

		} else {
			return false;
		}
	}

	public boolean deleteHttpConditionRuleByMockRuleId(String mockRuleid) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findByHttpMockRuleId(mockRuleid);
		if (httpConditionRule.isPresent()) {
			httpConditionRuleMongoRepository.deleteByHttpMockRuleId(mockRuleid);
			return true;

		} else {
			return false;
		}
	}

	public boolean deleteHttpConditionRules(HttpConditionRule conditionRules) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findById(conditionRules.getId());
		if (httpConditionRule.isPresent()) {

			conditionRules.getConditionRules()
					.forEach(condition -> httpConditionRule.get().getConditionRules().remove(condition)

			);
			reOrderTheIndexOfConditionRules(httpConditionRule.get().getConditionRules());

			httpConditionRuleMongoRepository.save(httpConditionRule.get());

			return true;

		} else
			return false;
	}

	private List<HttpCondition> reOrderTheIndexOfConditionRules(List<HttpCondition> httpConditions) {

		int[] i = { 0 };
		httpConditions.forEach(condition -> {
			condition.setOrderId(i[0]);
			i[0]++;
		});

		return httpConditions;

	}

	public HttpConditionRule getHttpConditionRulesByHttpMockRuleId(String mockRuleId) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository
				.findByHttpMockRuleId(mockRuleId);

		if (httpConditionRule.isPresent()) {

			return httpConditionRule.get();

		} else
			return null;
	}

	public HttpConditionRule getHttpConditionRulesById(String id) {
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository.findById(id);

		if (httpConditionRule.isPresent()) {

			return httpConditionRule.get();

		} else
			return null;
	}

	public void resort(HttpConditionRule localTempConditionRule) {

		int[] order = { 1 };

		localTempConditionRule.getConditionRules().stream().sorted(Comparator.comparing(HttpCondition::getOrderId))
				.forEach(conditionRule -> {

					conditionRule.setOrderId(order[0]++);

				});

	}
}
