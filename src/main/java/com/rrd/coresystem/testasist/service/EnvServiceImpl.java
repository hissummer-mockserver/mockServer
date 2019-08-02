package com.rrd.coresystem.testasist.service;

import org.springframework.stereotype.Service;

@Service
public class EnvServiceImpl {

	public Env getDetailByEnvName(String env) {

		return EnvConfig.envMap.get(env);

	}

	public String getEurekaServerByEnvName(String env) {
		// TODO Auto-generated method stub

		return EnvConfig.envMap.get(env).getEurekaServer().get("eureka");

	}

}
