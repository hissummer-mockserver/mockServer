package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.RequestLog;

public interface RequestLogMongoRepository extends MongoRepository<RequestLog, String> {

	Page<RequestLog> findByHittedMockRuleUri(String uri, Pageable pageable);

	Page<RequestLog> findByHittedMockRuleUriAndHittedMockRuleHostName(String uri, String hostname, Pageable pageable);

	Page<RequestLog> findByHittedMockRuleUriAndHittedMockRuleHostNameAndRequestUri(String uri, String hostname,String requestUri, Pageable pageable);

	Page<RequestLog> findAll(Pageable pageable);

	Optional<RequestLog> findById(String id);

	void deleteByCreateTimeLessThan(Date createTime);

}
