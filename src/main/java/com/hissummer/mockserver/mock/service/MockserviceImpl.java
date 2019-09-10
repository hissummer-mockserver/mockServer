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
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.vo.MockRuleWorkMode;
import com.hissummer.mockserver.mgmt.vo.Upstream;
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
	
    final private  String NOMATCHED = "Sorry , No rules matched.";

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文.
	 * 
	 * @param headers
	 * @param requestUri
	 * @return mock的报文
	 */
	public String getMockResponse(Map<String, String> headers, String hostname, String method, String requestUri,
			String requestBody) {

		MockResponse response = __getMatchedMockResponse(headers, hostname, method, requestUri, requestBody);

		if (response != null)
		{
			return response.getResponseBody();
		}
		else
		{
			return JSON.toJSONString(MockRuleMgmtResponseVo.builder().status(0).success(false)
					.message(NOMATCHED).build());
		}
	}

	/**
	 * 根据hostName和请求Url地址，获取到Mock报文的具体实现
	 * 
	 * @param headers
	 * @param requestUri
	 * @return
	 */
	private MockResponse __getMatchedMockResponse(Map<String, String> headers, String hostName, String method,
			String requestUri, String requestBody) {

		String host = hostName;
		headers.get("Host");
		log.info("host in headers: ", host);

		// 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
		if (__isIp(host)) {
			host = "*";
		}
		// 第一次匹配规则
		JSONObject matchedResult = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);

		// 如果第一次查找时,Host是域名,且没有找到对应的规则,则会重新假设Host为null时,重新再查找一次.
		if (matchedResult == null && host != null && !host.equals("*")) {
			host = "*";
			// 如果第一次host不为null时没有查到匹配规则,则重新将host设置为null,重新查找一次规则.
			matchedResult = __getMatchedMockRulesByHostnameAndUrl(host, requestUri);
		}

		if (matchedResult != null) {
			// 获取到匹配的结果

			String upstream = ((JSONObject) matchedResult).getString("upstream");

			String workMode = ((JSONObject) matchedResult).getString("workMode");
			
			String protocol = ((JSONObject) matchedResult).getString("protocol");

			if (workMode != null && workMode.equals(MockRuleWorkMode.UPSTREAM.name()) && upstream != null) {

				String response = __getUpstreamResponse(protocol,headers, upstream, method, requestUri, requestBody);				
				return MockResponse.builder().responseBody(response).build();
			}

			return MockResponse.builder().responseBody((((JSONObject) matchedResult).getString("mockResponse")))
					.build();

		} else
			return null;

	}

	private String __getUpstreamResponse(String protocol, Map<String, String> headers, String upstream, String method, String requestUri,
			String requestBody) {
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
		Request request = new Request.Builder().url(protocol+"://" + upstream + requestUri).method(method, okHttpRequestBody)
				.headers(requestHeaders).build();

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
	private JSONObject __getMatchedMockRulesByHostnameAndUrl(String hostName, String requestUri) {

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

			Document matachedResult = dataplatformServiceImpl
					.getDocumentByRunCommand(__generateFindMockRuleCommand(hostName, matchRequestURIString));

			Builder documentBuilder = JsonWriterSettings.builder();
			documentBuilder.outputMode(JsonMode.EXTENDED);
			JSONObject queryResultJson = (JSONObject) JSON.parse(matachedResult.toJson(documentBuilder.build()));
			log.info(queryResultJson.toJSONString());

			// 查看是否找到匹配的mock 规则
			if (__isGetMatchRule(queryResultJson)) {

				return (JSONObject) ((JSONObject) queryResultJson.get("cursor")).getJSONArray("firstBatch").get(0);

			}

		}

		return null;
	}

	private boolean __isGetMatchRule(JSONObject queryResult) {

		if (((JSONObject) queryResult.get("cursor")).getJSONArray("firstBatch").size() > 0) {
			return true;
		} else {
			return false;
		}
	}



	/**
	 * 根据hostName和requestUri 找到第一条mock规则(可能返回未找到)
	 * 
	 * @param hostName
	 *            如果传入的为null或者为空字符串,则hostName认为是* 并进行查找
	 * @param requestUri
	 *            requestUri 从业务场景来看, 不可能为null或者空字符串. 如果传入,则找不到. (因为添加规则时,
	 *            不可能出现uri为null和空串的可能性)
	 * @return
	 */
	private String __generateFindMockRuleCommand(String hostName, String requestUri) {

		// String generatedCommand =
		// String.format("{find:'mockrules',filter:{host:{},uri:'{}'}}",
		// hostName == null ? "null" : "'" + hostName + "'", requestUri);
		//

		Document findCommand = new Document();
		Document findFilter = new Document();
		findCommand.put("find", "mockrules");
		findFilter.put("host", hostName == null || hostName.trim().equals("") ? "*" : hostName);
		findFilter.put("uri", requestUri);
		findCommand.put("filter", findFilter);

		// jsonobject will sort keys automatically when get json string
		// JSONObject findCommand = new JSONObject();
		// JSONObject findFilter = new JSONObject();
		// findCommand.put("find", "mockrules");
		// findFilter.put("host", hostName);
		// findFilter.put("uri", requestUri);
		// findCommand.put("filter", findFilter);

		log.info("生成的Find命令:{}", findCommand.toJson());
		return findCommand.toJson();
	}

}