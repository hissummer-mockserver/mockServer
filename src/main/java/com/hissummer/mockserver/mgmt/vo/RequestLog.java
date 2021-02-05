package com.hissummer.mockserver.mgmt.vo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.MultiValueMap;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "requestlogs")
@Builder
public class RequestLog {

	private String id;
	private String hittedMockRuleHostName;
	/**
	 * uri of the mockrule 
	 */
	private String hittedMockRuleUri;
	/**
	 * actual request uri
	 */
	private String requestUri;
	@Builder.Default
	private String protocol = "http";
	private Map<String, String> requestHeaders;
	private String requestBody;
	private Map<String, String> responseHeaders;
	private String responseBody;
	private String upstreamRequestUri;
	private Map<String, String> upstreamRequestHeaders;
	private String upstreamRequestBody;

	private Date createTime;
	@Builder.Default
	private boolean isMock = true;

}
