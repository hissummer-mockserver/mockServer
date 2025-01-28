package com.hissummer.mockserver.mgmt.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpstreamNode {

	@Builder.Default
	private String protocol = "http";
	private String address;
	private String uri;
	private UpstreamPolicy upstreamPolicy;

}
