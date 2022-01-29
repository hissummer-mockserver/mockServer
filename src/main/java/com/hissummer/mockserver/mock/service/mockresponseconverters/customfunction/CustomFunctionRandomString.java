package com.hissummer.mockserver.mock.service.mockresponseconverters.customfunction;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component("CustomFunctionRandomString")
public class CustomFunctionRandomString implements CustomFunctionInterface {
	private Random rand = new Random();
	public String execute(String[] args) {

		if (args.length == 1)
			return this.randomString(args[0]);

		if (args.length == 2)
			return this.randomString(args[0], args[1]);

		return null;

	}

	private String randomString(String lengthStr, String charactors) {
		int length;

		try {
			length = Integer.parseInt(lengthStr);

		} catch (NumberFormatException | NullPointerException nfe) {

			return null;

		}

		String SALTCHARS = charactors;
		StringBuilder salt = new StringBuilder();
		while (salt.length() < length) { // length of the random string.
			int index = (int) (this.rand.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

	private String randomString(String lengthStr) {
		int length;

		try {
			length = Integer.parseInt(lengthStr);

		} catch (NumberFormatException | NullPointerException nfe) {

			return null;

		}

		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		while (salt.length() < length) { // length of the random string.
			int index = (int) (this.rand.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

}
