package com.hissummer.mockserver.mgmt.pojo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
