package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;

public interface HttpMockRuleMongoRepository extends MongoRepository<HttpMockRule, String> {

	Page<HttpMockRule> findAll(Pageable pageable);

	HttpMockRule findByHostAndUri(String host, String uri);

	Page<HttpMockRule> findByHost(String host, Pageable pageable);

	Page<HttpMockRule> findByUri(String uri, Pageable pageable);

	@Query("{host : {$regex : ?0}, uri:{$regex : ?1}}")
	Page<HttpMockRule> findByHostRegexpAndUriRegexp(String host, String uri, Pageable pageable);

	@Query("{host : {$regex : ?0}, uri:{$regex : ?1}, category: ?2}")
	Page<HttpMockRule> findByHostRegexpAndUriRegexpAndCategory(String host, String uri, String category,
			Pageable pageable);

	@Query("{host : {$regex : ?0}}")
	Page<HttpMockRule> findByHostWithRegex(String host, Pageable pageable);

	@Query("{uri : {$regex : ?0}}")
	Page<HttpMockRule> findByUriWithRegex(String uri, Pageable pageable);

	Long countByCategory(String category);

	List<HttpMockRule> findByCategory(String category);

}
