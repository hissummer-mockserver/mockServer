package com.hissummer.mockserver.mock.vo;

import java.util.Map;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockResponse {

	private Map<String, String> responseHeaders;
	private Object responseBody; // now only support string responseBody
	private boolean isMock;
	private boolean isUpstream;
	private HttpMockRule mockRule;
}
