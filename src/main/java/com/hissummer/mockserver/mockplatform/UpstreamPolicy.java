package com.hissummer.mockserver.mockplatform;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpstreamPolicy {

	private int weight;

}
