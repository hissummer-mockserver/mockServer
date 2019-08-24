package com.hissummer.mockserver.mockplatform.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MockPlatformHttpResponseBodyVo {

	int status; // server status
	boolean success; // business status
	Object data; // business data
	String message;
	
}
