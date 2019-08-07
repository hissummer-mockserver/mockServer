package com.hissummer.mockserver.mockplatform;

import com.hissummer.mockserver.mockplatform.MockRule.MockRuleBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Upstream {

	private String upstreamAddress;
	private UpstreamPolicy upstreamPolicy;

}
