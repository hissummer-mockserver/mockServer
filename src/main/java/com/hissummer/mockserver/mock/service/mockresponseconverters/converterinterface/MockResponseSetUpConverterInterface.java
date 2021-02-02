package com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface;

import java.util.Map;

public interface MockResponseSetUpConverterInterface {

	public String converter(String originalResponse, Map<String, String> requestHeders, byte[] requestBody);

}
