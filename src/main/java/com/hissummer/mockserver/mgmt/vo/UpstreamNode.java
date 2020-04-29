package com.hissummer.mockserver.mgmt.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpstreamNode {

	@Builder.Default
	private String protocol = "http";
	private String address;
	private String uri;
	private UpstreamPolicy upstreamPolicy;

}
