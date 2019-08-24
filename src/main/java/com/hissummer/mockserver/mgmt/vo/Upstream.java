package com.hissummer.mockserver.mgmt.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Upstream {

	private String upstreamAddress;
	private UpstreamPolicy upstreamPolicy;

}
