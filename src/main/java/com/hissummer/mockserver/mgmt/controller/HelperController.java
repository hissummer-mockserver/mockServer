package com.hissummer.mockserver.mgmt.controller;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated (debugonly)
 */
@Slf4j
@CrossOrigin(origins = "*")
//@RequestMapping("/help")
@RestController
@Deprecated
public class HelperController {

	@Autowired
	private ApplicationContext appContext;

	//@GetMapping(value = "/beans", produces = { "application/json" })
	public String beans() {

		String[] beanNames = appContext.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			log.info("beanName:{} ", beanName);
		}

		return "[\"" + String.join("\",\"", beanNames) + "\"]";
	}

	//@GetMapping(value = "/properties", produces = { "application/json" })
	public String properties() {

		final Environment env = appContext.getEnvironment();
		log.info("====== Environment and configuration ======");
		log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));

		JSONObject pSources = new JSONObject();

		final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

		StreamSupport.stream(sources.spliterator(), false).filter(ps -> ps instanceof EnumerablePropertySource)
				.forEach(ps -> {

					JSONObject properties = new JSONObject();
					for (String a : ((EnumerablePropertySource<?>) ps).getPropertyNames()) {

						properties.put(a, ps.getProperty(a));

					}
					pSources.put(ps.getName(), properties);

				});

		log.info("===========================================");

		return pSources.toJSONString();
	}
}