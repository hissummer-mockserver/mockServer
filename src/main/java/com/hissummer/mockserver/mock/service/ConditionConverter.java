package com.hissummer.mockserver.mock.service;

import com.hissummer.mockserver.mgmt.pojo.CompareConditionEnum;

/*
 * 	private String toBeCompareValue;
	private CompareConditionEnum compareCondition;
	private String conditionValue;
 */
public class ConditionConverter {
	
	final static private String VALIDINTORFLOAT = "[-+]?[0-9]+\\\\.?[0-9]*";
	
	public String converToGroovyExpression(String toBeCompareValue, CompareConditionEnum compareCondition,String conditionValue) {
		
		String convertResult = "";
		
		switch(compareCondition)
		{
		case  LEFT_PARENTHESIS:			
			convertResult = "(";
			break;
		case  RIGHT_PARENTHESIS:	
			convertResult = ")";
			break;
		case  GREATER_THAN:	
			convertResult = toBeCompareValue+" > "+conditionValue;
			break;			
		case  GREATER_OR_EQUAL:	
			convertResult = toBeCompareValue+" >= "+conditionValue;
			break;
		case  LESS_THAN:	
			convertResult = toBeCompareValue+" < "+conditionValue;
			break;			
		case  LESS_OR_EQUAL:	
			convertResult = toBeCompareValue+" <= "+conditionValue;
			break;		
			
		case  EQUAL:	
			if( conditionValue.matches(VALIDINTORFLOAT) &&  toBeCompareValue.matches(VALIDINTORFLOAT))
			{
				convertResult = ""+toBeCompareValue+".equals("+conditionValue+")";
			}
			else convertResult = "\""+toBeCompareValue+"\".equals(\""+conditionValue+"\")";
			break;		

		case  NON_EQUAL:	
			if( conditionValue.matches(VALIDINTORFLOAT) &&  toBeCompareValue.matches(VALIDINTORFLOAT))
			{
				convertResult = "!"+toBeCompareValue+".equals("+conditionValue+")";
			}
			else convertResult = "!\""+toBeCompareValue+"\".equals(\""+conditionValue+"\")";
			break;		

		case  REGREX_MATCH:	
			convertResult = "\""+toBeCompareValue+"\".matches(\""+conditionValue+"\")";			
			break;		
		case AND:
			convertResult = " && ";			
			break;
			
		case OR:
			convertResult = " || ";			
			break;
			
		default :
				convertResult = "";	
				break;
		}		

		return convertResult;
	}
	
	
	
	
	
	
}
