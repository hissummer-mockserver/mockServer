package com.hissummer.mockserver.mockplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mockplatform.EurekaMockRule;
import com.hissummer.mockserver.mockplatform.MockRule;
import com.hissummer.mockserver.mockplatform.NoMockResponseBody;
import com.hissummer.mockserver.mockplatform.service.EurekaMockRuleMongoRepository;
import com.hissummer.mockserver.mockplatform.service.MockRuleMongoRepository;

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
	
	

	@PostMapping(value = "/addRule")
	public NoMockResponseBody addRule(@RequestBody MockRule mockRule) {

		NoMockResponseBody result = null;

		try {
			MockRule saveMockRule = mockService.insert(mockRule);
			if (saveMockRule != null) {
				result = NoMockResponseBody.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {

				result = NoMockResponseBody.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;

	}

	@PostMapping(value = "/updateRule")
	public NoMockResponseBody updateRule(@RequestBody MockRule mockRule) {

		NoMockResponseBody result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = NoMockResponseBody.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			MockRule saveMockRule = mockService.save(mockRule);
			if (saveMockRule != null) {
				result = NoMockResponseBody.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {
				result = NoMockResponseBody.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/deleteRule")
	public NoMockResponseBody deleteRule(@RequestBody MockRule mockRule) {

		NoMockResponseBody result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = NoMockResponseBody.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			mockService.deleteById(mockRule.getId());

			result = NoMockResponseBody.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/queryRule")
	public NoMockResponseBody queryRules(@RequestBody JSONObject requestBody) {

		int pageNumber = requestBody.getIntValue("pageNumber") < 0 ? 0 : requestBody.getIntValue("pageNumber");
		int pageSize = requestBody.getIntValue("pageSize") <= 0 ? 50 : requestBody.getIntValue("pageSize");

		// MockRule searchRule =
		// MockRule.builder().uri(requestBody.getString("uri")).host(requestBody.getString("hostName")).workMode(null).protocol(null).build();

		// Example<MockRule> example = Example.of(searchRule);
		PageRequest page = PageRequest.of(pageNumber, pageSize);

		Page<MockRule> rules = null;

		if (StringUtils.isEmpty(requestBody.getString("uri")) && StringUtils.isEmpty(requestBody.getString("host"))) {
			rules = mockService.findAll(page);
		} else if (!StringUtils.isEmpty(requestBody.getString("uri"))
				&& !StringUtils.isEmpty(requestBody.getString("host"))) {
			rules = mockService.findByHostAndUri(requestBody.getString("host"), requestBody.getString("uri"), page);
		}

		else if (StringUtils.isEmpty(requestBody.getString("uri"))) {
			rules = mockService.findByHost(requestBody.getString("host"), page);
		}

		else if (StringUtils.isEmpty(requestBody.getString("host"))) {
			rules = mockService.findByUri(requestBody.getString("uri"), page);
		}

		// (requestBody.getString("hostName"), requestBody.getString("uri"),
		// PageRequest.of(pageNumber, pageSize));

		if (rules != null && rules.getContent() != null && rules.getContent().size() > 0)
			return NoMockResponseBody.builder().status(0).success(true).data(rules).build();
		else
			return NoMockResponseBody.builder().status(0).success(false).message("No Rules found.").build();

	}
	
	@PostMapping(value = "/addEurekaRule")
	public NoMockResponseBody addRule(@RequestBody EurekaMockRule mockRule) {

		NoMockResponseBody result = null;

		try {
			EurekaMockRule saveMockRule = eurekaMockService.insert(mockRule);
			if (saveMockRule != null) {
				result = NoMockResponseBody.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {

				result = NoMockResponseBody.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;

	}

	@PostMapping(value = "/updateEurekaRule")
	public NoMockResponseBody updateRule(@RequestBody EurekaMockRule mockRule) {

		NoMockResponseBody result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = NoMockResponseBody.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			EurekaMockRule saveMockRule = eurekaMockService.save(mockRule);
			if (saveMockRule != null) {
				result = NoMockResponseBody.builder().status(0).success(true).message("save success.")
						.data(saveMockRule).build();
			} else {
				result = NoMockResponseBody.builder().status(0).success(false).message("save faild.").build();
			}
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

	@PostMapping(value = "/deleteEurekaRule")
	public NoMockResponseBody deleteRule(@RequestBody EurekaMockRule mockRule) {

		NoMockResponseBody result = null;
		if (mockRule.getId() == null || mockRule.getId().equals("")) {
			result = NoMockResponseBody.builder().status(0).success(false).message("The id could not be empty.")
					.build();
		}
		try {
			eurekaMockService.deleteById(mockRule.getId());

			result = NoMockResponseBody.builder().status(0).success(true).message("Delete success.").build();
		} catch (Exception e) {

			result = NoMockResponseBody.builder().status(0).success(false).message(e.getMessage()).build();
		}

		return result;
	}

}