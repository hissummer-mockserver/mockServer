package com.hissummer.mockserver.mock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.vo.HttpMockRule;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.vo.HttpMockWorkMode;
import com.hissummer.mockserver.mock.service.jpa.MockRuleMongoRepository;
import com.hissummer.mockserver.mock.service.mockresponseconverters.GroovyScriptsHandler;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseSetUpConverterInterface;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseTearDownConverterInterface;
import com.hissummer.mockserver.mock.vo.MockResponse;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
	List<MockResponseSetUpConverterInterface> mockResponseConverters;
	@Autowired
	List<MockResponseTearDownConverterInterface> mockResponseTearDownConverters;
	@Autowired
	MockRuleMongoRepository mockRuleRepository;

	@Autowired
	GroovyScriptsHandler groovyScriptsHandler;

	private static final String NOMATCHED = "Sorry , No rules matched.";

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文.
	 * 
	 * @param headers
	 * @param requestUri
	 * @return mock的报文
	 */
	public String getResponseBody(Map<String, String> headers, String hostname, String method, String requestUri,
			byte[] requestBody) {

		MockResponse response = getResponse(headers, hostname, method, requestUri, requestBody);

		return response.getResponseBody();
	}

	public MockResponse getResponse(Map<String, String> requestHeaders, String requestHostname, String requestMethod,
			String requestUri, byte[] requestBody) {

		MockResponse response = __getResponse(requestHeaders, requestHostname, requestMethod, requestUri, requestBody);

		if (response != null) {
			return response;
		} else {
			return MockResponse.builder()
					.responseBody(JSON.toJSONString(
							MockRuleMgmtResponseVo.builder().status(0).success(true).message(NOMATCHED).build()))
					.build();
		}
	}

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文的具体实现
	 * 
	 * @param requestHeaders
	 * @param requestUri
	 * @return
	 */
	private MockResponse __getResponse(Map<String, String> requestHeaders, String requestHostName, String requestMethod,
			String requestUri, byte[] requestBody) {

		String host = requestHostName;
		requestHeaders.get("Host");

		// 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
		if (__isIpv4(host)) {
			host = "*";
		}
		// 第一次匹配规则
		HttpMockRule matchedMockRule = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);

		// 如果第一次查找时,Host是域名,且没有找到对应的规则,则会重新假设Host为null时,重新再查找一次.
		if (matchedMockRule == null && host != null && !host.equals("*")) {
			host = "*";
			// 如果第一次host不为null时没有查到匹配规则,则重新将host设置为null,重新查找一次规则.
			matchedMockRule = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);
		}

		if (matchedMockRule != null) {

			HttpMockWorkMode workMode = matchedMockRule.getWorkMode();

			if (workMode != null && workMode.equals(HttpMockWorkMode.UPSTREAM)) {

				// mock rule 的工作模式为upstream模式. 后期将upstream作为hostname的rule单独管理，这里的代码将会移除！
				return __getUpstreamResponse(matchedMockRule, requestHeaders, requestMethod, requestUri, requestBody);

			} else {
				// mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
				return MockResponse.builder()
						.responseBody(
								__interpreterResponse(matchedMockRule.getMockResponse(), requestHeaders, requestBody))
						.isMock(true).isUpstream(false).headers(matchedMockRule.getResponseHeaders()).build();
			}
		} else {
			return null;
		}
	}

	private String __interpreterResponse(String originalMockResponse, Map<String, String> requestHeders,
			byte[] requestBody) {

		// multipart 暂不支持requestBody的解析，multipart的请求报文待确认后支持
		if (requestHeders.get("content-type") == null || requestHeders.get("content-type").contains("multipart")) {
			// requestBody = "";
		}

		String mockResponse = originalMockResponse;
		for (MockResponseSetUpConverterInterface mockResponseConverter : mockResponseConverters) {
			mockResponse = mockResponseConverter.converter(mockResponse, requestHeders, requestBody);
		}

		if (originalMockResponse.startsWith("//groovy")) {
			mockResponse = groovyScriptsHandler.converter(mockResponse, requestHeders, requestBody);
		}

		for (MockResponseTearDownConverterInterface mockResponseConverter : mockResponseTearDownConverters) {
			mockResponse = mockResponseConverter.converter(mockResponse, requestHeders, requestBody);
		}

		return mockResponse;
	}

	private MockResponse __getUpstreamResponse(HttpMockRule matchedMockRule, Map<String, String> requestHeaders,
			String requestMethod, String requestUri, byte[] requestBody) {
		// 获取到匹配的结果
		String upstreamAddress = "mockserver.hissummer.com";
		String protocol = "http";
		String upstreamUri = "/docs";
		try {
			if (matchedMockRule.getUpstreams().getNodes().get(0).getAddress() != null)
				upstreamAddress = matchedMockRule.getUpstreams().getNodes().get(0).getAddress();

			if (matchedMockRule.getUpstreams().getNodes().get(0).getProtocol() != null)
				protocol = matchedMockRule.getUpstreams().getNodes().get(0).getProtocol();

			if (matchedMockRule.getUpstreams().getNodes().get(0).getUri() != null)
				upstreamUri = matchedMockRule.getUpstreams().getNodes().get(0).getUri();

		} catch (Exception e) {
			log.info("{} mockrule : upstream data is not defined{}", matchedMockRule.getId(),
					matchedMockRule.getUpstreams());
		}
		
		
		upstreamUri = getActualRequestUpstreamUri(matchedMockRule.getUri(),requestUri,upstreamUri);

		return __getUpstreamResponse(protocol, requestHeaders, upstreamAddress, requestMethod, upstreamUri, requestBody);
	}

	/**
	 * 
	 * 
	 * {"upstreams":[{protocol:"",addrress:"",uri:""},{}]}
	 * http://mockserver/request1/  ->  
	 * 
	 * @param requestUri
	 * @param upstreamUri
	 * @return
	 */
	private String getActualRequestUpstreamUri(String mockRuleUri,String requestUri, String upstreamUri) {

		String handledRequestUri = requestUri;
		String handledUpstreamUri = upstreamUri;
		String handledMockRuleUri = mockRuleUri;
		
		if( requestUri.charAt(requestUri.length()-1) == '/' ) {
			handledRequestUri  = requestUri.substring(0, requestUri.length()-1);
		}
		if( upstreamUri.charAt(upstreamUri.length()-1) == '/' ) {
			handledUpstreamUri  = upstreamUri.substring(0, upstreamUri.length()-1);
		}
		if( mockRuleUri.charAt(mockRuleUri.length()-1) == '/' ) {
			handledMockRuleUri  = mockRuleUri.substring(0, mockRuleUri.length()-1);
		}
		
		String suffixUri  = handledRequestUri.substring(handledMockRuleUri.length());
		
		
		if( suffixUri.length() >0 && suffixUri.charAt(0) != '/' ) {
			suffixUri  = "/"+suffixUri;
		}		
		
		
		return handledUpstreamUri+suffixUri;
	}
	
	public static void main(String[] args)
	{
		System.out.println(new MockserviceImpl().getActualRequestUpstreamUri("/test/","/test/jsdjfksdjf/sjdjfjf/","/test2/"));		
		
	}

	private MockResponse __getUpstreamResponse(String protocol, Map<String, String> requestHeaders,
			String upstreamAddress, String requestMethod, String requestUri, byte[] requestBody) {

		final OkHttpClient client = new OkHttpClient();

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
		log.info("upstream request: {} | {}", JSON.toJSONString(request.headers()), JSON.toJSONString(request.body()));
		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			log.info("upstream response:{} | {} | {}", JSON.toJSONString(response.code()),
					JSON.toJSONString(response.headers()), JSON.toJSONString(response.body()));

			JSONObject responseJson = new JSONObject();

			if (response.isSuccessful()) {
				Iterator<Pair<String, String>> headerIterator = response.headers().iterator();
				while (headerIterator.hasNext()) {
					Pair<String, String> responseHeader =   headerIterator.next();
					requestHeaders.put(responseHeader.getFirst(), responseHeader.getSecond());
				}

				return MockResponse.builder().headers(requestHeaders).responseBody(response.body().string())
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
		for (int i = 0; i <= loops; i++) {

			if (i != 0) {
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
					.responseBody(__interpreterResponse(mockRule.getMockResponse(), Collections.emptyMap(), null))
					.isMock(true).isUpstream(false).headers(mockRule.getResponseHeaders()).build().getResponseBody();
		} else {
			return "upstream mode not support test, please directly access the upstream address.";
		}

	}

}
