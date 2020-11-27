package com.hissummer.mockserver.mgmt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.service.EurekaMockRuleServiceImpl;
import com.hissummer.mockserver.mgmt.service.MockRuleManagerServiceImpl;
import com.hissummer.mockserver.mgmt.service.jpa.EurekaMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.service.jpa.MockRuleMgmtMongoRepository;
import com.hissummer.mockserver.mgmt.vo.EurekaMockRule;
import com.hissummer.mockserver.mgmt.vo.HttpMockRule;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockserviceImpl;

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
	MockRuleMgmtMongoRepository mockRuleRepository;

	@Autowired
	EurekaMockRuleMongoRepository eurekaMockRuleRepository;

	@Autowired
	MockRuleManagerServiceImpl mockRuleManagerService;

	@Autowired
	MockserviceImpl mockService;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	EurekaMockRuleServiceImpl eurekaMockRuleServiceImpl;

	@PostMapping(value = "/addRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {

			if (mockRuleRepository.findByHostAndUri(mockRule.getHost(), mockRule.getUri()) != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("mockrule already exist.")
						.build();
				return result;
			}

			HttpMockRule saveMockRule = mockRuleRepository.insert(mockRule);

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
					.data(saveMockRule).build();

		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save failed: " + e.getMessage())
					.build();
		}

		return result;

	}

	@PostMapping(value = "/updateRule")
	public MockRuleMgmtResponseVo updateRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			HttpMockRule saveMockRule = mockRuleRepository.save(mockRule);
			if (saveMockRule != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("update success.")
						.data(saveMockRule).build();
			} else {
				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("save faild:" + e.getMessage())
					.build();
		}

		return result;
	}

	@PostMapping(value = "/deleteRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			mockRuleRepository.deleteById(mockRule.getId());

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("delete success.").build();
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/testRule")
	public MockRuleMgmtResponseVo testRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {

			String testResponse = mockService.testRule(mockRule);

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message(testResponse).build();
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

		Page<HttpMockRule> rules = null;

		String uri = ".*";
		String host = ".*";

		if (!StringUtils.isEmpty(requestBody.getString("uri"))) {

			uri = requestBody.getString("uri");

		}
		if (!StringUtils.isEmpty(requestBody.getString("host"))) {

			if (requestBody.getString("host").equals("*")) {
				// 因为做的是正则匹配查询，所以特殊的*字符转换为\*，即查询包含*字符的host值。 而不是将*认为是正则表达式。
				host = "\\*";
			} else {
				host = requestBody.getString("host");
			}

		}

		rules = mockRuleRepository.findByHostAndUriWithRegex(host, uri, page);

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
		ruleExmple.setEurekaServer(StringUtils.isEmpty(requestBody.getString("eurekaServer")) ? null
				: requestBody.getString("eurekaServer"));
		ruleExmple.setServiceName(StringUtils.isEmpty(requestBody.getString("serviceName")) ? null
				: requestBody.getString("serviceName"));
		Example<EurekaMockRule> example = Example.of(ruleExmple);
		rules = eurekaMockRuleRepository.findAll(example, page);

		if (rules != null && rules.getContent() != null && rules.getContent().size() > 0)
			return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
		else
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

	}

	@PostMapping(value = "/addEurekaRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {
			EurekaMockRule saveMockRule = eurekaMockRuleRepository.insert(mockRule);
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
			EurekaMockRule saveMockRule = eurekaMockRuleRepository.save(mockRule);
			if (saveMockRule != null) {

				eurekaMockRuleServiceImpl.unRegisterApp(mockRule);

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
			eurekaMockRuleRepository.deleteById(mockRule.getId());

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

}