package com.hissummer.mockserver.mgmt.service;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hissummer.mockserver.mgmt.vo.EurekaMockRule;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.ActionType;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.LeaseInfo;

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

	public boolean register(EurekaMockRule rule) throws IOException {

		/*
		 * /eureka/apps/appID
		 * 
		 * Input: JSON/XML payload HTTP Code: 204 on success
		 * 
		 * 
		 */

		
		String registerInfoStringFormatter  = "{" + 
				"	\"instance\": {" + 
				"		\"instanceId\": \"%s\"," + 
				"		\"app\": \"%s\"," + 
				"		\"appGroutName\": null," + 
				"		\"ipAddr\": \"%s\"," + 
				"		\"sid\": \"na\"," + 
				"		\"homePageUrl\": null," + 
				"		\"statusPageUrl\": \"%s\"," + 
				"		\"healthCheckUrl\": null," + 
				"		\"secureHealthCheckUrl\": null," + 
				"		\"vipAddress\": \"%s\"," + 
				"		\"secureVipAddress\": \"%s\"," + 
				"		\"countryId\": 1," + 
				"		\"dataCenterInfo\": {" + 
				"			\"@class\": \"com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo\"," + 
				"			\"name\": \"MyOwn\"" + 
				"		}," + 
				"		\"hostName\": \"%s\"," + 
				"		\"status\": \"UP\"," + 
				"		\"leaseInfo\": null," + 
				"		\"isCoordinatingDiscoveryServer\": false," + 
				"		\"lastUpdatedTimestamp\": null," + 
				"		\"lastDirtyTimestamp\": null," + 
				"		\"actionType\": null," + 
				"		\"asgName\": null," + 
				"		\"overridden_status\": \"UNKNOWN\"," + 
				"		\"port\": {" + 
				"			\"$\": %s," + 
				"			\"@enabled\": \"true\"" + 
				"		}," + 
				"		\"securePort\": {" + 
				"			\"$\": %s," + 
				"			\"@enabled\": \"false\"" + 
				"		}," + 
				"		\"metadata\": {" + 
				"			\"@class\": \"java.util.Collections$EmptyMap\"" + 
				"		}" + 
				"	}" + 
				"}" + 
				"";
		String jsonstr=String.format(registerInfoStringFormatter, rule.getHostName() + ":" + rule.getPort(),rule.getServiceName()
				,rule.getHostName(),"http://" + rule.getHostName() + ":" + rule.getPort() + "/status",rule.getServiceName()
				,rule.getServiceName(),rule.getHostName(),rule.getPort(),rule.getPort()
				);
		
//		LeaseInfo lease = LeaseInfo.Builder.newBuilder().setRegistrationTimestamp(System.currentTimeMillis()).build();
//
//		DataCenterInfo centerInfo = new Myown();
//
//		InstanceInfo registerInfo = InstanceInfo.Builder.newBuilder().setAppName(rule.getServiceName())
//				.setHostName(rule.getHostName())
//				.setStatusPageUrl("/status", "http://" + rule.getHostName() + ":" + rule.getPort() + "/status")
//				.setSecureVIPAddress(rule.getHostName()).setIPAddr(rule.getHostName())
//				.setVIPAddress(rule.getServiceName()).setPort(Integer.valueOf(rule.getPort())).setSecurePort(Integer.valueOf(rule.getPort())).enablePort(InstanceInfo.PortType.UNSECURE, true)
//				.setActionType(ActionType.ADDED).setOverriddenStatus(InstanceStatus.UNKNOWN)
//				.setDataCenterInfo(centerInfo).setCountryId(1).enablePort(InstanceInfo.PortType.SECURE, false)
//				.setHostName(rule.getHostName()).setInstanceId(rule.getHostName() + ":" + rule.getPort())
//				.setStatus(InstanceStatus.UP).setLeaseInfo(lease).build();
//		
//		log.info("port is {}",registerInfo.getPort());
		
		RequestBody okHttpRequestBody = null;

//		ObjectMapper mapperObj = new ObjectMapper();
		
		Response response = null;

//		Map<String, Object> instanceInfoRequest = new HashMap();
//		
//		instanceInfoRequest.put("instance", registerInfo);
//
//		if (registerInfo != null) {
//			okHttpRequestBody = RequestBody.create(jsonstr,
//					MediaType.parse("application/json"));
//		}
		okHttpRequestBody = RequestBody.create(jsonstr,
				MediaType.parse("application/json"));
		log.info("requestBody: {}", jsonstr);

		log.info("content-length: {}", okHttpRequestBody.contentLength());

		final OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url("http://" + rule.getEurekaServer() + "/eureka/apps/" + rule.getServiceName())
				.method("POST", okHttpRequestBody).header(HttpHeaders.AUTHORIZATION, credentials)
				.header("Content-Type", "application/json").build();

		Call call = client.newCall(request);
		try {
			response = call.execute();
			log.info(response.toString());
			log.info(JSON.toJSONString(response.body()));
			return response.isSuccessful();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				.url("http://" + rule.getEurekaServer() + "/eureka/apps/" + rule.getServiceName() + "/"
						+ rule.getHostName() + ":" + rule.getPort())
				.header(HttpHeaders.AUTHORIZATION, credentials)
				.method("PUT", RequestBody.create("", MediaType.parse("application/json"))).build();

		Call call = client.newCall(request);
		try {
			response = call.execute();
			log.info(response.toString());
			return response.isSuccessful();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (response != null) {
				response.close();
			}
		}

		return false;
	}

	public void heartBeatAllRules() {

		List<EurekaMockRule> rules = null;

		rules = (List<EurekaMockRule>) eurekaMockRepository.findByEnable(Boolean.TRUE);

		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		if (rules != null) {
			for (EurekaMockRule rule : rules) {
				threadPool.execute(() -> {

					log.info("{} heart beat to {} ", rule.getServiceName(), rule.getEurekaServer());

					try {

						if (!heartBeat(rule)) {
							register(rule);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

		}
	}

	private class Myown implements DataCenterInfo {

		@Override
		public Name getName() {
			// TODO Auto-generated method stub
			return DataCenterInfo.Name.MyOwn;
		}
	}
	
	public static void main(String[] args) throws JsonProcessingException {
		LeaseInfo lease = LeaseInfo.Builder.newBuilder().setRegistrationTimestamp(System.currentTimeMillis()).build();



		InstanceInfo registerInfo = InstanceInfo.Builder.newBuilder().setAppName("11080")
				.setHostName("aaa")
				.setStatusPageUrl("/status", "http://" + "abcd.com" + ":" + "11080" + "/status")
				.setSecureVIPAddress("").setIPAddr("")
				.setVIPAddress("").setPort(Integer.valueOf("11080")).setSecurePort(Integer.valueOf("11080")).enablePort(InstanceInfo.PortType.UNSECURE, true)
				.setActionType(ActionType.ADDED).setOverriddenStatus(InstanceStatus.UNKNOWN)
				.setCountryId(1).enablePort(InstanceInfo.PortType.SECURE, false)
				.setHostName("11080").setInstanceId("11080" + ":" + "11080")
				.setStatus(InstanceStatus.UP).setLeaseInfo(lease).build();
		
		log.info("port is {}",registerInfo.getPort());
		


		ObjectMapper mapperObj = new ObjectMapper();
		

		Map<String, Object> instanceInfoRequest = new HashMap();
		
		instanceInfoRequest.put("instance", registerInfo);


		log.info("requestBody: {}", mapperObj.writeValueAsString(instanceInfoRequest));
		
		mapperObj.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		log.info("requestBody: {}", mapperObj.writeValueAsString(registerInfo));
		
	}

}
