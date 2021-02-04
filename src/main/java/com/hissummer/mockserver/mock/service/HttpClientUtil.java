package com.hissummer.mockserver.mock.service;

import java.time.Duration;

import okhttp3.OkHttpClient;

public class HttpClientUtil {

	public static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(10))
			.readTimeout(Duration.ofSeconds(60)).build();

}
