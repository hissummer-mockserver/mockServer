package com.hissummer.mockserver.mgmt.service.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.vo.RuleCategory;


public interface RuleCategoryMongoRepository extends MongoRepository<RuleCategory, String>{
	

}
