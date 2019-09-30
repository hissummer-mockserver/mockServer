package com.hissummer.mockserver.mock.service.mockResponseConverter;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 
 * @author lihao
 *
 */
@Component
@Order(value=3)
public class TrimNewLineCharactorsConverterHandler implements MockResponseConverter{

	@Override
	public String converter(String originalResponse,Map<String, String> requestHeders, String requestBody) {
		
		return originalResponse.replaceAll("\r\n", "").replaceAll("\n", "");
	}
	
	
	

}
