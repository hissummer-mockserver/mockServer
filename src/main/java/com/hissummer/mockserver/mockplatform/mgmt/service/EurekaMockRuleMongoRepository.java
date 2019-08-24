package com.hissummer.mockserver.mockplatform.mgmt.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mockplatform.mgmt.vo.EurekaMockRule;


public interface EurekaMockRuleMongoRepository extends MongoRepository<EurekaMockRule, String> {

	
	Page<EurekaMockRule> findByEnable( Boolean enable,Pageable pageable);
	List<EurekaMockRule> findByEnable( Boolean enable);
}