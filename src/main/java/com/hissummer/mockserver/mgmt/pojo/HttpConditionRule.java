package com.hissummer.mockserver.mgmt.pojo;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpConditionRule {

	private List<String> condition;
	private List<String> conditionValue;
	private String mockResponse;
	private Map<String, String> responseHeaders;
	private UpstreamGroup upstreams;
	@Builder.Default
	private HttpMockWorkMode workMode = HttpMockWorkMode.MOCK;

}
