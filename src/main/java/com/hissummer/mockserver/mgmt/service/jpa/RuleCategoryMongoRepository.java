package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.RuleCategory;

public interface RuleCategoryMongoRepository extends MongoRepository<RuleCategory, String> {
	
	RuleCategory findByCategory(String category);
	Optional<RuleCategory> findById(String id);
	Page<RuleCategory>  findAll(Pageable page);

}
