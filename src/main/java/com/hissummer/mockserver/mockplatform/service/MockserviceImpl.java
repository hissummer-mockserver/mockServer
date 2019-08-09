package com.hissummer.mockserver.mockplatform.service;

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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.dataplatform.service.DataplatformServiceImpl;
import com.hissummer.mockserver.mockplatform.MockResponse;
import com.hissummer.mockserver.mockplatform.MockRuleWorkMode;
import com.hissummer.mockserver.mockplatform.NoMockResponseBody;
import com.hissummer.mockserver.mockplatform.Upstream;
import com.mysql.jdbc.StringUtils;

import io.netty.util.internal.StringUtil;
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
	DataplatformServiceImpl dataplatformServiceImpl;
	
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
			return JSON.toJSONString(NoMockResponseBody.builder().status(0).success(false)
					.message(NOMATCHED).build());
		}
	}

	/**
	 * 添加mock 规则, 一个规则包含 hostname, uri, mock的报文. hostname可以为null.
	 * 后续会增加支持返回mockheader支持.
	 * 
	 * @param hostName
	 * @param requestUri
	 * @param mockResponse
	 * @return 返回true则认为添加成功
	 */
	@Deprecated
	public boolean addMockRule(String hostName, String requestUri, String mockResponse,
			Map<String, String> mockHeaders, String workMode) {

		// return addmatachedResult.toJson(documentBuilder.build());
		try {
			String requestUriFormat = requestUri;
			String hostNameFormat = hostName;

			if (!requestUri.startsWith("/"))
				return false;

			if (requestUri != null && requestUri.length() > 1 && requestUri.charAt(requestUri.length() - 1) == '/') {
				requestUriFormat = requestUri.substring(0, requestUri.length() - 1);
			}

			if (StringUtil.isNullOrEmpty(hostName) || this.__isIp(hostName))
				hostNameFormat = "*"; // * meaning including all hostName

			Document addmatachedResult = dataplatformServiceImpl.getDocumentByRunCommand(
					__generateInsertMockRuleCommand(hostNameFormat, requestUriFormat, mockResponse,workMode));
			// 设置document output 到json的设置
			Builder documentBuilder = JsonWriterSettings.builder();
			documentBuilder.outputMode(JsonMode.EXTENDED);
			log.info("添加MockRule结果:{}", addmatachedResult.toJson());
			// 因为一些版本的mongodb（例如 3.4.21）返回的不是integer而是double类型值，所以要特殊处理下。 例如return { ok:
			// 1.0, n : 1}
			Object dn = addmatachedResult.get("n");
			Object dok = addmatachedResult.get("ok");
			int idn = 0;
			int idok = 0;
			if (dn instanceof Double)
				idn = ((Double) dn).intValue();
			else
				idn = (int) dn;
			if (dok instanceof Double)
				idok = ((Double) dok).intValue();
			else
				idok = (int) dok;

			if (idn < idok) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			log.warn("添加MOckRule失败:{}", e);
			return false;
		}

	}

	@Deprecated
	public boolean updateMockRule(String id, String hostName, String requestUri, String mockResponse,
			Map<String, String> mockHeaders, String workMode , Upstream upstreamGroup) {
		// return addmatachedResult.toJson(documentBuilder.build());
		try {
			String requestUriFormat = requestUri;

			if (!requestUri.startsWith("/"))
				return false;

			if (requestUri != null && requestUri.length() > 1 && requestUri.charAt(requestUri.length() - 1) == '/') {
				requestUriFormat = requestUri.substring(0, requestUri.length() - 1);
			}

			Document addmatachedResult = dataplatformServiceImpl.getDocumentByRunCommand(
					__generateUpdateMockRuleCommand(id, hostName, requestUriFormat, mockResponse,workMode));
			// 设置document output 到json的设置
			Builder documentBuilder = JsonWriterSettings.builder();
			documentBuilder.outputMode(JsonMode.EXTENDED);
			log.info("更新MockRule结果:{}", addmatachedResult.toJson());
			// mongodb 3.4.21 return ok = 1.0, n = 1
			Object dn = addmatachedResult.get("n");
			Object dok = addmatachedResult.get("ok");
			int idn = 0;
			int idok = 0;
			if (dn instanceof Double)
				idn = ((Double) dn).intValue();
			else
				idn = (int) dn;
			if (dok instanceof Double)
				idok = ((Double) dok).intValue();
			else
				idok = (int) dok;

			if (idn < idok) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			log.warn("添加MOckRule失败:{}", e);
			return false;
		}

	}

	@Deprecated
	public boolean deleteMockRule(String id) {
		try {

			Document addmatachedResult = dataplatformServiceImpl
					.getDocumentByRunCommand(__generateDeleteMockRuleCommand(id));
			// 设置document output 到json的设置
			Builder documentBuilder = JsonWriterSettings.builder();
			documentBuilder.outputMode(JsonMode.EXTENDED);
			log.info("更新MockRule结果:{}", addmatachedResult.toJson());
			// mongodb 3.4.21 return ok = 1.0, n = 1
			Object dn = addmatachedResult.get("n");
			Object dok = addmatachedResult.get("ok");
			int idn = 0;
			int idok = 0;
			if (dn instanceof Double)
				idn = ((Double) dn).intValue();
			else
				idn = (int) dn;
			if (dok instanceof Double)
				idok = ((Double) dok).intValue();
			else
				idok = (int) dok;

			if (idn < idok) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			log.warn("添加MOckRule失败:{}", e);
			return false;
		}

	}

	private String __generateDeleteMockRuleCommand(String id) {

		Document updateCommand = new Document();
		JSONArray updateDocuments = new JSONArray();
		Document updateDocument = new Document();
		Document query = new Document();

		updateCommand.put("delete", "mockrules");
		ObjectId objectId = new ObjectId(id);
		query.put("_id", objectId);
		updateDocument.put("q", query);
		updateDocument.put("limit", 1);

		updateDocuments.add(updateDocument);
		updateCommand.put("deletes", updateDocuments);

		log.info("生成的Delete命令:{}", updateCommand.toJson());
		return updateCommand.toJson();
	}

	@Deprecated
	public JSONArray queryMockRules(String hostName, String uri, int pageNumber, int pageSize) {
		// TODO Auto-generated method stub

		String host = hostName;

		// 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
		if (StringUtils.isNullOrEmpty(hostName) || __isIp(hostName)) {
			host = "All";
		}

		Document matachedResult = dataplatformServiceImpl
				.getDocumentByRunCommand(__generateFindMockRuleCommand(host, uri, pageNumber, pageSize));

		Builder documentBuilder = JsonWriterSettings.builder();
		documentBuilder.outputMode(JsonMode.EXTENDED);
		JSONObject queryResultJson = (JSONObject) JSON.parse(matachedResult.toJson(documentBuilder.build()));
		log.info(queryResultJson.toJSONString());

		// 查看是否找到匹配的mock 规则
		if (__isGetMatchRule(queryResultJson)) {

			return ((JSONObject) queryResultJson.get("cursor")).getJSONArray("firstBatch");
		}

		return null;
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
	 * 
	 * @param requestUri
	 * @return
	 */
	// private String __getMatchedMockRulesByUrl(String requestUri) {
	// Document matachedResult = dataplatformServiceImpl
	// .getDocumentByRunCommand(__generateFindMockRuleCommand(null, requestUri));
	// // 设置document output 到json的设置
	// Builder documentBuilder = JsonWriterSettings.builder();
	// documentBuilder.outputMode(JsonMode.SHELL);
	// log.info("默认返回的结果:{}", matachedResult.toJson());
	// log.info("shellMode 返回的json结果:{}",
	// matachedResult.toJson(documentBuilder.build()));
	// return matachedResult.toJson(documentBuilder.build());
	// }

	/**
	 * 
	 * @param hostName
	 * @param requestUri
	 * @return
	 */
	private String __generateInsertMockRuleCommand(String hostName, String requestUri, String mockResponse,String workMode) {

		// String generatedCommand =
		// String.format("{insert:'mockrules',documents:[{host:%s,uri:'%s',mockResponse:'%s'}]}",
		// hostName == null ? "null" : "'" + hostName + "'", requestUri, mockResponse);

		Document insertCommand = new Document();
		JSONArray insertDocuments = new JSONArray();
		Document insertRule = new Document();
		insertCommand.put("insert", "mockrules");
		insertRule.put("host", hostName == null || hostName.trim().equals("") ? "*" : hostName);
		insertRule.put("uri", requestUri);
		insertRule.put("mockResponse", mockResponse);
		insertRule.put("workMode", workMode);
		insertDocuments.add(insertRule);
		insertCommand.put("documents", insertDocuments);

		log.info("生成的Insert命令:{}", insertCommand.toJson());
		return insertCommand.toJson();
	}

	private String __generateUpdateMockRuleCommand(String id, String hostName, String requestUri, String mockResponse,String workMode) {

		// String generatedCommand =
		// String.format("{insert:'mockrules',documents:[{host:%s,uri:'%s',mockResponse:'%s'}]}",
		// hostName == null ? "null" : "'" + hostName + "'", requestUri, mockResponse);

		Document updateCommand = new Document();
		JSONArray updateDocuments = new JSONArray();
		Document updateDocument = new Document();
		Document query = new Document();
		Document updateData = new Document();

		updateCommand.put("update", "mockrules");
		// updateDocument.put("host", hostName == null || hostName.trim().equals("") ?
		// "undefined" : hostName);
		// updateDocument.put("uri", requestUri);
		ObjectId objectId = new ObjectId(id);
		query.put("_id", objectId);
		updateData.put("host", hostName == null || hostName.trim().equals("") ? "*" : hostName);
		updateData.put("uri", requestUri);
		updateData.put("mockResponse", mockResponse);
		updateData.put("workMode",workMode);
		updateDocument.put("u", updateData);
		updateDocument.put("q", query);
		updateDocument.put("upsert", false);

		updateDocuments.add(updateDocument);
		updateCommand.put("updates", updateDocuments);

		log.info("生成的Insert命令:{}", updateCommand.toJson());
		return updateCommand.toJson();
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

	/**
	 * 
	 * 根据hostName,requestRui查找规则列表
	 * 
	 * @param hostName
	 * @param requestUri
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	private String __generateFindMockRuleCommand(String hostName, String requestUri, int pageNumber, int pageSize) {

		// String generatedCommand =
		// String.format("{find:'mockrules',filter:{host:{},uri:'{}'}}",
		// hostName == null ? "null" : "'" + hostName + "'", requestUri);
		//

		Document findCommand = new Document();
		Document findFilter = new Document();
		findCommand.put("find", "mockrules");
		if (!StringUtils.isNullOrEmpty(hostName) && !hostName.equals("All"))
			findFilter.put("host", hostName);

		if (!StringUtils.isNullOrEmpty(requestUri))
			findFilter.put("uri", requestUri);

		findCommand.put("filter", findFilter);
		findCommand.put("skip", (pageNumber - 1) * pageSize);
		findCommand.put("limit", pageSize);
		log.info("生成的Find命令:{}", findCommand.toJson());
		return findCommand.toJson();
	}

}
