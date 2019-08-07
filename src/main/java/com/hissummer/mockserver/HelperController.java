package com.hissummer.mockserver;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
public class HelperController  {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationContext appContext;

	

	@RequestMapping(value = "/beans", produces = { "application/json" })	
	public String beans() {


		String[] beanNames = appContext.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			log.info("beanName:{} ",beanName);
		}

		return "[\"" + String.join("\",\"", beanNames) + "\"]";
	}
	

}