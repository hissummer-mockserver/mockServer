package com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface;

import java.util.Map;

public interface MockResponseTearDownConverterInterface {

	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody);

}
