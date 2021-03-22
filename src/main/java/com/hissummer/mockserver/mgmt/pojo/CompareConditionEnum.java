package com.hissummer.mockserver.mgmt.pojo;

import java.util.EnumSet;

public enum CompareConditionEnum {
	EQUAL("equal"), NON_EQUAL("non_equal"), REGREX_MATCH("regrex_match"), GREATER_THAN(
			"greater_than"), GREATER_OR_EQUAL("greater_than_or_equal"), LESS_THAN("less_than"), LESS_OR_EQUAL(
					"less_than_or_equal"), LEFT_PARENTHESIS("("), RIGHT_PARENTHESIS(")"), OR("or"), AND("and");

	private String presentText = "";

	private CompareConditionEnum(String presentText) {
		this.presentText = presentText;
	}

	@Override
	public String toString() {
		return presentText;
	}

	public String test() {

		return null;
	}

}
