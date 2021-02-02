package com.hissummer.mockserver.mgmt.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hissummer.mockserver.mgmt.service.jpa.EurekaMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.vo.Constants;
import com.hissummer.mockserver.mgmt.vo.EurekaMockRule;
import com.netflix.appinfo.DataCenterInfo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class EurekaMockRuleServiceImpl {

	@Autowired
	EurekaMockRuleMongoRepository eurekaMockRepository;

	private String credentials = Credentials.basic("user", "pass");

	public boolean register(EurekaMockRule rule) {

		/*
		 * /eureka/apps/appID
		 * 
		 * Input: JSON/XML payload HTTP Code: 204 on success
		 * 
		 * 
		 */

		String registerInfoStringFormatter = "{" + "	\"instance\": {" + "		\"instanceId\": \"%s\","
				+ "		\"app\": \"%s\"," + "		\"appGroutName\": null," + "		\"ipAddr\": \"%s\","
				+ "		\"sid\": \"na\"," + "		\"homePageUrl\": null," + "		\"statusPageUrl\": \"%s\","
				+ "		\"healthCheckUrl\": null," + "		\"secureHealthCheckUrl\": null,"
				+ "		\"vipAddress\": \"%s\"," + "		\"secureVipAddress\": \"%s\"," + "		\"countryId\": 1,"
				+ "		\"dataCenterInfo\": {"
				+ "			\"@class\": \"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\","
				+ "			\"name\": \"MyOwn\"" + "		}," + "		\"hostName\": \"%s\","
				+ "		\"status\": \"UP\"," + "		\"leaseInfo\": {\"evictionDurationInSecs\":180},"
				+ "		\"isCoordinatingDiscoveryServer\": false," + "		\"lastUpdatedTimestamp\": null,"
				+ "		\"lastDirtyTimestamp\": null," + "		\"actionType\": null," + "		\"asgName\": null,"
				+ "		\"overridden_status\": \"UNKNOWN\"," + "		\"port\": {" + "			\"$\": %s,"
				+ "			\"@enabled\": \"true\"" + "		}," + "		\"securePort\": {" + "			\"$\": %s,"
				+ "			\"@enabled\": \"false\"" + "		}," + "		\"metadata\": {"
				+ "			\"@class\": \"java.util.Collections$EmptyMap\"" + "		}" + "	}" + "}" + "";
		String jsonstr = String.format(registerInfoStringFormatter, rule.getHostName() + ":" + rule.getPort(),
				rule.getServiceName(), rule.getHostName(),
				Constants.HTTP + rule.getHostName() + ":" + rule.getPort() + "/status", rule.getServiceName(),
				rule.getServiceName(), rule.getHostName(), rule.getPort(), rule.getPort());

		RequestBody okHttpRequestBody = null;

		Response response = null;

		okHttpRequestBody = RequestBody.create(jsonstr, MediaType.parse("application/json"));
		log.info("requestBody: {}", jsonstr);

		final OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url(Constants.HTTP + rule.getEurekaServer() + Constants.EUREKA_URI_BASE_PATH + rule.getServiceName())
				.method("POST", okHttpRequestBody).header(HttpHeaders.AUTHORIZATION, credentials)
				.header("Content-Type", "application/json").build();

		Call call = client.newCall(request);
		try {
			response = call.execute();
			log.info(response.toString());
			log.info(JSON.toJSONString(response.body()));
			return response.isSuccessful();

		} catch (IOException e) {
			log.warn(e.toString());
		} finally {
			if (response != null) {
				response.close();
			}
		}

		return false;

	}

	public boolean heartBeat(EurekaMockRule rule) {
		/*
		 * /eureka/apps/appID/instanceID
		 * 
		 * HTTP Code: 200 on success 404 if instanceID doesn’t exist
		 * 
		 */

		final OkHttpClient client = new OkHttpClient();

		Response response = null;

		Request request = new Request.Builder()
				.url(Constants.HTTP + rule.getEurekaServer() + Constants.EUREKA_URI_BASE_PATH + rule.getServiceName()
						+ "/" + rule.getHostName() + ":" + rule.getPort())
				.header(HttpHeaders.AUTHORIZATION, credentials)
				.method("PUT", RequestBody.create("", MediaType.parse("application/json"))).build();

		Call call = client.newCall(request);
		try {
			response = call.execute();
			log.info(response.toString());
			return response.isSuccessful();

		} catch (IOException e) {
			log.warn(e.toString());
		} finally {
			if (response != null) {
				response.close();
			}
		}

		return false;
	}

	public boolean unRegisterApp(EurekaMockRule rule) {

		final OkHttpClient client = new OkHttpClient();

		Response response = null;

		Request request = new Request.Builder()
				.url("http://" + rule.getEurekaServer() + Constants.EUREKA_URI_BASE_PATH + rule.getServiceName() + "/"
						+ rule.getHostName() + ":" + rule.getPort())
				.header(HttpHeaders.AUTHORIZATION, credentials)
				.method("DELETE", RequestBody.create("", MediaType.parse("application/json"))).build();

		Call call = client.newCall(request);
		try {
			response = call.execute();
			log.info(response.toString());
			return response.isSuccessful();

		} catch (IOException e) {

			log.warn(e.toString());
		} finally {
			if (response != null) {
				response.close();
			}
		}

		return false;

	}

	public void heartBeatAllRules() {

		List<EurekaMockRule> rules = null;
		ExecutorService threadPool = Executors.newFixedThreadPool(20);

		while (true) {

			rules = eurekaMockRepository.findByEnable(Boolean.TRUE);

			if (rules != null) {

				for (EurekaMockRule rule : rules) {
					threadPool.execute(() -> {

						log.info("{} heart beat to {} ", rule.getServiceName(), rule.getEurekaServer());

						if (!heartBeat(rule)) {
							register(rule);
						}

					});
				}

			}

			try {
				TimeUnit.SECONDS.sleep(30);
			} catch (InterruptedException e) {
				log.warn("heartbeat sleep interrupted: {}", e);
				shutdownAndAwaitTermination(threadPool);
				Thread.currentThread().interrupt();
				throw new RuntimeException("interrupted");

			}

		}

	}

	void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown();
		// Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow();
				// Cancel currently executing tasks

				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					log.error("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	public static void main(String[] args) throws JsonProcessingException {

		/*
		 * LeaseInfo lease =
		 * LeaseInfo.Builder.newBuilder().setRegistrationTimestamp(System.
		 * currentTimeMillis()).build();
		 * 
		 * 
		 * InstanceInfo registerInfo =
		 * InstanceInfo.Builder.newBuilder().setAppName("11080").setHostName("aaa")
		 * .setStatusPageUrl("/status", Constants.HTTP + "abcd.com" + ":" + "11080" +
		 * "/status").setSecureVIPAddress("")
		 * .setIPAddr("").setVIPAddress("").setPort(Integer.valueOf("11080"))
		 * .setSecurePort(Integer.valueOf("11080")).enablePort(InstanceInfo.PortType.
		 * UNSECURE, true)
		 * .setActionType(ActionType.ADDED).setOverriddenStatus(InstanceStatus.UNKNOWN).
		 * setCountryId(1) .enablePort(InstanceInfo.PortType.SECURE,
		 * false).setHostName("11080") .setInstanceId("11080" + ":" +
		 * "11080").setStatus(InstanceStatus.UP).setLeaseInfo(lease).build();
		 * 
		 * log.info("port is {}", registerInfo.getPort());
		 * 
		 * ObjectMapper mapperObj = new ObjectMapper();
		 * 
		 * Map<String, Object> instanceInfoRequest = new HashMap<String, Object>();
		 * 
		 * instanceInfoRequest.put("instance", registerInfo);
		 * 
		 * log.info("requestBody: {}",
		 * mapperObj.writeValueAsString(instanceInfoRequest));
		 * 
		 * mapperObj.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		 * log.info("requestBody: {}", mapperObj.writeValueAsString(registerInfo));
		 */
	}

}
