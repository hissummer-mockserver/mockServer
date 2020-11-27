package com.hissummer.mockserver.mgmt.vo;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "mockrules")
@Builder
public class HttpMockRule {

	private String id;

	@Builder.Default
	private String host = "*"; // if we do not specify hostName , host will be "*" , that mean will match all
								// hostName.
	private String uri;

	private String mockResponse;

	private Map<String, String> responseHeaders;
	private List<Plugin> plugins;
	private UpstreamGroup upstreams;

	@Builder.Default
	private HttpMockWorkMode workMode = HttpMockWorkMode.MOCK;

	@Builder.Default
	private Boolean enable = true;

}
