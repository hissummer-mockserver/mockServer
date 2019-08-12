package com.hissummer.mockserver.mockplatform.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class StartEurekaHeartBeatListener {

	@Autowired
	EurekaMockRuleServiceImpl eurekaMockRuleService;
	
	
	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {
		
		while(true) {
		
			log.info("eureka heart beat.");
			eurekaMockRuleService.heartBeatAllRules();
			
			try {
				TimeUnit.MINUTES.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
}
