package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
	public String converter(String originalResponse, Map<String, String> requestHeaders,
			Map<String, String> requestQueryString, byte[] requestBody) {

		String pattern = "\\$\\{([^_]{1}.*?)\\}";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(originalResponse);

		// boolean firstMatch=true;

		int offposition = 0;

		boolean logResponse = false;
		while (m.find()) {
			String replaceString = null;
			log.debug("CusotomVarReplacementConverterHandler - Found need replacement vars: " + m.group(0));
			log.debug("CusotomVarReplacementConverterHandler - start{} end{}", m.start(), m.end());
			log.debug("CusotomVarReplacementConverterHandler - Found var expression: " + m.group(1));

			if (!logResponse) {
				logResponse = true;
				log.debug("headers: {} response: {}  to be extracted: ", requestHeaders, requestBody);
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
					// m.group(1).substring(15) get the "headerkey" used to get header value. 14 is
					// "requestHeader." length. 2 is "$." length.
					replaceString = getReplaceStringFromHeader(m.group(1).substring(14 + 2), requestHeaders);
				} else if (m.group(1).startsWith("requestQueryString.")) {
					// requestHeader.$."headerKey"
					// m.group(1).substring(15) get the "headerkey" used to get header value.
					replaceString = getReplaceStringFromQueryString(m.group(1).substring(21), requestQueryString);
				}
				if (replaceString != null) {
					offposition = offposition + replaceString.length() - m.group(0).length();
					originalResponse = originalResponse.substring(0, newStart) + replaceString
							+ originalResponse.substring(newEnd);
				}

			} catch (Exception e) {
				log.error("originalResponse:{} ,  {} replacement got errors", m.group(1), e);
			}

		}

		return originalResponse;
	}

	private String getReplaceStringFromQueryString(String extractPath, Map<String, String> requestQueryString) {

		String returnValue = null;

		returnValue = requestQueryString.get(extractPath);

		if (returnValue == null)
			returnValue = "!!requestQueryString_" + extractPath + "_Undefined!!";
		else {
			try {
				returnValue = URLDecoder.decode(returnValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.warn("url decode exception!{}", e);
			}
		}
		return returnValue;

	}

	private String getReplaceStringFromHeader(String extractPath, Map<String, String> requestHeaders) {

		String returnValue = null;
		returnValue = requestHeaders.get(extractPath);
		if (returnValue == null) {
			// header default is lowercase string
			extractPath = extractPath.toLowerCase();
			returnValue = requestHeaders.get(extractPath);
		}
		if (returnValue == null)
			returnValue = "!!requestHeader_" + extractPath + "_Undefined!!";
		return returnValue;

	}

	private String getReplaceStringFromBody(String extractPath, Map<String, String> requestHeaders,
			byte[] requestBody) {
		log.debug("get from body:{} ", extractPath);

		if (contentTypeContains(requestHeaders, "application/x-www-form-urlencoded")) {

			String extractValue = wwwformtoMap(new String(requestBody, StandardCharsets.UTF_8))
					.get(extractPath.replace("$.", ""));

			if (extractValue == null)
				return "!!requestBody_" + extractPath + "_Undefined!!";
			else
				return extractValue;

		} else if (contentTypeContains(requestHeaders, "application/xml")) {
			log.warn("content type: xml not support to extract!");
			return "!!xml_content_not_support_extract!!";
		} else if (contentTypeContains(requestHeaders, "application/json")) {
			try {
				ReadContext ctx = JsonPath.parse(new String(requestBody, StandardCharsets.UTF_8));
				String jsonValue = JSON.toJSONString(ctx.read(extractPath));
				// 因为JSON.toJSONString后非json format串会加上双引号，因为我们不需要双引号，此时我们需要处理下。
				jsonValue = StringUtils.strip(jsonValue, "\"");
				if (jsonValue == null)
					return "!!requestBody_" + extractPath + "_Undefined!!";
				return jsonValue;

			} catch (Exception e) {
				log.warn("{} read json path error: ", requestBody, e);
			}
		} else {
			log.warn(" {} not support  to extract!", requestHeaders.get("content-type"));
		}

		return "!!requestBody_" + extractPath + "_Undefined!!";
	}

	private boolean contentTypeContains(Map<String, String> requestHeaders, String content) {

		if (!StringUtils.isBlank(requestHeaders.get("content-type"))) {

			return requestHeaders.get("content-type").contains(content);

		} else {
			return false;
		}

	}

	/**
	 * 把x-www-form-urlencode的字符串转为Map 例如 a=b 转为 Map<"a","b">
	 * 
	 * @param requestBody
	 * @return
	 */
	private Map<String, String> wwwformtoMap(String requestBody) {

		Map<String, String> requestBodyMap = new HashMap<>();

		try {
			requestBody = URLDecoder.decode(requestBody, "UTF-8");
		} catch (UnsupportedEncodingException e) {
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

		JSONObject a = new JSONObject();
		a.put("test", "value");
		System.out.println(JSON.toJSONString(a));

	}

}
