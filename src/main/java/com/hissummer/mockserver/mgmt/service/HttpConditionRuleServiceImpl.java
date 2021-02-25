package com.hissummer.mockserver.mgmt.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.service.jpa.HttpConditionRuleMongoRepository;

public class HttpConditionRuleServiceImpl {
	
	@Autowired
	HttpConditionRuleMongoRepository httpConditionRuleMongoRepository;
	
	public HttpConditionRule addHttpConditionRule(HttpConditionRule conditionRules) {
		
		return httpConditionRuleMongoRepository.save(conditionRules);
		
	}
	
	public HttpConditionRule updateHttpConditionRule(HttpConditionRule conditionRules) {
		
		Optional<HttpConditionRule> httpConditionRule = httpConditionRuleMongoRepository.findById(conditionRules.getId());
		
		if( httpConditionRule.isPresent()) {
			
			
			
		}

		
	}
}
