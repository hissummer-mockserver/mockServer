package com.hissummer.mockserver.mock.service.mockResponseConverter;

import java.util.Map;

public interface MockResponseConverterInterface {

	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody);

}
