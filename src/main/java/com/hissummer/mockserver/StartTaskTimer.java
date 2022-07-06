package com.hissummer.mockserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Timer;

@Component
public class StartTaskTimer {

	@Autowired
	RemoveOldRequestLogsTask taskMonitorTimerTask;

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() {

		Calendar calendar = Calendar.getInstance();

		if (calendar.get(Calendar.HOUR_OF_DAY) >= 9) {
			calendar.add(Calendar.DATE, 1);
		}

		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		// running timer task as daemon thread
		Timer timer = new Timer(true);
		// timer.schedule(taskMonitorTimerTask, calendar.getTime(),
		// TimeUnit.MINUTES.toMillis(5));
		timer.schedule(taskMonitorTimerTask, calendar.getTime());

	}
}
