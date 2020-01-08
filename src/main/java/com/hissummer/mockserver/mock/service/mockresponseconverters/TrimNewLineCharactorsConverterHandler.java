package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseTearDownConverterInterface;

/**
 * 
 * @author lihao
 *
 */
@Component
@Order(value = 1)
public class TrimNewLineCharactorsConverterHandler implements MockResponseTearDownConverterInterface {

	@Override
	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody) {

		return originalResponse.replaceAll("\r\n", "").replaceAll("\n", "");
	}

}
