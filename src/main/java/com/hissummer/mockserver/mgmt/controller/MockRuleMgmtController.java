package com.hissummer.mockserver.mgmt.controller;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hissummer.mockserver.mgmt.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.entity.EurekaMockRule;
import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.entity.RequestLog;
import com.hissummer.mockserver.mgmt.entity.RuleCategory;
import com.hissummer.mockserver.mgmt.entity.User;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.pojo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.service.jpa.EurekaMockRuleMongoRepository;
//import com.hissummer.mockserver.mgmt.service.jpa.HttpMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.service.jpa.RequestLogMongoRepository;
import com.hissummer.mockserver.mock.service.MockServiceImpl;


import lombok.extern.slf4j.Slf4j;

/**
 * @author lihao
 */

@Slf4j
@CrossOrigin(origins = "*", allowCredentials = "true")
@RestController
@RequestMapping("/xxxxhissummerxxxx/api")
public class MockRuleMgmtController {

    private static final String USERNAMEPARAM = "username";
    private static final String PASSWORDPARAM = "password";

//	@Autowired
//	HttpMockRuleMongoRepository mockRuleMgmtMongoRepository;

    @Autowired
    RequestLogMongoRepository requestLogMongoRepository;

    @Autowired
    RequestLogService RequestLogService;

    @Autowired
    EurekaMockRuleMongoRepository eurekaMockRuleRepository;

    @Autowired
    RuleCategoryServiceImpl ruleCategoryServiceImpl;

    @Autowired
    MockServiceImpl mockService;

//	@Autowired
//	MongoTemplate mongoTemplate;

    @Autowired
    EurekaMockRuleServiceImpl eurekaMockRuleServiceImpl;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    HttpMockRuleServiceImpl httpMockRuleServiceImpl;

     private static final   String PAGE_SIZE ="pageSize";

    private static final  String PAGE_NUMBER ="pageNumber";

    @PostMapping(value = "/addRule")
    public MockRuleMgmtResponseVo addRule(@RequestBody HttpMockRule mockRule) {

        MockRuleMgmtResponseVo result;

        try {

            ruleCategoryServiceImpl.addCategory(RuleCategory.builder().category(mockRule.getCategory())
                    .description(mockRule.getCategory()).build());
            HttpMockRule savedMockRule = httpMockRuleServiceImpl.addMockRule(mockRule);

            result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Add success.")
                    .data(savedMockRule).build();

        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
        }
        return result;

    }

    @PostMapping(value = "/updateRule")
    public MockRuleMgmtResponseVo updateRule(@RequestBody HttpMockRule mockRule) {

        MockRuleMgmtResponseVo result;
        try {
            ruleCategoryServiceImpl.addCategory(RuleCategory.builder().category(mockRule.getCategory())
                    .description(mockRule.getCategory()).build());
            HttpMockRule savedMockRule = httpMockRuleServiceImpl.updateMockRule(mockRule);
            result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Update success.")
                    .data(savedMockRule).build();

        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
        }

        return result;
    }

    @PostMapping(value = "/deleteRule")
    public MockRuleMgmtResponseVo deleteRule(@RequestBody HttpMockRule mockRule) {

        MockRuleMgmtResponseVo result;

        try {
            httpMockRuleServiceImpl.deleteMockRule(mockRule);
            result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete Rule success.").build();
        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();
        }
        return result;

    }

    @PostMapping(value = "/testRule")
    public MockRuleMgmtResponseVo testRule(@RequestBody HttpMockRule mockRule) {

        MockRuleMgmtResponseVo result;

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

        int pageNumber = Math.max(requestBody.getIntValue(PAGE_NUMBER), 0);
        int pageSize = requestBody.getIntValue(PAGE_SIZE) <= 0 ? 50 : requestBody.getIntValue(PAGE_SIZE);
        String uri = requestBody.getString("uri");
        String host = requestBody.getString("host");
        String category = requestBody.getString("category");
        Page<HttpMockRule> rules = null;
        try {
            rules = httpMockRuleServiceImpl.queryMockRules(host, uri, category, pageNumber, pageSize);
        } catch (ServiceException e) {
            return MockRuleMgmtResponseVo.builder().status(0).success(false).message(e.getServiceMessage()).build();

        }
        if (rules != null && !rules.getContent().isEmpty())
            return MockRuleMgmtResponseVo.builder().status(0).success(true).data(rules).build();
        else
            return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No Rules found.").build();

    }

