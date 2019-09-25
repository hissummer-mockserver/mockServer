package com.hissummer.mockserver.mockplatform.service;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hissumemr.mockserver.SpringBootTestBase;
import com.hissummer.mockserver.mgmt.service.MockRuleMgmtMongoRepository;
import com.hissummer.mockserver.mgmt.vo.MockRule;

@Ignore
public class MockRuleMongoRepositoryTest extends SpringBootTestBase{
	
	@Autowired
	MockRuleMgmtMongoRepository mockRuleMongoRepository;

	@Test
	public void test() {
		

		MockRule rule = MockRule.builder().uri("/testnewmockrule").host("*").build();
		mockRuleMongoRepository.insert(rule);
		
	}

	
	@Test
	public void update() {
		
		MockRule rule = MockRule.builder().uri("/testnewmockrule").host("*").build();
		mockRuleMongoRepository.insert(rule);
		
	}
	
}
