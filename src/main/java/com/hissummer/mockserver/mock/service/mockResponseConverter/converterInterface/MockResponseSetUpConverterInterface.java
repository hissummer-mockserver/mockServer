package com.hissummer.mockserver.mock.service.mockResponseConverter.converterInterface;

import java.util.Map;

public interface MockResponseSetUpConverterInterface {

	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody);

}
