package com.hissummer.mockserver.mgmt.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.hissummer.mockserver.mgmt.service.UserService;
import com.hissummer.mockserver.mgmt.service.jpa.EurekaMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.service.jpa.MockRuleMgmtMongoRepository;
import com.hissummer.mockserver.mgmt.vo.EurekaMockRule;
import com.hissummer.mockserver.mgmt.vo.HttpMockRule;
import com.hissummer.mockserver.mgmt.vo.Loginpair;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockserviceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lihao
 *
 */

@Slf4j
@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/mock/2.0")
public class MockMgmtV2Controller {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	@Autowired
	MockRuleMgmtMongoRepository mockRuleMgmtMongoRepository;

	@Autowired
	EurekaMockRuleMongoRepository eurekaMockRuleRepository;

	@Autowired
	MockserviceImpl mockservice;

	@Autowired
	MockRuleManagerServiceImpl mockRuleManagerService;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	EurekaMockRuleServiceImpl eurekaMockRuleServiceImpl;

	@Autowired
	UserService userService;

	@PostMapping(value = "/addRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {

			if (mockRuleMgmtMongoRepository.findByHostAndUri(mockRule.getHost(), mockRule.getUri()) != null) {
				result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("mockrule already exist.")
						.build();
				return result;
			}

			HttpMockRule saveMockRule = mockRuleMgmtMongoRepository.insert(mockRule);

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Add success.")
					.data(saveMockRule).build();

		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("Add failed: " + e.getMessage())
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

			HttpMockRule saveMockRule = mockRuleMgmtMongoRepository.save(mockRule);

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Update success.")
					.data(saveMockRule).build();

		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("Update faild:" + e.getMessage())
					.build();
		}

		return result;
	}

	@PostMapping(value = "/deleteRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {

			mockRuleMgmtMongoRepository.deleteById(mockRule.getId());

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("Delete failed: "+e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/testRule")
	public MockRuleMgmtResponseVo testRule(@RequestBody HttpMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {

			String testResponse = mockservice.testRule(mockRule);

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
				// 因为做的是正则匹配查询，所以特殊的*字符转换为\*，即查询包含*字符的host值。
				host = "\\*";
			} else {
				host = requestBody.getString("host");
			}

		}
		String category = requestBody.getString("category");

		if (StringUtils.isEmpty(category)) {
			rules = mockRuleMgmtMongoRepository.findByHostRegexpAndUriRegexp(host, uri, page);
		} else {
			rules = mockRuleMgmtMongoRepository.findByHostRegexpAndUriRegexpAndCategory(host, uri, category, page);
		}

		if (rules != null && !rules.getContent().isEmpty())
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

		if (rules != null && !rules.getContent().isEmpty())
			return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
		else
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

	}

	@PostMapping(value = "/addEurekaRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;

		try {
			EurekaMockRule saveMockRule = eurekaMockRuleRepository.insert(mockRule);

			return MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.").data(saveMockRule)
					.build();

		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;

	}

	@PostMapping(value = "/updateEurekaRule")
	public MockRuleMgmtResponseVo updateRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			EurekaMockRule saveMockRule = eurekaMockRuleRepository.save(mockRule);

			eurekaMockRuleServiceImpl.unRegisterApp(mockRule);

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("save success.")
					.data(saveMockRule).build();

		} catch (Exception e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/deleteEurekaRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody EurekaMockRule mockRule) {

		MockRuleMgmtResponseVo result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("The id could not be empty.")
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

	@PostMapping(value = "/login")
	public MockRuleMgmtResponseVo login(@RequestBody JSONObject requestBody, HttpServletResponse response) {

		String username = requestBody.getString(USERNAME);
		String password = requestBody.getString(PASSWORD);

		boolean loginStatus = userService.login(username, password);
		Loginpair user = userService.finduserByuserName(username);
		if (user != null) {
			Cookie mu = new Cookie("mu", user.getId());
			mu.setHttpOnly(true);
			response.addCookie(mu);
		}
		return MockRuleMgmtResponseVo.builder().status(0).success(loginStatus).build();
	}

	@PostMapping(value = "/logout")
	public MockRuleMgmtResponseVo logout(@RequestBody JSONObject requestBody, HttpServletRequest request) {

		String username = requestBody.getString(USERNAME);

		return MockRuleMgmtResponseVo.builder().status(0).success(userService.logout(username, request.getCookies()))
				.build();
	}

	@PostMapping(value = "/createUser")
	public MockRuleMgmtResponseVo createUser(@RequestBody JSONObject requestBody) {

		String username = requestBody.getString(USERNAME);
		String password = requestBody.getString(PASSWORD);

		return MockRuleMgmtResponseVo.builder().status(0).success(userService.createUser(username, password)).build();
	}

	@PostMapping(value = "/rePassword")
	public MockRuleMgmtResponseVo rePasswordUser(@RequestBody JSONObject requestBody, HttpServletRequest request) {

		String username = requestBody.getString(USERNAME);
		String password = requestBody.getString(PASSWORD);
		String newpassword = requestBody.getString("newpassword");

		return MockRuleMgmtResponseVo.builder().status(0)
				.success(userService.rePasswordUser(username, password, newpassword, request.getCookies())).build();
	}

	@PostMapping(value = "/delUser")
	public MockRuleMgmtResponseVo delUser(@RequestBody JSONObject requestBody, HttpServletRequest request) {

		String username = requestBody.getString(USERNAME);
		String password = requestBody.getString(PASSWORD);

		return MockRuleMgmtResponseVo.builder().status(0)
				.success(userService.delUser(username, password, request.getCookies())).build();
	}

	@PostMapping(value = "/isLogin")
	public MockRuleMgmtResponseVo isUserLogin() {
		return MockRuleMgmtResponseVo.builder().status(0).success(true).build();
	}

}