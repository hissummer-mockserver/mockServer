package com.rrd.coresystem.testasist.service;

import java.util.HashMap;
import java.util.Map;

public class EnvConfig {

	static final Map<String, Env> envMap = createMap();

	private static Map<String, Env> createMap() {
		Map<String, Env> myMap = new HashMap<String, Env>();
		Env env_150 = new Env();
		env_150.serviceDbConf.put("proxy",
				new String[] { "172.16.2.112", "account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_150.serviceDbConf.put("account",
				new String[] { "172.16.2.112", "account", "heika_dev", "heika_dev@qwe321" });
		env_150.serviceDbConf.put("finance",
				new String[] { "172.16.2.112", "finance", "heika_dev", "heika_dev@qwe321" });
		env_150.serviceDbConf.put("pay", new String[] { "172.16.1.77", "pay", "pay-dev", "pay-dev" });
		env_150.serviceDbConf.put("redis", new String[] { "172.16.2.64", "8001|8002|8003|8004|8005|8006" });
		env_150.serviceDbConf.put("verify", new String[] { "172.16.2.112", "verify", "heika_dev", "heika_dev@qwe321" });
		env_150.serviceDbConf.put("fund", new String[] { "172.16.2.112", "ibg_fund", "heika_dev", "heika_dev@qwe321" });
		env_150.eurekaServer.put("eureka", "core-eureka-server-test-env-150.test.rrdbg.com");

		myMap.put("env_150", env_150);

		Env env_151 = new Env();
		env_151.serviceDbConf.put("proxy",
				new String[] { "172.16.2.112", "account_proxy_other", "heika_dev", "heika_dev@qwe321" });
		env_151.serviceDbConf.put("account",
				new String[] { "172.16.2.112", "account_other", "heika_dev", "heika_dev@qwe321" });
		env_151.serviceDbConf.put("finance",
				new String[] { "172.16.2.112", "finance_other", "heika_dev", "heika_dev@qwe321" });
		env_151.serviceDbConf.put("pay", new String[] { "172.16.2.112", "pay_other", "heika_dev", "heika_dev@qwe321" });
		env_151.serviceDbConf.put("redis", new String[] { "172.16.2.63", "8001|8002|8003|8004|8005|8006" });

		env_151.serviceDbConf.put("fund",
				new String[] { "172.16.2.112", "fund_other", "heika_dev", "heika_dev@qwe321" });
		env_151.eurekaServer.put("eureka", "core-eureka-server-test-env-151.test.rrdbg.com");

		myMap.put("env_151", env_151);

		Env env_158 = new Env();
		env_158.serviceDbConf.put("proxy",
				new String[] { "172.16.2.158", "account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_158.serviceDbConf.put("account",
				new String[] { "172.16.2.158", "account", "heika_dev", "heika_dev@qwe321" });
		env_158.serviceDbConf.put("finance",
				new String[] { "172.16.2.158", "finance", "heika_dev", "heika_dev@qwe321" });
		env_158.serviceDbConf.put("pay", new String[] { "172.16.2.158", "pay", "heika_dev", "heika_dev@qwe321" });
		env_158.serviceDbConf.put("redis", new String[] { "172.16.2.65", "7001|7002|7003|7004|7005|7006" });
		env_158.eurekaServer.put("eureka", "core-eureka-server-test-env-158.test.rrdbg.com");

		myMap.put("env_158", env_158);

		Env env_164 = new Env();
		env_164.serviceDbConf.put("proxy",
				new String[] { "172.16.2.164", "account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_164.serviceDbConf.put("account",
				new String[] { "172.16.2.164", "account", "heika_dev", "heika_dev@qwe321" });
		env_164.serviceDbConf.put("finance",
				new String[] { "172.16.2.164", "finance", "heika_dev", "heika_dev@qwe321" });
		env_164.serviceDbConf.put("pay", new String[] { "172.16.2.164", "pay", "heika_dev", "heika_dev@qwe321" });
		env_164.serviceDbConf.put("redis", new String[] { "172.16.2.39", "6001|6002|6003|6004|6005|6000" });
		env_164.eurekaServer.put("eureka", "core-eureka-server-test-env-164.test.rrdbg.com");

		myMap.put("env_164", env_164);

		Env env_165 = new Env();
		env_165.serviceDbConf.put("proxy",
				new String[] { "172.16.2.165", "account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_165.serviceDbConf.put("account",
				new String[] { "172.16.2.165", "account", "heika_dev", "heika_dev@qwe321" });
		env_165.serviceDbConf.put("finance",
				new String[] { "172.16.2.165", "finance", "heika_dev", "heika_dev@qwe321" });
		env_165.serviceDbConf.put("pay", new String[] { "172.16.2.165", "pay", "heika_dev", "heika_dev@qwe321" });
		env_165.serviceDbConf.put("redis", new String[] { "172.16.2.64", "7001|7002|7003|7004|7005|7006" });
		env_165.eurekaServer.put("eureka", "core-eureka-server-test-env-165.test.rrdbg.com");

		myMap.put("env_165", env_165);

		Env env_39 = new Env();
		env_39.serviceDbConf.put("proxy",
				new String[] { "172.16.2.39", "account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_39.serviceDbConf.put("account", new String[] { "172.16.2.39", "account", "heika_dev", "heika_dev@qwe321" });
		env_39.serviceDbConf.put("finance", new String[] { "172.16.2.39", "finance", "heika_dev", "heika_dev@qwe321" });
		env_39.serviceDbConf.put("pay", new String[] { "172.16.2.39", "pay", "heika_dev", "heika_dev@qwe321" });
		env_39.serviceDbConf.put("redis", new String[] { "172.16.2.39", "7000|7001|7002" });
		env_39.eurekaServer.put("eureka", "core-eureka-server-test-env-39.test.rrdbg.com");

		myMap.put("env_39", env_39);

		Env env_combined = new Env();
		env_combined.serviceDbConf.put("proxy",
				new String[] { "172.16.2.39", "env_combined_account_proxy", "heika_dev", "heika_dev@qwe321" });
		env_combined.serviceDbConf.put("account",
				new String[] { "172.16.2.39", "env_combined_account", "heika_dev", "heika_dev@qwe321" });
		env_combined.serviceDbConf.put("finance",
				new String[] { "172.16.2.39", "env_combined_finance", "heika_dev", "heika_dev@qwe321" });
		env_combined.serviceDbConf.put("pay",
				new String[] { "172.16.2.39", "env_combined_pay", "heika_dev", "heika_dev@qwe321" });
		env_combined.serviceDbConf.put("redis", new String[] { "172.16.2.39", "6480" });
		env_combined.eurekaServer.put("eureka", "core-eureka-server-test-env-cmb.test.rrdbg.com");
		myMap.put("env_combined", env_combined);

		return myMap;
	}

}
