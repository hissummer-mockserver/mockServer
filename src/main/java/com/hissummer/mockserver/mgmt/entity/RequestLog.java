package com.hissummer.mockserver.mgmt.entity;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "requestlogs")
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RequestLog {

	private String id;
	private String hittedMockRuleHostName;
	/**
	 * uri of the mockRule
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

	@Indexed(name="createTime", expireAfterSeconds=86400)
	private Date createTime;
	@Builder.Default
	private boolean isMock = true;

}
