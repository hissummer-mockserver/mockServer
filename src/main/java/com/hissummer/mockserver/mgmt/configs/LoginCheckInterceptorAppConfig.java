package com.hissummer.mockserver.mgmt.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hissummer.mockserver.mgmt.service.CheckLoginInterceptor;

@Component
public class LoginCheckInterceptorAppConfig implements WebMvcConfigurer {

	@Autowired
	CheckLoginInterceptor checkLoginInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(checkLoginInterceptor).addPathPatterns("/api/mock/**")
				.excludePathPatterns("/api/mock/2.0/login", "/api/mock/2.0/logout");
	}
}
