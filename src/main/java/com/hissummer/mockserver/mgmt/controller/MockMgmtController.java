package com.hissummer.mockserver.mgmt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.service.MockRuleManagerServiceImpl;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockserviceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * 第一次启动数据库后,我们需要设定如下索引. mongodb最低版本中3.2 版本,否则不支持find命令。
 * 如下是ubuntu安装最新版本mongodb的教程,https://www.anintegratedworld.com/how-to-install-or-upgrade-mongodb-on-ubuntu/
 * 
 * > db.mockrules.createIndex({host:1,uri:1},{unique:true}); {
 * "createdCollectionAutomatically" : false, "numIndexesBefore" : 1,
 * "numIndexesAfter" : 2, "ok" : 1 } > > db.mockrules.getIndexes(); [ { "v" : 1,
 * "key" : { "_id" : 1 }, "name" : "_id_", "ns" : "testplatform.mockrules" }, {
 * "v" : 1, "unique" : true, "key" : { "host" : 1, "uri" : 1 }, "name" :
 * "host_1_uri_1", "ns" : "testplatform.mockrules" } ]
 * 
 * @author lihao
 *
 */

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/mock/old/")
@Deprecated
public class MockMgmtController {

	@Autowired
	private MockRuleManagerServiceImpl mockRuleservice;

	@PostMapping(value = "/addRule")
	public MockRuleMgmtResponseVo addRule(@RequestBody JSONObject requestBody) {

		try {
			MockRuleMgmtResponseVo result = MockRuleMgmtResponseVo.builder().status(0)
					.success(mockRuleservice.addMockRule(requestBody.getString("hostName"), requestBody.getString("uri"),
							requestBody.getString("mockResponse"), null,requestBody.getString("workMode")))
					.build();
			return result;
		} catch (Exception e) {

			return MockRuleMgmtResponseVo.builder().status(-1).message(e.getMessage()).success(false).build();

		}
	}

	@PostMapping(value = "/updateRule")
	public MockRuleMgmtResponseVo updateRule(@RequestBody JSONObject requestBody) {

		try {
			MockRuleMgmtResponseVo result = MockRuleMgmtResponseVo.builder().status(0)
					.success(mockRuleservice.updateMockRule(requestBody.getString("id"), requestBody.getString("hostName"),
							requestBody.getString("uri"), requestBody.getString("mockResponse"), null, requestBody.getString("workMode"),null))
					.build();
			return result;
		} catch (Exception e) {

			return MockRuleMgmtResponseVo.builder().status(-1).message(e.getMessage()).success(false).build();

		}
	}

	@PostMapping(value = "/deleteRule")
	public MockRuleMgmtResponseVo deleteRule(@RequestBody JSONObject requestBody) {

		try {
			MockRuleMgmtResponseVo result = MockRuleMgmtResponseVo.builder().status(0)
					.success(mockRuleservice.deleteMockRule(requestBody.getString("id"))).build();
			return result;
		} catch (Exception e) {

			return MockRuleMgmtResponseVo.builder().status(-1).message(e.getMessage()).success(false).build();

		}
	}

	@PostMapping(value = "/queryRule")
	public MockRuleMgmtResponseVo queryRules(@RequestBody JSONObject requestBody) {

		int pageNumber = requestBody.getIntValue("pageNumber") == 0 ? 1 : requestBody.getIntValue("pageNumber");
		int pageSize = requestBody.getIntValue("pageSize") == 0 ? 50 : requestBody.getIntValue("pageSize");
		;
		JSONArray rules = mockRuleservice.queryMockRules(requestBody.getString("hostName"), requestBody.getString("uri"),
				pageNumber, pageSize);
		if (rules != null)
			return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
		else
			return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

	}

}