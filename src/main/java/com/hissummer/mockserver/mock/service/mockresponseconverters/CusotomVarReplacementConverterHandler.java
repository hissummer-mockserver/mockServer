package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseSetUpConverterInterface;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(value = 1)
@Slf4j
public class CusotomVarReplacementConverterHandler implements MockResponseSetUpConverterInterface {

	@Autowired
	ApplicationContext context;

	@Override
	public String converter(String originalResponse, Map<String, String> requestHeaders, String requestBody) {

		String pattern = "\\$\\{([^_]+?)\\}";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(originalResponse);

		// boolean firstMatch=true;

		String replaceString = null;
		int offposition = 0;

		boolean logResponse = false;
		while (m.find()) {
			log.info("CusotomVarReplacementConverterHandler - Found need replacement vars: " + m.group(0));
			log.info("CusotomVarReplacementConverterHandler - start{} end{}", m.start(), m.end());
			log.info("CusotomVarReplacementConverterHandler - Found var expression: " + m.group(1));

			log.info(originalResponse);
			
			if(!logResponse)
			{
				logResponse = true;
				log.info("headers: {} response: {}  to be extracted: ",requestHeaders,requestBody);
			}

			try {

				int newStart = m.start();
				int newEnd = m.end();

				newStart = m.start() + offposition;
				newEnd = m.end() + offposition;

				if (m.group(1).startsWith("requestBody.")) {
					replaceString = getReplaceStringFromBody(m.group(1).substring(12), requestHeaders, requestBody);
				} else if (m.group(1).startsWith("requestHeader.")) {
					// requestHeader.$."headerKey"
					// m.group(1).substring(15) get the "headerkey" used to get header value.
					replaceString = getReplaceStringFromHeader(m.group(1).substring(16), requestHeaders);
				}
				if (replaceString != null) {
					offposition = offposition + replaceString.length() - m.group(0).length();

					originalResponse = originalResponse.substring(0, newStart) + replaceString
							+ originalResponse.substring(newEnd);
				} else {
					log.warn("replacement string is null");
				}

				log.info(originalResponse);
			} catch (Exception e) {

				log.warn("replacement got errors", m.group(1), e);
			}

		}

		return originalResponse;
	}

	private String getReplaceStringFromHeader(String extractPath, Map<String, String> requestHeaders) {

		// header is lowercase string
		extractPath = extractPath.toLowerCase();
		log.info("get from headers:{} ", extractPath);

		String returnValue = requestHeaders.get(extractPath);
		if (returnValue == null)
			returnValue = "null";
		return returnValue;

	}

	private String getReplaceStringFromBody(String extractPath, Map<String, String> requestHeaders,
			String requestBody) {
		log.info("get from body:{} ", extractPath);

		if (contentTypeContains(requestHeaders,"application/x-www-form-urlencoded")) {

			return wwwformtoMap(requestBody).get(extractPath.replace("$.", ""));

		} else if (contentTypeContains(requestHeaders,"application/xml")) {
			log.warn("content type : xml not support  to extract!");
		} else if (contentTypeContains(requestHeaders,"application/json")) {
			try {
				ReadContext ctx = JsonPath.parse(requestBody);
				String jsonValue = JSON.toJSONString(ctx.read(extractPath));
				// 因为JSON.toJSONString后非json format串会加上双引号，因为我们不需要双引号，此时我们需要处理下。
				jsonValue = StringUtils.strip(jsonValue, "\"");
				if (jsonValue == null)
					jsonValue = "null";
				return jsonValue;

			} catch (Exception e) {
				log.warn("{} read json path error: ", requestBody,e);
			}
		} else {
			log.warn(" {} not support  to extract!", requestHeaders.get("content-type"));
		}

		return null;
	}
	
	private boolean contentTypeContains( Map<String, String> requestHeaders , String content)
	{
		
		return requestHeaders.get("content-type").contains(content);
		
	}
	

	/**
	 * 把x-www-form-urlencode的字符串转为Map
	 * 例如 a=b 转为  Map<"a","b">
	 * @param requestBody
	 * @return 
	 */
	private Map<String, String> wwwformtoMap(String requestBody) {


		Map<String, String> requestBodyMap = new HashMap<>();

		try {
			requestBody = URLDecoder.decode(requestBody, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return requestBodyMap;
		}
		
		String[] parameters = requestBody.split("&");

		for (int i = 0; i < parameters.length; i++) {

			String[] pandvalue = parameters[i].split("=");

			if (pandvalue.length > 1) {
				requestBodyMap.put(pandvalue[0], pandvalue[1]);
			} else if (pandvalue.length == 1) {
				requestBodyMap.put(pandvalue[0], "");
			}

		}
		return requestBodyMap;
	}

	 public static void main(String args[]) {
	
		  JSONObject a= new JSONObject();
		  a.put("test", "value");
		 System.out.println(JSON.toJSONString(a));
	
	 }

}
