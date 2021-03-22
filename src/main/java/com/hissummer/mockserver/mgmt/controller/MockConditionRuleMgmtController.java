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

	@RequestMapping(value = "/addRule/{id}", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
			RequestMethod.DELETE })
	public MockRuleMgmtResponseVo addConditionRule(HttpServletRequest request, @PathVariable("") String id,
			@RequestBody HttpConditionRule conditionRule) {

		MockRuleMgmtResponseVo result = null;
		HttpConditionRule localTempConditionRule = null;
		try {

			switch (request.getMethod())

			{
			case "GET":
				if (id == null)
					throw ServiceException.builder().status(0).serviceMessage("id is not exist.").build();
				else {
					localTempConditionRule = httpConditionRuleServiceImpl.getHttpConditionRulesByHttpMockRuleId(id);
				}
				break;
			case "POST":
				if (id == null || !id.equals(conditionRule.getId()))
					throw ServiceException.builder().status(0).serviceMessage("id problems,please check your input id.")
							.build();
				else {
					httpConditionRuleServiceImpl.updateHttpConditionRule(conditionRule);
				}
				break;
			case "PUT":
				break;
			case "DELETE":
				break;
			default:
				break;

			}

			result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("success.").data(null).build();

		} catch (ServiceException e) {

			result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
		}
		return result;

	}

}
