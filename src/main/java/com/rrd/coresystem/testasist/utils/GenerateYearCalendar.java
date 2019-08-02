package com.rrd.coresystem.testasist.utils;

import java.util.Calendar;

public class GenerateYearCalendar {

	public static void main(String[] args) {

		Calendar cal = Calendar.getInstance();
		// cal.setTime(date);
		cal.set(Calendar.YEAR, 2019);
		cal.set(Calendar.DAY_OF_YEAR, 1);

		int i = 0;
		while (i <= 365) {

			if (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7) {

				System.out.println(cal.get(Calendar.YEAR) + "-" + String.format("%02d", cal.get(Calendar.MONTH) + 1)
						+ "-" + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + " 节假日");

			}

			cal.add(Calendar.DAY_OF_YEAR, 1);
			i++;

		}

	}

}
