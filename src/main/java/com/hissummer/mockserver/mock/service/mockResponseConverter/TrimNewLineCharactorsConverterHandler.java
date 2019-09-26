package com.hissummer.mockserver.mock.service.mockResponseConverter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 
 * @author lihao
 *
 */
@Component
@Order(value=2)
public class TrimNewLineCharactorsConverterHandler implements MockResponseConverter{

	@Override
	public String converter(String originalResponse) {
		
		return originalResponse.replaceAll("\r\n", "").replaceAll("\n", "");
	}
	
	
	

}
