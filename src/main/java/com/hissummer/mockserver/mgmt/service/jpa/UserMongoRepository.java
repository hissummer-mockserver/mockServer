package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.vo.Loginpair;

public interface UserMongoRepository extends MongoRepository<Loginpair, String> {

	Page<Loginpair> findByEnable(Boolean enable, Pageable pageable);

	List<Loginpair> findByEnable(Boolean enable);
	
	Loginpair findByUsernameAndPassword(String username, String password);

	Loginpair findByUsername(String username);
	
	Optional<Loginpair> findById(String id);


}
