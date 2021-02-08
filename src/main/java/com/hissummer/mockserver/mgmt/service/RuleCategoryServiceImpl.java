package com.hissummer.mockserver.mgmt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.hissummer.mockserver.mgmt.entity.RuleCategory;
import com.hissummer.mockserver.mgmt.service.jpa.RuleCategoryMongoRepository;

public class RuleCategoryServiceImpl {

	@Autowired
	RuleCategoryMongoRepository ruleCategoryMongoRepository;

	@Transactional
	public RuleCategory addCategory(RuleCategory category) {

		return ruleCategoryMongoRepository.insert(category);

	}

}
