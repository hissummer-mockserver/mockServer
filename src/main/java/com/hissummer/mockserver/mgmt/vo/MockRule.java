package com.hissummer.mockserver.mgmt.vo;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;


@Data
@Document(collection = "mockrules")
@Builder
public class MockRule {

	
	private String id; 
	
	@Builder.Default
	private String host = "*"; // if we do not specify hostName , host will be "*" , that mean will match all
							// hostName.
	private String uri;
	@Builder.Default
	private String protocol = "http";
	private String mockResponse;
	private List<Plugin> plugins;
	private UpstreamGroup upstreamGroup;

	@Builder.Default
	private MockRuleWorkMode workMode = MockRuleWorkMode.MOCK;

	@Builder.Default
	private Boolean enable = true;
	
	

}
