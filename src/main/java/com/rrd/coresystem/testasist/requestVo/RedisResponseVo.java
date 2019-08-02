package com.rrd.coresystem.testasist.requestVo;

import com.alibaba.fastjson.JSONArray;

import lombok.Data;

@Data
public class RedisResponseVo {

	String status;
	int count;
	String message;
	JSONArray data;

	public RedisResponseVo() {
		status = "";
		count = 0;
		message = "";
	}

	public RedisResponseVo(String status, String message, JSONArray data) {

		this.status = status;
		this.count = 0;
		this.message = message;
		this.data = data;
	}

}
