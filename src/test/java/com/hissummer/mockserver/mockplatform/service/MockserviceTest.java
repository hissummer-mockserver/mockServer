package com.hissummer.mockserver.mockplatform.service;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissumemr.mockserver.SpringBootTestBase;
import com.hissummer.mockserver.mgmt.service.MockRuleManagerServiceImpl;
import com.hissummer.mockserver.mgmt.service.jpa.MockRuleMgmtMongoRepository;
import com.hissummer.mockserver.mock.service.MongoDbRunCommandServiceImpl;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Ignore
public class MockserviceTest extends SpringBootTestBase {

	@Autowired
	MongoDbRunCommandServiceImpl dataplatformServiceImpl;
	
	@Autowired
	MockRuleManagerServiceImpl mockservice;

	@Autowired
	MockRuleMgmtMongoRepository mockservice2;
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void test__getMatchedMockRulesByUrl() throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		String url = "/mockbaidu/baidutest/abcdefg/";
		String hostName = null;
		Method method = mockservice.getClass().getDeclaredMethod("__getMatchedMockRulesByHostnameAndUrl", String.class,url.getClass());
		method.setAccessible(true);
		JSONObject result =  (JSONObject) method.invoke(mockservice,hostName, url);
		if(result != null)
		log.info(result.toJSONString());
		else log.info("not found the matched rules");

	}
	
	@Test
	public void testAddMockRule() {
		
		mockservice.addMockRule(null, "/haha", "{\"mockResponse\":\"this is the haha mock Response!\"}", null,null);
		
	}
	
	@Test
	public void testAddMockRule2() {
		
		log.info(JSON.toJSONString(mockservice2.findByHost("*", PageRequest.of(0, 5))));
		
		log.info(JSON.toJSONString(mockservice2.findByUri("/newtest", PageRequest.of(0, 5))));
		
		log.info(JSON.toJSONString(mockservice2.findByHostAndUri("*","/test1/test2")));
		
	}
	

}
