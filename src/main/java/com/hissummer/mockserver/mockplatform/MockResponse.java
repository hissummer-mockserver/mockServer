package com.hissummer.mockserver.mockplatform;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockResponse {

	private Map<String, String> headers;
	private String responseBody; // now only support string responseBody
	private boolean isMock;

}