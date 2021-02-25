package com.hissummer.mockserver.mgmt.pojo;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HttpCondition {

	private String toBeCompareValue;
	private CompareConditionEnum compareCondition;
	private String conditionValue;
	private String mockResponse;
	private Map<String, String> responseHeaders;
	private UpstreamGroup upstreams;
	@Builder.Default
	private HttpMockWorkMode workMode = HttpMockWorkMode.MOCK;
	@Builder.Default
	private Boolean enable = false;
}
