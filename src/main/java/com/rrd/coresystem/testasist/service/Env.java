package com.rrd.coresystem.testasist.service;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Env {

	public Map<String, String[]> serviceDbConf = new HashMap<String, String[]>();

	public Map<String, String> eurekaServer = new HashMap<String, String>();

}
