package com.hissummer.mockserver.mgmt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hissummer.mockserver.mgmt.vo.MockRule;

public interface MockRuleMgmtMongoRepository extends MongoRepository<MockRule, String> {

	Page<MockRule> findAll(Pageable pageable);
	
	MockRule findByHostAndUri(String host, String uri);
	
	Page<MockRule> findByHost(String host,Pageable pageable);
	 
	Page<MockRule> findByUri( String uri,Pageable pageable);
	
	@Query("{host : {$regex : ?0}, uri:{$regex : ?1}}")
	Page<MockRule> findByHostAndUriWithRegex(String host, String uri,Pageable pageable);
	
	@Query("{host : {$regex : ?0}}")
	Page<MockRule> findByHostWithRegex(String host,Pageable pageable);
	
	@Query("{uri : {$regex : ?0}}")
	Page<MockRule> findByUriWithRegex(String uri,Pageable pageable);
	
}
