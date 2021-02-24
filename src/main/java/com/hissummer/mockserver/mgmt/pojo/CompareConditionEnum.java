package com.hissummer.mockserver.mgmt.pojo;

public enum CompareConditionEnum {
	EQUAL("equal"),
	NON_EQUAL("non equal"),
	REGREX_MATCH("regrex match"),
	GREATER_THAN("greater than"),
	GREATER_OR_EQUAL("greater than or equal"),
	LESS_THAN("less than"),
	LESS_OR_EQUAL("less than or equal");
	
	private String presentText="";
    private CompareConditionEnum(String presentText) {
        this.presentText = presentText;
    }
	
    @Override
    public String toString(){
        return presentText;
    }
}