    @PostMapping(value = "/addCategory")
    public MockRuleMgmtResponseVo addCategory(@RequestBody RuleCategory ruleCategory) {

        MockRuleMgmtResponseVo result = null;

        try {

            return MockRuleMgmtResponseVo.builder().status(0).success(true)
                    .data(ruleCategoryServiceImpl.addCategory(ruleCategory)).message("Add category successfully.")
                    .build();

        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false)
                    .message("Add failed: " + e.getServiceMessage()).build();
        }

        return result;

    }

    @PostMapping(value = "/updateCategory")
    public MockRuleMgmtResponseVo updateCategory(@RequestBody RuleCategory ruleCategory) {

        MockRuleMgmtResponseVo result = null;

        try {

            RuleCategory savedRuleCategory = ruleCategoryServiceImpl.updateCategory(ruleCategory);

            result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Update success.")
                    .data(savedRuleCategory).build();

        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false)
                    .message("Update faild:" + e.getServiceMessage()).build();
        } catch (Exception e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false).message("Update faild:" + e.getMessage())
                    .build();
        }
        return result;
    }

    @PostMapping(value = "/deleteCategory")
    public MockRuleMgmtResponseVo deleteCategory(@RequestBody RuleCategory ruleCategory) {

        MockRuleMgmtResponseVo result = null;
        try {
            ruleCategoryServiceImpl.deleteCategory(ruleCategory);

            result = MockRuleMgmtResponseVo.builder().status(0).success(true).message("Delete success.").build();
        } catch (ServiceException e) {

            result = MockRuleMgmtResponseVo.builder().status(0).success(false)
                    .message("Delete failed: " + e.getServiceMessage()).build();
        }

        return result;
    }

    @PostMapping(value = "/queryCategory")
    public MockRuleMgmtResponseVo queryCategories(@RequestBody JSONObject requestBody) {

        List<RuleCategory> categories = null;

        if (!requestBody.containsKey(PAGE_NUMBER) || !requestBody.containsKey(PAGE_SIZE)) {

            categories = ruleCategoryServiceImpl.queryCategories();

            return MockRuleMgmtResponseVo.builder().status(0).success(true).data(categories).build();

        }

        int pageNumber = Math.max(requestBody.getIntValue(PAGE_NUMBER), 0);
        int pageSize = requestBody.getIntValue(PAGE_SIZE) <= 0 ? 50 : requestBody.getIntValue(PAGE_SIZE);

        Page<RuleCategory> categoryWithPage = ruleCategoryServiceImpl.queryCategories(pageSize, pageNumber);

        return MockRuleMgmtResponseVo.builder().status(0).success(true).data(categoryWithPage.getContent()).build();

    }

    @PostMapping(value = "/queryRequestLog")
    public MockRuleMgmtResponseVo queryRequestLog(@RequestBody JSONObject requestBody) {

        int pageNumber = Math.max(requestBody.getIntValue(PAGE_NUMBER), 0);
        int pageSize = requestBody.getIntValue(PAGE_SIZE) <= 0 ? 20 : requestBody.getIntValue(PAGE_SIZE);

        PageRequest page = PageRequest.of(pageNumber, pageSize, Sort.by("createTime").descending());

        Page<RequestLog> requestLogs = null;

        String uri = "/";
        String hostname = "*";
        String requestUri = "";
        String requestKeyWord = "";
        long startDate=-1L;
        long endDate=-1L;

        if (!StringUtils.isEmpty(requestBody.getString("uri"))) {
            uri = requestBody.getString("uri");
        }

        if (!StringUtils.isEmpty(requestBody.getString("hostname"))) {
            hostname = requestBody.getString("hostname");
        }

        if (!StringUtils.isEmpty(requestBody.getString("requestUri"))) {
            requestUri = requestBody.getString("requestUri");
        }
        if (!StringUtils.isEmpty(requestBody.getString("requestBodyKeyword"))) {
            requestKeyWord = requestBody.getString("requestBodyKeyword");
        }
        if (!StringUtils.isEmpty(requestBody.getLong("startDate"))) {
            startDate = Long.parseLong(requestBody.getString("startDate"));
        }
        if (!StringUtils.isEmpty(requestBody.getLong("endDate"))) {
            endDate = Long.parseLong(requestBody.getString("endDate"));
        }
        requestLogs = RequestLogService.searchByRequestLogKeyWord(requestKeyWord, uri, hostname, requestUri, page,startDate,endDate);

        if (requestLogs != null && !requestLogs.getContent().isEmpty())
            return MockRuleMgmtResponseVo.builder().status(0).success(true).data(requestLogs).build();
        else
            return MockRuleMgmtResponseVo.builder().status(0).success(false).message("No request logs found.").build();

    }

    @PostMapping(value = "/queryEurekaRule")
    public MockRuleMgmtResponseVo queryEurekaRules(@RequestBody JSONObject requestBody) {

        int pageNumber = Math.max(requestBody.getIntValue(PAGE_NUMBER), 0);
        int pageSize = requestBody.getIntValue(PAGE_SIZE) <= 0 ? 50 : requestBody.getIntValue(PAGE_SIZE);

        PageRequest page = PageRequest.of(pageNumber, pageSize);

        Page<EurekaMockRule> rules = null;

        EurekaMockRule ruleExample = EurekaMockRule.builder().build();
        ruleExample.setEurekaServer(StringUtils.isEmpty(StringUtils.trimAllWhitespace(requestBody.getString("eurekaServer"))) ? null
                : StringUtils.trimAllWhitespace(requestBody.getString("eurekaServer")));
        ruleExample.setServiceName(StringUtils.isEmpty(StringUtils.trimAllWhitespace(requestBody.getString("serviceName"))) ? null
                : StringUtils.trimAllWhitespace(requestBody.getString("serviceName")));
        Example<EurekaMockRule> example = Example.of(ruleExample);
        rules = eurekaMockRuleRepository.findAll(example, page);

        if ( !rules.getContent().isEmpty())
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
        if (mockRule.getId() == null || mockRule.getId().isEmpty()) {
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
        if (mockRule.getId() == null || mockRule.getId().isEmpty()) {
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

        String username = requestBody.getString(USERNAMEPARAM);
        String password = requestBody.getString(PASSWORDPARAM);

        boolean loginStatus = userService.login(username, password);
        User user = userService.finduserByuserName(username);
        if (user != null) {
            Cookie mu = new Cookie("mu", user.getId());
            mu.setHttpOnly(true);
            response.addCookie(mu);
        }
        return MockRuleMgmtResponseVo.builder().status(0).success(loginStatus).build();
    }

    @PostMapping(value = "/logout")
    public MockRuleMgmtResponseVo logout(@RequestBody JSONObject requestBody, HttpServletRequest request) {

        String username = requestBody.getString(USERNAMEPARAM);

        return MockRuleMgmtResponseVo.builder().status(0).success(userService.logout(username, request.getCookies()))
                .build();
    }

    @PostMapping(value = "/createUser")
    public MockRuleMgmtResponseVo createUser(@RequestBody JSONObject requestBody) {

        String username = requestBody.getString(USERNAMEPARAM);
        String password = requestBody.getString(PASSWORDPARAM);

        return MockRuleMgmtResponseVo.builder().status(0).success(userService.createUser(username, password)).build();
    }

    @PostMapping(value = "/rePassword")
    public MockRuleMgmtResponseVo rePasswordUser(@RequestBody JSONObject requestBody, HttpServletRequest request) {

        String username = requestBody.getString(USERNAMEPARAM);
        String password = requestBody.getString(PASSWORDPARAM);
        String newpassword = requestBody.getString("newpassword");

        return MockRuleMgmtResponseVo.builder().status(0)
                .success(userService.rePasswordUser(username, password, newpassword, request.getCookies())).build();
    }

    @PostMapping(value = "/delUser")
    public MockRuleMgmtResponseVo delUser(@RequestBody JSONObject requestBody, HttpServletRequest request) {

        String username = requestBody.getString(USERNAMEPARAM);
        String password = requestBody.getString(PASSWORDPARAM);

        return MockRuleMgmtResponseVo.builder().status(0)
                .success(userService.delUser(username, password, request.getCookies())).build();
    }

    @PostMapping(value = "/isLogin")
    public MockRuleMgmtResponseVo isUserLogin() {
        return MockRuleMgmtResponseVo.builder().status(0).success(true).build();
    }

}