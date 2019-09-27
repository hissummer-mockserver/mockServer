package com.hissummer.mockserver.mgmt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hissummer.mockserver.mgmt.vo.HttpMockRule;

public interface MockRuleMgmtMongoRepository extends MongoRepository<HttpMockRule, String> {

	Page<HttpMockRule> findAll(Pageable pageable);
	
	HttpMockRule findByHostAndUri(String host, String uri);
	
	Page<HttpMockRule> findByHost(String host,Pageable pageable);
	 
	Page<HttpMockRule> findByUri( String uri,Pageable pageable);
	
	@Query("{host : {$regex : ?0}, uri:{$regex : ?1}}")
	Page<HttpMockRule> findByHostAndUriWithRegex(String host, String uri,Pageable pageable);
	
	@Query("{host : {$regex : ?0}}")
	Page<HttpMockRule> findByHostWithRegex(String host,Pageable pageable);
	
	@Query("{uri : {$regex : ?0}}")
	Page<HttpMockRule> findByUriWithRegex(String uri,Pageable pageable);
	
}
