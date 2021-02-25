package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;

public interface HttpConditionRuleMongoRepository extends MongoRepository<HttpConditionRule, String> {

	Page<HttpConditionRule> findByEnable(Boolean enable, Pageable pageable);

	List<HttpConditionRule> findByEnable(Boolean enable);
		
	Optional<HttpConditionRule> findById(String id);

}
