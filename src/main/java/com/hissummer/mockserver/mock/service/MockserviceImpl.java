package com.hissummer.mockserver.mock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.pojo.CompareCondition;
import com.hissummer.mockserver.mgmt.pojo.HttpCondition;
import com.hissummer.mockserver.mgmt.pojo.HttpMockWorkMode;
import com.hissummer.mockserver.mgmt.pojo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.service.HttpConditionRuleServiceImpl;
import com.hissummer.mockserver.mock.service.jpa.MockRuleMongoRepository;
import com.hissummer.mockserver.mock.service.mockresponseconverters.GroovyScriptsHandler;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.TextConverterSetUpInterface;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseTearDownConverterInterface;
import com.hissummer.mockserver.mock.vo.MockResponse;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * MockserviceImpl
 * 
 * @author lihao
 * 
 *
 */
@Slf4j
@Service
public class MockserviceImpl {

	@Autowired
	List<TextConverterSetUpInterface> setUpConverters;
	@Autowired
	List<MockResponseTearDownConverterInterface> tearDownConverters;
	@Autowired
	MockRuleMongoRepository mockRuleRepository;

	@Autowired
	HttpConditionRuleServiceImpl httpConditionRuleServiceImpl;

	@Autowired
	GroovyScriptsHandler groovyScriptsHandler;

