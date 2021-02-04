package com.hissummer.mockserver.mgmt.vo;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpstreamGroup {

	private List<UpstreamNode> nodes;

	@Builder.Default
	private int tryCount = 0;

	@Builder.Default
	private Long connectionTimeOut = 10000L;

	@Builder.Default
	private Long writeTimeOut = 60000L;

	@Builder.Default
	private Long readTimeOut = 60000L;

}
