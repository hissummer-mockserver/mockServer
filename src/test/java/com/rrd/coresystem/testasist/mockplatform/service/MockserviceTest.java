package com.rrd.coresystem.testasist.mockplatform.service;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.dataplatform.service.DataplatformServiceImpl;
import com.hissummer.mockserver.mockplatform.service.MockRuleMongoRepository;
import com.hissummer.mockserver.mockplatform.service.MockserviceImpl;
import com.rrd.coresystem.testasist.SpringBootTestBase;

import lombok.extern.slf4j.Slf4j;



@Slf4j
public class MockserviceTest extends SpringBootTestBase {

	@Autowired
	DataplatformServiceImpl dataplatformServiceImpl;
	
	@Autowired
	MockserviceImpl mockservice;

	@Autowired
	MockRuleMongoRepository mockservice2;
	
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
		
		log.info(JSON.toJSONString(mockservice2.findByHostAndUri("*","/newtest", PageRequest.of(0, 5))));
		
	}
	

}
