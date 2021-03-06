package com.hissummer.mockserver.mock.service.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;

public interface MockRuleMongoRepository extends MongoRepository<HttpMockRule, String> {

	HttpMockRule findByHostAndUri(String host, String uri);

}
