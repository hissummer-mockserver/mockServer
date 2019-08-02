package com.rrd.coresystem.testasist.requestVo;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.ToString;

@Data
@JsonDeserialize
@ToString
public class SqlResponseVo {

	String status;
	int count;
	String message;
	JSONArray data;

	public SqlResponseVo() {

		status = "";
		count = 0;
		message = "";

	}

	public SqlResponseVo(String status, String message, JSONArray data) {

		this.status = status;
		this.count = 0;
		this.message = message;
		this.data = data;
	}

}
