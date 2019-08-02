package com.rrd.coresystem.testasist.mockplatform;

import java.util.Map;

import lombok.Data;

@Data
public class MockRequest {
	private Map<String , String> headers ;
	private String responseBody;
	

}
