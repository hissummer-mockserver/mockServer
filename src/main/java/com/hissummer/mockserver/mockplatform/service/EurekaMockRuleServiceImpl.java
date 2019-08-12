package com.hissummer.mockserver.mockplatform.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.hissummer.mockserver.mockplatform.EurekaMockRule;
import com.netflix.appinfo.InstanceInfo;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
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

	public boolean register(EurekaMockRule rule) {

		/*
		 * /eureka/v2/apps/appID
		 * 
		 * Input: JSON/XML payload HTTP Code: 204 on success
		 * 
		 */
		InstanceInfo registerInfo = InstanceInfo.Builder.newBuilder().setAppName(rule.getServiceName())
				.setInstanceId(rule.getHostName() + ":" + rule.getPort()).build();

		RequestBody okHttpRequestBody = null;
		if (registerInfo != null) {
			okHttpRequestBody = RequestBody.create(JSON.toJSONString(registerInfo),
					MediaType.parse("application/json"));
		}

		final OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
				.url("http://" + rule.getEurekaServer() + "/eureka/v2/apps/" + rule.getServiceName())
				.method("PUT", okHttpRequestBody).addHeader("Content-Type", "application/json").build();

		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			log.info(response.toString());
			return response.isSuccessful();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	public boolean heartBeat(EurekaMockRule rule) {
		/*
		 * /eureka/v2/apps/appID/instanceID
		 * 
		 * HTTP Code: 200 on success 404 if instanceID doesn’t exist
		 * 
		 */

		final OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder().url("http://" + rule.getEurekaServer() + "/eureka/v2/apps/"
				+ rule.getServiceName() + "/" + rule.getHostName() + ":" + rule.getPort()).method("PUT", null).build();

		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			log.info(response.toString());
			return response.isSuccessful();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
						log.info(e.getMessage());
					}
				});
			}

		}
	}

}
