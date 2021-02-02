package com.hissummer.mockserver.mgmt.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CheckLoginInterceptor implements HandlerInterceptor {

	@Autowired
	UserService userService;

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		log.info(request.getRequestURI());

		if (request.getMethod().equals(HttpMethod.OPTIONS.name()))
			return true;

		boolean loginCheck[] = { false };

		List<Cookie> cookies = request.getCookies() == null ? Collections.emptyList()
				: Arrays.asList(request.getCookies());

		cookies.forEach(cookie -> {

			if (cookie.getName().equals("mu")) {
				loginCheck[0] = userService.isUserLoginWithMuId(cookie.getValue());
			}

		});

		if (!loginCheck[0]) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		}

		return loginCheck[0];
	}

}