	private static final String NOMATCHED = "Sorry , No rules matched.";

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文.
	 * 
	 * @param requestHeaders
	 * @param requestUri
	 * @return mock的报文
	 */
	public String getResponseBody(Map<String, String> requestHeaders, String requestHostname, String requestMethod,
			String requestUri, String requestQueryString, byte[] requestBody) {

		MockResponse response = getResponse(requestHeaders, requestHostname, requestMethod, requestUri,
				requestQueryString, requestBody);
		return response.getResponseBody();
	}

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文的具体实现
	 * 
	 * @param requestHeaders
	 * @param requestUri
	 * @return
	 */
	public MockResponse getResponse(Map<String, String> requestHeaders, String requestHostName, String requestMethod,
			String requestUri, String requestQueryString, byte[] requestBody) {

		MockResponse returnResponse = null;
		String host = requestHostName;
		if (!host.equals(requestHeaders.get("Host"))) {
			log.warn("requestHostName = {}, header Host value={} not equal", requestHostName,
					requestHeaders.get("Host"));
		}

		// 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
		// TODO 需要支持ipv6
		if (__isIpv4(host)) {
			host = "*";
		}
		// 第一次匹配规则
		HttpMockRule matchedMockRule = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);
		HttpCondition conditionRule = null;
		// 如果第一次查找时,Host是域名,没有找到对应的规则,则会重新假设Host为*时,重新再查找一次.
		if (matchedMockRule == null && !host.equals("*")) {
			host = "*";
			// 如果第一次host不为null时没有查到匹配规则,则重新将host设置为null,重新查找一次规则.
			matchedMockRule = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);
		}

		if (matchedMockRule != null) {

			// 如果找到了匹配的mock规则，然后查找是否有命中的条件规则。
			HttpConditionRule conditionRulesOnMockRule = httpConditionRuleServiceImpl
					.getHttpConditionRulesByHttpMockRuleId(matchedMockRule.getId());

			if (conditionRulesOnMockRule != null
					&& (conditionRule = getMatchedConditionRule(conditionRulesOnMockRule.getConditionRules(),
							requestUri, requestMethod, requestQueryString, requestHeaders, requestBody)) != null) {

				// 如果存在条件规则，则直接看是否命中某一条规则

				HttpMockWorkMode workMode = conditionRule.getWorkMode();
				if (workMode == null)
					workMode = HttpMockWorkMode.MOCK;
				switch (workMode) {
				case UPSTREAM:
					// mock rule 的工作模式为upstream模式. 后期将upstream作为hostname的rule单独管理，这里的代码将会移除！
					returnResponse = getUpstreamResponse(matchedMockRule, conditionRule, requestHeaders,
							requestMethod, requestUri + "?" + requestQueryString, requestBody);
					break;

				case MOCK:
					// mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
					returnResponse = MockResponse.builder()
							.responseBody(interpreter(conditionRule.getMockResponse(), requestHeaders, requestUri,
									requestQueryString, requestBody, false))
							.mockRule(matchedMockRule).isMock(true).isUpstream(false)
							.headers(conditionRule.getResponseHeaders()).build();
					break;

				case INTERNAL_FORWARD:
					break;
				default:
					break;

				}

			}

			else {
				// 没有找到条件规则，则走默认的mock规则。
				HttpMockWorkMode workMode = matchedMockRule.getWorkMode();
				if (workMode == null)
					workMode = HttpMockWorkMode.MOCK;
				switch (workMode) {
				case UPSTREAM:
					// mock rule 的工作模式为upstream模式. 后期将upstream作为hostname的rule单独管理，这里的代码将会移除！
					returnResponse = getUpstreamResponse(matchedMockRule, null, requestHeaders, requestMethod,
							requestUri + "?" + requestQueryString, requestBody);
					break;

				case MOCK:
					// mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
					returnResponse = MockResponse.builder()
							.responseBody(interpreter(matchedMockRule.getMockResponse(), requestHeaders, requestUri,
									requestQueryString, requestBody, false))
							.mockRule(matchedMockRule).isMock(true).isUpstream(false)
							.headers(matchedMockRule.getResponseHeaders()).build();
					break;

				case INTERNAL_FORWARD:
					break;
				default:
					break;
				}
			}
		} else {
			// 什么也不需要做，返回默认的规则
		}

		if (returnResponse == null) {
			String nomatchresponse = JSON
					.toJSONString(MockRuleMgmtResponseVo.builder().status(0).success(true).message(NOMATCHED).build());
			returnResponse = MockResponse.builder().responseBody(nomatchresponse)
					.mockRule(HttpMockRule.builder().uri("null").host("*").build()).build();
		}

		return returnResponse;
	}

	private HttpCondition getMatchedConditionRule(List<HttpCondition> conditionRules, String requestUri,
			String requestMethod, String requestQueryString, Map<String, String> requestHeaders, byte[] requestBody) {
		String[] conditionsResult = { "" };

		for (HttpCondition condition : conditionRules) {

			// 默认的顺序 TODO 这里需要确认存入的顺序和读出的顺序是否一致。

			conditionsResult[0] = "response = ";
			condition.getConditionExpression().forEach(conditionExpression -> {

				conditionsResult[0] = conditionsResult[0]
						+ ConditionConverter.converToGroovyExpression(conditionExpression.getToBeCompareValue(),
								conditionExpression.getCompareCondition(), conditionExpression.getConditionValue());

			});

			String result = interpreter(conditionsResult[0], requestHeaders, requestUri, requestQueryString,
					requestBody, true);

			if (result.equals("true")) {
				return condition;
			}
		}

		return null;

	}

	/**
	 * 解析文本文件
	 * 
	 * @param toBeInterpreteredText 待解析处理的文本
	 * @param requestHeders         请求头
	 * @param requestUri            请求Uri
	 * @param requestQueryString    请求查询字符串
	 * @param requestBody           请求体
	 * @param forceUseGroovyHandler 是否强制使用groovy脚本执行
	 * @return
	 */
	private String interpreter(String toBeInterpreteredText, Map<String, String> requestHeders, String requestUri,
			String requestQueryString, byte[] requestBody, boolean forceUseGroovyHandler) {

		// multipart 暂不支持requestBody的解析，multipart的请求报文待确认后支持
//		if (requestHeders.get("content-type") == null || requestHeders.get("content-type").contains("multipart")) {
//			// requestBody = "";
//		}
		Map<String, String> requestQueryStringMap = new HashMap<>();

		if (requestQueryString != null && !requestQueryString.equals("null")) {
			String[] queryStrings = requestQueryString.split("&");

			for (String queryString : queryStrings) {
				String[] keyvalue = queryString.split("=");
				String value = keyvalue[1] == null ? null : keyvalue[1];
				if (keyvalue[0] != null)

				{
					requestQueryStringMap.put(keyvalue[0], value);
				}

			}
		}
		String afterInterpreteredText = toBeInterpreteredText;
		for (TextConverterSetUpInterface converter : setUpConverters) {
			// 内部变量替换
			afterInterpreteredText = converter.converter(afterInterpreteredText, requestHeders, requestUri,
					requestQueryStringMap, requestBody);
		}

		if (toBeInterpreteredText.startsWith("//groovy") || forceUseGroovyHandler) {
			// groovy的脚本执行
			afterInterpreteredText = groovyScriptsHandler.converter(afterInterpreteredText, requestHeders,
					requestQueryStringMap, requestBody);
		}

		for (MockResponseTearDownConverterInterface converter : tearDownConverters) {
			// 输出的格式化
			afterInterpreteredText = converter.converter(afterInterpreteredText, requestHeders,
					requestQueryStringMap, requestBody);
		}

		return afterInterpreteredText;
	}

	private MockResponse getUpstreamResponse(HttpMockRule matchedMockRule, HttpCondition condition,
			Map<String, String> requestHeaders, String requestMethod, String requestUri, byte[] requestBody) {
		// 获取到匹配的结果
		String upstreamAddress = "mockserver.hissummer.com";
		String protocol = "http";
		String upstreamUri = "/docs";
		try {
			if (matchedMockRule.getUpstreams().getNodes().get(0).getAddress() != null)
				upstreamAddress = condition != null ? condition.getUpstreams().getNodes().get(0).getAddress()
						: matchedMockRule.getUpstreams().getNodes().get(0).getAddress();

			if (matchedMockRule.getUpstreams().getNodes().get(0).getProtocol() != null)
				protocol = condition != null ? condition.getUpstreams().getNodes().get(0).getProtocol()
						: matchedMockRule.getUpstreams().getNodes().get(0).getProtocol();

			if (matchedMockRule.getUpstreams().getNodes().get(0).getUri() != null)
				upstreamUri = condition != null ? condition.getUpstreams().getNodes().get(0).getUri()
						: matchedMockRule.getUpstreams().getNodes().get(0).getUri();

		} catch (Exception e) {
			log.error("{} mockrule : upstream data is not defined{}", matchedMockRule.getId(),
					matchedMockRule.getUpstreams());
		}

		upstreamUri = getActualRequestUpstreamUri(matchedMockRule.getUri(), requestUri, upstreamUri);

		MockResponse upstreamResponse = requestToUpstream(protocol, requestHeaders, upstreamAddress, requestMethod,
				upstreamUri, requestBody);

		upstreamResponse.setMockRule(matchedMockRule);

		return upstreamResponse;
	}

	/**
	 * 
	 * 
	 * {"upstreams":[{protocol:"",addrress:"",uri:""},{}]}
	 * http://mockserver/request1/ ->
	 * 
	 * @param requestUri
	 * @param upstreamUri
	 * @return
	 */
	private String getActualRequestUpstreamUri(String mockRuleUri, String requestUri, String upstreamUri) {

		String handledRequestUri = requestUri;
		String handledUpstreamUri = upstreamUri;
		String handledMockRuleUri = mockRuleUri;

		if (requestUri.charAt(requestUri.length() - 1) == '/') {
			handledRequestUri = requestUri.substring(0, requestUri.length() - 1);
		}
		if (upstreamUri.charAt(upstreamUri.length() - 1) == '/') {
			handledUpstreamUri = upstreamUri.substring(0, upstreamUri.length() - 1);
		}
		if (mockRuleUri.charAt(mockRuleUri.length() - 1) == '/') {
			handledMockRuleUri = mockRuleUri.substring(0, mockRuleUri.length() - 1);
		}

		String suffixUri = handledRequestUri.substring(handledMockRuleUri.length());

		if (suffixUri.length() > 0 && suffixUri.charAt(0) != '/') {
			suffixUri = "/" + suffixUri;
		}

		return handledUpstreamUri + suffixUri;
	}

	public static void main(String[] args) {
		System.out.println(
				new MockserviceImpl().getActualRequestUpstreamUri("/test/", "/test/jsdjfksdjf/sjdjfjf/", "/test2/"));

	}

	private MockResponse requestToUpstream(String protocol, Map<String, String> requestHeaders,
			String upstreamAddress, String requestMethod, String requestUri, byte[] requestBody) {

		Headers.Builder headerBuilder = new Headers.Builder();

		for (Entry<String, String> header : requestHeaders.entrySet()) {
			if (header.getKey().equalsIgnoreCase("host")) {
				headerBuilder.add(header.getKey(), getHost(upstreamAddress));
			} else {
				headerBuilder.add(header.getKey(), header.getValue());
			}
		}

		Headers requestUpstreamHeaders = headerBuilder.build();

		RequestBody okHttpRequestBody = null;
		if (requestBody != null) {
			okHttpRequestBody = RequestBody.create(requestBody, MediaType.parse(requestHeaders.get("content-type")));
		}
		Request request = new Request.Builder().url(protocol + "://" + upstreamAddress + requestUri)
				.method(requestMethod, okHttpRequestBody).headers(requestUpstreamHeaders).build();
		log.debug("upstream request: {} | {}", JSON.toJSONString(request.headers()), JSON.toJSONString(request.body()));
		Call call = HttpClientUtil.client.newCall(request);
		try {
			Response response = call.execute();
			log.debug("upstream response:{} | {} | {}", JSON.toJSONString(response.code()),
					JSON.toJSONString(response.headers()), JSON.toJSONString(response.body()));		
			JSONObject responseJson = new JSONObject();
			Map<String, String> upstreamResponseHeaders = new HashMap<>();
			if (response.isSuccessful()) {
				Iterator<Pair<String, String>> headerIterator = response.headers().iterator();
				while (headerIterator.hasNext()) {
					Pair<String, String> responseHeader = headerIterator.next();
					upstreamResponseHeaders.put(responseHeader.getFirst(), responseHeader.getSecond());
				}

				if (upstreamResponseHeaders.containsKey("X-Forwarded-For")) {
					upstreamResponseHeaders.put("X-Forwarded-For",
							"hissummer-mockserver," + upstreamResponseHeaders.get("X-Forwarded-For"));
				} else {
					upstreamResponseHeaders.put("X-Forwarded-For", "hissummer-mockserver");
				}

				return MockResponse.builder().headers(upstreamResponseHeaders).responseBody(response.body().string())
						.isUpstream(true).isMock(false).build();
			} else {

				Map<String, List<String>> readableHeaders = response.headers().toMultimap();

				responseJson.put("code", response.code());
				responseJson.put("message", response.message());
				responseJson.put("networkResponse", response.networkResponse());
				responseJson.put("headers", readableHeaders);
				responseJson.put("body", response.body().string());

				return MockResponse.builder().responseBody(responseJson.toJSONString()).isUpstream(true).isMock(false)
						.build();

			}

		} catch (IOException e) {
			return MockResponse.builder().responseBody(e.toString()).isUpstream(true).isMock(false).build();

		}

	}

	private String getHost(String upstream) {
		return upstream.split(":")[0];
	}

	/*
	 * isIp 判断是否是ipv4地址
	 */
	private boolean __isIpv4(String host) {
		try {
			if (host == null || host.isEmpty()) {
				return false;
			}

			String[] parts = host.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			if (host.endsWith(".")) {
				return false;
			}

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}

	}

	/*
	 * Get matched mock rules by Hostname and url. If the hostname is ip address,
	 * hostname is '*'.
	 * 
	 * 
	 */
	private HttpMockRule __getMatchedMockRulesByHostnameAndUrl(String hostName, String requestUri) {

		String requestUriFormat = requestUri;
		if (requestUri != null && requestUri.length() > 0 && requestUri.charAt(requestUri.length() - 1) == '/') {
			requestUriFormat = requestUri.substring(0, requestUri.length() - 1);
		}

		String[] requestURIArray = requestUriFormat.split("/");
		List<String> matchRequestURI = new ArrayList<String>(Arrays.asList(requestURIArray));
		String matchRequestURIString = requestUriFormat;
		int loops = matchRequestURI.size();

		/**
		 * uri: /1/2/3/4 size=4, loop=5 first loop: /1/2/3/4 second loop: /1/2/3 third
		 * loop: /1/2 fourth loop: /1 fifth loop: /
		 * 
		 */

		for (int i = 0; i <= loops; i++) {

			if (i != 0) {
				// Verify that "remove()" is used correctly. Here is correct, we don't use i in
				// loop. Every loop remove last element of the list.
				matchRequestURI.remove(matchRequestURI.size() - 1);
				if (matchRequestURI.isEmpty()) {
					matchRequestURIString = "/";
				} else {
					matchRequestURIString = String.join("/", matchRequestURI);
				}
			}

			HttpMockRule matchedMockRule = mockRuleRepository.findByHostAndUri(hostName, matchRequestURIString);

			if (matchedMockRule != null)
				return matchedMockRule;
		}
		return null;
	}

	public String testRule(HttpMockRule mockRule) {

		HttpMockWorkMode workMode = mockRule.getWorkMode();

		if (workMode.equals(HttpMockWorkMode.MOCK)) {

			// mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
			return MockResponse.builder()
					.responseBody(
							interpreter(mockRule.getMockResponse(), Collections.emptyMap(), null, null, null, false))
					.isMock(true).isUpstream(false).headers(mockRule.getResponseHeaders()).build().getResponseBody();
		} else {
			return "upstream mode not support test, please directly access the upstream address.";
		}

	}

}
