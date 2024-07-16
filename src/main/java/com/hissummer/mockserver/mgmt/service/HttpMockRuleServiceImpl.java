package com.hissummer.mockserver.mgmt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.service.jpa.HttpMockRuleMongoRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class HttpMockRuleServiceImpl {

    @Autowired
    HttpMockRuleMongoRepository mockRuleMgmtRepository;

    @Autowired
    JmsTemplate jmsTemplate;

    @Transactional
    public HttpMockRule addMockRule(HttpMockRule mockRule) throws ServiceException {

        if (mockRuleMgmtRepository.findByHostAndUri(mockRule.getHost(), mockRule.getUri()) != null) {

            throw ServiceException.builder().status(0).serviceMessage("mockrule already exist.").build();
        }
        return mockRuleMgmtRepository.insert(mockRule);

    }

    @Transactional
    public HttpMockRule updateMockRule(HttpMockRule mockRule) throws ServiceException {

        if (mockRule.getId() == null || mockRule.getId().isEmpty()) {
            throw ServiceException.builder().status(0).serviceMessage("The id could not be empty.").build();

        }

        boolean changedHostnameOrUri = true;
        Optional<HttpMockRule> originalMockRule = mockRuleMgmtRepository.findById(mockRule.getId());
        if (originalMockRule.isPresent() && originalMockRule.get().getHost().equals(mockRule.getHost()) && originalMockRule.get().getUri().equals(mockRule.getUri())) {
            changedHostnameOrUri = false;
        }

        HttpMockRule saveMockRule = mockRuleMgmtRepository.save(mockRule);

        if (changedHostnameOrUri && originalMockRule.isPresent()) {
            jmsTemplate.convertAndSend("cleanCache", originalMockRule.get());
            jmsTemplate.convertAndSend("cleanCache", saveMockRule);

        }

        return saveMockRule;

    }

    @Transactional
    public HttpMockRule deleteMockRule(HttpMockRule mockRule) throws ServiceException {

        if (mockRule.getId() == null || mockRule.getId().isEmpty()) {
            throw ServiceException.builder().status(0).serviceMessage("The id could not be empty.").build();
        }
        jmsTemplate.convertAndSend("cleanCache", mockRule);
        mockRuleMgmtRepository.deleteById(mockRule.getId());
        return mockRule;

    }

    //@Transactional
    public Page<HttpMockRule> queryMockRules(String host, String uri, String category, int pageNumber, int pageSize)
            throws ServiceException {

        Page<HttpMockRule> rules = null;
        String byHost = ".*";
        String byUri = ".*";
        PageRequest page = PageRequest.of(pageNumber, pageSize);
        if (!StringUtils.isEmpty(uri)) {
            byUri = uri;
        }
        if (!StringUtils.isEmpty(host)) {

            if (host.equals("*")) {
                // 因为做的是正则匹配查询，所以特殊的*字符转换为\*，即查询包含*字符的host值。
                byHost = "\\*";
            } else {
                byHost = host;
            }

        }

        if (StringUtils.isEmpty(category)) {
            rules = mockRuleMgmtRepository.findByHostRegexpAndUriRegexp(byHost, byUri, page);
        } else {
            rules = mockRuleMgmtRepository.findByHostRegexpAndUriRegexpAndCategory(byHost, byUri, category, page);
        }

        if (rules != null && !rules.getContent().isEmpty())
            return rules;
        else
            return Page.empty();

    }

}
