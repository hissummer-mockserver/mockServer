package com.hissummer.mockserver.mockplatform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoMockResponseBody {

	int status; // server status
	boolean success; // mock or upstream response status
	Object data; // business data
	String message; // message
	
}
