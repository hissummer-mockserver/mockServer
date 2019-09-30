package com.hissummer.mockserver.mock.service;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.vo.HttpMockRule;

public interface MockRuleMongoRepository extends MongoRepository<HttpMockRule, String> {
	
	HttpMockRule findByHostAndUri(String host, String uri);
	
}
