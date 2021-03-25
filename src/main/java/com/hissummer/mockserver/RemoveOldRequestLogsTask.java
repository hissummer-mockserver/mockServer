package com.hissummer.mockserver;

import java.util.Calendar;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hissummer.mockserver.mgmt.service.jpa.RequestLogMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RemoveOldRequestLogsTask extends TimerTask {

	@Autowired
	RequestLogMongoRepository requestLogMongoRepository;

	@Override
	public void run() {
		removeOldRequestLogs();
	}

	public void removeOldRequestLogs() {

		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, -7);
		//now.add(Calendar.MINUTE, -1);
		requestLogMongoRepository.deleteByCreateTimeLessThan(now.getTime());
		log.info("removed old request logs!");
	}

}
