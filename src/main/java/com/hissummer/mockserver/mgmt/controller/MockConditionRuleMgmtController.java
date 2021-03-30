package com.hissummer.mockserver.mgmt.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.pojo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.service.HttpConditionRuleServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lihao 2021年3月22日
 * 
 *         mock http condition rule management api.
 * 
 */
@Slf4j
@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/xxxxhissummerxxxx/api")
public class MockConditionRuleMgmtController {

	@Autowired
	HttpConditionRuleServiceImpl httpConditionRuleServiceImpl;

	@RequestMapping(value = "/httpConditionRule/{id}", method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.DELETE })
	public MockRuleMgmtResponseVo operateConditionRuleById(HttpServletRequest request, @PathVariable("id") String id,
			@RequestBody HttpConditionRule conditionRule) {

		MockRuleMgmtResponseVo result = null;
		HttpConditionRule localTempConditionRule = null;
		boolean operationResult = true;
		try {

			switch (request.getMethod())

			{
			case "GET":
				if (id == null)
					throw ServiceException.builder().status(0).serviceMessage("id is not exist.").build();
				else {
					localTempConditionRule = httpConditionRuleServiceImpl.getHttpConditionRulesById(id);
				}
				break;
			case "POST":
				if (id == null || !id.equals(conditionRule.getId()))
					throw ServiceException.builder().status(0).serviceMessage("id problems,please check your input id.")
							.build();
				else {
					if (!httpConditionRuleServiceImpl.updateHttpConditionRule(conditionRule))
						operationResult = false;
				}
				break;

			case "DELETE":
				if (id == null || !id.equals(conditionRule.getId()))
					throw ServiceException.builder().status(0).serviceMessage("id is not exist.").build();
				else {
					if (!httpConditionRuleServiceImpl.deleteHttpConditionRuleById(id))
						operationResult = false;
				}
				break;
			default:
				break;

			}

			result = MockRuleMgmtResponseVo.builder().status(0).success(operationResult).message("")
					.data(localTempConditionRule).build();

		} catch (ServiceException e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
		}
		return result;

	}

	@RequestMapping(value = "/httpConditionRule/mockRuleId-{id}", method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.DELETE })
	public MockRuleMgmtResponseVo operateConditionRuleByMockruleId(HttpServletRequest request,
			@PathVariable("id") String mockRuleId, @RequestBody HttpConditionRule conditionRule) {

		MockRuleMgmtResponseVo result = null;
		HttpConditionRule localTempConditionRule = null;
		boolean operationResult = true;
		try {

			switch (request.getMethod())

			{
			case "GET":
				if (mockRuleId == null)
					throw ServiceException.builder().status(0).serviceMessage("mockRuleId is not exist.").build();
				else {
					localTempConditionRule = httpConditionRuleServiceImpl
							.getHttpConditionRulesByHttpMockRuleId(mockRuleId);
				}
				break;
			case "POST":
				if (mockRuleId == null || !mockRuleId.equals(conditionRule.getHttpMockRuleId()))
					throw ServiceException.builder().status(0)
							.serviceMessage("mockRuleId problems,please check your input id.").build();
				else {
					if (!httpConditionRuleServiceImpl.updateHttpConditionRule(conditionRule))
						operationResult = false;
				}
				break;

			case "DELETE":
				if (mockRuleId == null || !mockRuleId.equals(conditionRule.getHttpMockRuleId()))
					throw ServiceException.builder().status(0).serviceMessage("mockRuleId is not exist.").build();
				else {
					if (!httpConditionRuleServiceImpl.deleteHttpConditionRuleByMockRuleId(mockRuleId))
						operationResult = false;
				}
				break;
			default:
				break;

			}

			result = MockRuleMgmtResponseVo.builder().status(0).success(operationResult).message("")
					.data(localTempConditionRule).build();

		} catch (ServiceException e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
		}
		return result;

	}

	@RequestMapping(value = "/httpConditionRule", method = { RequestMethod.PUT })
	public MockRuleMgmtResponseVo addConditionRule(@RequestBody HttpConditionRule conditionRule) {
		MockRuleMgmtResponseVo result = null;
		HttpConditionRule localTempConditionRule = null;
		try {
			localTempConditionRule = httpConditionRuleServiceImpl.addHttpConditionRule(conditionRule);
			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("").data(localTempConditionRule)
					.build();
		} catch (ServiceException e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
		}
		return result;

	}

}
