package com.hissummer.mockserver.mock.vo;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockRequest {

	@Builder.Default
	private String protocol = "http";
	private String host;
	private String method;
	private Map<String, String> headers;
	private String requestBody;

	public void setProtocol(String protocol) {

	}

}
