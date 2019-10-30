package com.hissummer.mockserver.mock.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.json.JsonWriterSettings.Builder;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.vo.HttpMockRule;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.vo.HttpMockWorkMode;
import com.hissummer.mockserver.mgmt.vo.Upstream;
import com.hissummer.mockserver.mock.service.mockResponseConverter.MockResponseConverter;
import com.hissummer.mockserver.mock.vo.MockResponse;

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
	MongoDbRunCommandServiceImpl dataplatformServiceImpl;

	@Autowired
	List<MockResponseConverter> mockResponseConverters;

	@Autowired
	MockRuleMongoRepository mockRuleRepository;

	final private String NOMATCHED = "Sorry , No rules matched.";

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文.
	 * 
	 * @param headers
	 * @param requestUri
	 * @return mock的报文
	 */
	public String getResponse(Map<String, String> headers, String hostname, String method, String requestUri,
			String requestBody) {

		MockResponse response = __getResponse(headers, hostname, method, requestUri, requestBody);

		if (response != null) {
			return response.getResponseBody();
		} else {
			return JSON
					.toJSONString(MockRuleMgmtResponseVo.builder().status(0).success(false).message(NOMATCHED).build());
		}
	}

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文的具体实现
	 * 
	 * @param headers
	 * @param requestUri
	 * @return
	 */
	private MockResponse __getResponse(Map<String, String> headers, String hostName, String method, String requestUri,
			String requestBody) {

		String host = hostName;
		headers.get("Host");
		log.info("host in headers: ", host);

		// 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
		if (__isIp(host)) {
			host = "*";
		}
		// 第一次匹配规则
		HttpMockRule matchedResult = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);

		// 如果第一次查找时,Host是域名,且没有找到对应的规则,则会重新假设Host为null时,重新再查找一次.
		if (matchedResult == null && host != null && !host.equals("*")) {
			host = "*";
			// 如果第一次host不为null时没有查到匹配规则,则重新将host设置为null,重新查找一次规则.
			matchedResult = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);
		}

		if (matchedResult != null) {
			// 获取到匹配的结果
			String upstream = null;
			try {
				upstream = matchedResult.getUpstreamGroup().getUpstreams().get(0).getUpstreamAddress();
			} catch (Exception e) {
				log.info("{} mockrule : upstream data is not defined{}", matchedResult.getId(),
						matchedResult.getUpstreamGroup());
			}
			HttpMockWorkMode workMode = matchedResult.getWorkMode();

			String protocol = matchedResult.getProtocol();

			if (workMode != null && workMode.equals(HttpMockWorkMode.UPSTREAM) && upstream != null) {

				// mock rule 的工作模式为upstream模式
				String response = __getUpstreamResponse(protocol, headers, upstream, method, requestUri, requestBody);
				return MockResponse.builder().responseBody(response).build();
			} else {
				// mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
				return MockResponse.builder()
						.responseBody(__interpreterResponse(matchedResult.getMockResponse(), headers, requestBody))
						.build();
			}
		} else
			return null;

	}

	private String __interpreterResponse(String originalMockResponse, Map<String, String> requestHeders,
			String requestBody) {
		String mockResponse = originalMockResponse;
		for (MockResponseConverter mockResponseConverter : mockResponseConverters) {
			mockResponse = mockResponseConverter.converter(mockResponse, requestHeders, requestBody);
		}
		return mockResponse;
	}

	private String __getUpstreamResponse(String protocol, Map<String, String> headers, String upstream, String method,
			String requestUri, String requestBody) {
		// TODO Auto-generated method stub

		final OkHttpClient client = new OkHttpClient();

		Headers.Builder headerBuilder = new Headers.Builder();

		for (Entry<String, String> header : headers.entrySet()) {
			headerBuilder.add(header.getKey(), header.getValue());
		}

		Headers requestHeaders = headerBuilder.build();

		RequestBody okHttpRequestBody = null;
		if (requestBody != null) {
			okHttpRequestBody = RequestBody.create(requestBody, MediaType.parse(headers.get("content-type")));
		}
		Request request = new Request.Builder().url(protocol + "://" + upstream + requestUri)
				.method(method, okHttpRequestBody).headers(requestHeaders).build();

		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			log.info(response.toString());
			if (response.isSuccessful())
				return response.body().string();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * isIp 判断是否是ip地址
	 */
	private boolean __isIp(String host) {
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

		for (int i = 0; i <= matchRequestURI.size(); i++) {

			if (i != 0) {
				matchRequestURI.remove(matchRequestURI.size() - 1);
				matchRequestURIString = String.join("/", matchRequestURI);
			}
			if (i == matchRequestURI.size()) {
				matchRequestURIString = "/";
			}

			HttpMockRule matchedMockRule = mockRuleRepository.findByHostAndUri(hostName, matchRequestURIString);

			if (matchedMockRule != null)
				return matchedMockRule;
		}
		return null;
	}

}
