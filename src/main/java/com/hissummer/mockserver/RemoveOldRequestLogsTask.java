package com.hissummer.mockserver;

import java.util.Calendar;
import java.util.Timer;
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
	
	private final  Timer timer = new Timer(true);; 

	@Override
	public void run() {
		removeOldRequestLogs();
	}

	public void removeOldRequestLogs() {

		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, -1);
		// now.add(Calendar.MINUTE, -1);
		requestLogMongoRepository.deleteByCreateTimeLessThan(now.getTime());
		log.info("removed old request logs!");
		
		Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);		
		//timer.schedule(this, calendar.getTime());
	}

}
