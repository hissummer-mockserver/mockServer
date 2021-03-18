package com.hissummer.mockserver.mgmt.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompareCondition {
	private String toBeCompareValue;
	private CompareConditionEnum compareCondition;
	private String conditionValue;
}
