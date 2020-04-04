package com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface;

import java.util.Map;

public interface ScriptsConverterInterface {

	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody);

}
