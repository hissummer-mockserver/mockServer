package com.hissummer.mockserver.mock.service.mockResponseConverter;

import java.util.Map;

public interface MockResponseConverter {

	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody);

}
