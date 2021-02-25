package com.hissummer.mockserver.mgmt.entity;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.hissummer.mockserver.mgmt.pojo.HttpCondition;
import com.hissummer.mockserver.mgmt.pojo.HttpMockWorkMode;
import com.hissummer.mockserver.mgmt.pojo.Plugin;
import com.hissummer.mockserver.mgmt.pojo.UpstreamGroup;

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
	@Builder.Default
	private String protocol = "http";
	private String mockResponse;

	private Map<String, String> responseHeaders;
	private List<Plugin> plugins;
	private UpstreamGroup upstreams;

	@Builder.Default
	private HttpMockWorkMode workMode = HttpMockWorkMode.MOCK;

	@Builder.Default
	private Boolean enable = true;

	private String category;

}
