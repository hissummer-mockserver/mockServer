package com.hissummer.mockserver.mgmt.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.hissummer.mockserver.mgmt.pojo.HttpCondition;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "conditionrules")
@Builder
public class HttpConditionRule {
	String id;
	String httpMockRuleId;
	List<HttpCondition> conditionRules;

}
