package com.hissummer.mockserver.mockplatform.service;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hissumemr.mockserver.SpringBootTestBase;
import com.hissummer.mockserver.RemoveOldRequestLogsTask;
import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.service.jpa.HttpMockRuleMongoRepository;

@Ignore
public class MockRuleMongoRepositoryTest extends SpringBootTestBase {

	@Autowired
	HttpMockRuleMongoRepository mockRuleMongoRepository;

	@Autowired
	RemoveOldRequestLogsTask task;
	
	@Test
	public void test() {

		task.removeOldRequestLogs();
//		HttpMockRule rule = HttpMockRule.builder().uri("/testnewmockrule").host("*").build();
//		mockRuleMongoRepository.insert(rule);

	}

	//@Test
	public void update() {

		HttpMockRule rule = HttpMockRule.builder().uri("/testnewmockrule").host("*").build();
		mockRuleMongoRepository.insert(rule);

	}

}
