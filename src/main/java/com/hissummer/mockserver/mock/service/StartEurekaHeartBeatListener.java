package com.hissummer.mockserver.mock.service;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mgmt.service.EurekaMockRuleServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class StartEurekaHeartBeatListener {

	@Autowired
	EurekaMockRuleServiceImpl eurekaMockRuleService;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		// start a new thread to run the eucreka heart beat background service
		// if we don't start a new thread, the main thread will running looply, will
		// prevent the test case be testing.
		log.info("start execute eureka rules after application ready!");
		new Thread(() -> eurekaMockRuleService.heartBeatAllRules()).start();

	}
}
