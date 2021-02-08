package com.hissummer.mockserver.mgmt.service.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hissummer.mockserver.mgmt.entity.User;

public interface UserMongoRepository extends MongoRepository<User, String> {

	Page<User> findByEnable(Boolean enable, Pageable pageable);

	List<User> findByEnable(Boolean enable);

	User findByUsernameAndPassword(String username, String password);

	User findByUsername(String username);

	Optional<User> findById(String id);

}
