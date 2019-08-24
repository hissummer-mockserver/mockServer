package com.hissummer.mockserver.mgmt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.vo.MockRule;

public interface MockRuleMongoRepository extends MongoRepository<MockRule, String> {

	Page<MockRule> findAll(Pageable pageable);
	
	Page<MockRule> findByHostAndUri(String host, String uri,Pageable pageable);
	
	Page<MockRule> findByHost(String host,Pageable pageable);
	 
	Page<MockRule> findByUri( String uri,Pageable pageable);
}
