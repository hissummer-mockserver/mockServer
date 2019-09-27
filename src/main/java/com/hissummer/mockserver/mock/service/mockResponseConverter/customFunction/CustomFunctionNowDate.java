package com.hissummer.mockserver.mock.service.mockResponseConverter.customFunction;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component("CustomFunctionNowDate")
public class CustomFunctionNowDate implements CustomFunctionInterface {

	public String execute(String[] args) {

		if(args.length == 0)
			 return this.nowDate();
		
		if (args.length == 1)
			return this.nowDate(args[0]);

		if (args.length == 2)
			return this.nowDate(args[0], args[1]);

		return null;

	}

	private String nowDate() {
		
		return String.valueOf(System.currentTimeMillis());
	}

	private String nowDate(String lengthStr, String charactors) {


		return null;

	}

	private String nowDate(String lengthStr) {

      return null;

	}

}
