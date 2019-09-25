package com.hissummer.mockserver.mock.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.vo.MockRule;

public interface MockRuleMongoRepository extends MongoRepository<MockRule, String> {

	
	MockRule findByHostAndUri(String host, String uri);
	

	
}
