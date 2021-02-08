package com.hissummer.mockserver.mgmt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mgmt.entity.RequestLog;
import com.hissummer.mockserver.mgmt.service.jpa.RequestLogMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestLogMessageReceiver {

	@Autowired
	RequestLogMongoRepository requestLogMongoRepository;

	@JmsListener(destination = "requestlog", containerFactory = "jmsListenerFactory")
	public void receiveMessage(RequestLog requestLog) {
		requestLogMongoRepository.save(requestLog);
		log.info("request log saved ok!");
	}

}
