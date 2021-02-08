package com.hissummer.mockserver.mgmt.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpstreamPolicy {

	private int weight;

}
