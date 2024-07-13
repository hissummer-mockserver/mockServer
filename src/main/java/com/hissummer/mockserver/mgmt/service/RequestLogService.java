package com.hissummer.mockserver.mgmt.service;

import com.hissummer.mockserver.mgmt.entity.RequestLog;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestLogService {


    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<RequestLog> searchByRequestLogKeyWord(String requestBodyKeyword, String mockRuleUri, String mockRuleHostName, String requestUri, PageRequest page) {
        Query query = new Query();
        if (StringUtils.isNotEmpty(requestBodyKeyword)) {
            TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(requestBodyKeyword);
            query.addCriteria(criteria);
        }
        if (StringUtils.isNotEmpty(mockRuleUri)) {

            Criteria mockRuleUriCriteria = Criteria.where("hittedMockRuleUri").is(mockRuleUri);
            query.addCriteria(mockRuleUriCriteria);

        }
        if (StringUtils.isNotEmpty(mockRuleHostName)) {

            Criteria mockRuleHostNameCriteria = Criteria.where("hittedMockRuleHostName").is(mockRuleHostName);
            query.addCriteria(mockRuleHostNameCriteria);

        }
        if (StringUtils.isNotEmpty(requestUri)) {

            Criteria requestUriCriteria = Criteria.where("requestUri").is(requestUri);
            query.addCriteria(requestUriCriteria);

        }
        query.with(page);
        List<RequestLog> documents = mongoTemplate.find(query, RequestLog.class);
        long count = mongoTemplate.count(query, RequestLog.class);
        return new PageImpl<>(documents, page, count);

    }


}
