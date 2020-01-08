package com.hissummer.mockserver.mock.service.mockresponseconverters.customfunction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component("CustomFunctionNowDate")
public class CustomFunctionNowDate implements CustomFunctionInterface {

	public String execute(String[] args) {

		if (args.length == 0)
			return this.nowDate();

		if (args.length == 1)
			return this.nowDate(args[0]);

		return null;

	}

	private String nowDate() {

		return String.valueOf(System.currentTimeMillis());
	}

	private String nowDate(String specifyDateTime) {

		try {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime localDateTime = LocalDateTime.parse(specifyDateTime, formatter);

			return String.valueOf(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		} catch (Exception e) {

			return null;
		}

	}

}
