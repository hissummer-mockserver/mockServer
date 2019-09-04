package com.hissummer.mockserver.mgmt.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.service.EurekaMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.service.MockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.vo.EurekaMockRule;
import com.hissummer.mockserver.mgmt.vo.MockRule;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lihao
 *
 */

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/mock/2.0")
public class MockMgmtV2Controller {

	@Autowired
	MockRuleMongoRepository mockService;
	
	@Autowired
	EurekaMockRuleMongoRepository eurekaMockService;
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	

	@PostMapping(value = "/addRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody MockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {
			MockRule saveMockRule = mockService.insert(mockRule);
			if (saveMockRule != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {

				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;

	}

	@PostMapping(value = "/updateRule")
	public MockRuleMgmtResponseVo updateRule(@RequestBody MockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			MockRule saveMockRule = mockService.save(mockRule);
			if (saveMockRule != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {
				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/deleteRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody MockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			mockService.deleteById(mockRule.getId());

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/queryRule")
	public MockRuleMgmtResponseVo queryRules(@RequestBody JSONObject requestBody) {

		int pageNumber = requestBody.getIntValue("pageNumber") < 0 ? 0 : requestBody.getIntValue("pageNumber");
		int pageSize = requestBody.getIntValue("pageSize") <= 0 ? 50 : requestBody.getIntValue("pageSize");

		PageRequest page = PageRequest.of(pageNumber, pageSize);

		Page<MockRule> rules = null;
		
		String  uri = ".*";
		String host = ".*";
		
		if (!StringUtils.isEmpty(requestBody.getString("uri"))) {
			
			uri = requestBody.getString("uri");

		}
		if (!StringUtils.isEmpty(requestBody.getString("host"))) {
			
			
			if(requestBody.getString("host").equals("*"))
			{
				//因为做的是正则匹配查询，所以特殊的*字符转换为\*，即查询包含*字符的host值。 而不是将*认为是正则表达式。
				host="\\*";	
			}else {
				host = requestBody.getString("host");
			}
			
		}
		
		rules = mockService.findByHostAndUriWithRegex(host, uri, page);

		if (rules != null && rules.getContent().size() > 0)
			return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
		else
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

	}
	
	
	
	@PostMapping(value = "/queryEurekaRule")
	public MockRuleMgmtResponseVo queryEurekaRules(@RequestBody JSONObject requestBody) {

		int pageNumber = requestBody.getIntValue("pageNumber") < 0 ? 0 : requestBody.getIntValue("pageNumber");
		int pageSize = requestBody.getIntValue("pageSize") <= 0 ? 50 : requestBody.getIntValue("pageSize");

		PageRequest page = PageRequest.of(pageNumber, pageSize);

		Page<EurekaMockRule> rules = null;

		EurekaMockRule ruleExmple = EurekaMockRule.builder().build();
		ruleExmple.setEurekaServer( StringUtils.isEmpty(requestBody.getString("eurekaServer")) ?null : requestBody.getString("eurekaServer"));
		ruleExmple.setServiceName(StringUtils.isEmpty(requestBody.getString("serviceName"))?null:requestBody.getString("serviceName"));
		Example<EurekaMockRule> example = Example.of(ruleExmple);
		rules = eurekaMockService.findAll(example,page);


		if (rules != null && rules.getContent() != null && rules.getContent().size() > 0)
			return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
		else
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

	}	
	
	
	
	
	@PostMapping(value = "/addEurekaRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {
			EurekaMockRule saveMockRule = eurekaMockService.insert(mockRule);
			if (saveMockRule != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {

				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;

	}

	@PostMapping(value = "/updateEurekaRule")
	public MockRuleMgmtResponseVo updateRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			EurekaMockRule saveMockRule = eurekaMockService.save(mockRule);
			if (saveMockRule != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {
				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	
	@PostMapping(value = "/deleteEurekaRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			eurekaMockService.deleteById(mockRule.getId());

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

}