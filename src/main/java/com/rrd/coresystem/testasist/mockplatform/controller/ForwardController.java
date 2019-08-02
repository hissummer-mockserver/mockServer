package com.rrd.coresystem.testasist.mockplatform.controller;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rrd.coresystem.testasist.mockplatform.MockPlatformHttpResponseBodyVo;
import com.rrd.coresystem.testasist.mockplatform.service.MockserviceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")

@RestController
public class ForwardController implements ErrorController {

	@Autowired
	MockserviceImpl mockservice;

	@RequestMapping(value = "/error")
	public String error(HttpServletRequest request, @RequestHeader Map<String, String> headers,
			HttpServletResponse response) {

		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
	    
		URL url = null;
		String host = null;
		try {
			url = new URL(request.getRequestURL().toString());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(url!=null)
	     host  = url.getHost();
	    

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {

				ResponseFacade responsefacade = (ResponseFacade) response;

				try {

					//https://blog.hissummer.com/2019/07/%E5%88%A9%E7%94%A8springboot%E5%AE%9E%E7%8E%B0http-mock-%E6%9C%8D%E5%8A%A1/
					Field innerResponse = getField(responsefacade.getClass(), "response");
					innerResponse.setAccessible(true);
					Response innterResponseObject = (Response) innerResponse.get(responsefacade);
					org.apache.coyote.Response coyoteResponse = innterResponseObject.getCoyoteResponse();

					Field httpstatus = getField(coyoteResponse.getClass(), "status");
					httpstatus.setAccessible(true);
					httpstatus.set(coyoteResponse, 200);

				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				return mockservice.getMockResponse(headers,host,
						(String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI));
				
				// return "{\"" +"404 error found!"+"\"}";
			} else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				return MockPlatformHttpResponseBodyVo.builder().status(0).success(false).message("http 5xx error.").build().toString();
			}
		}

		return MockPlatformHttpResponseBodyVo.builder().status(0).success(false).message("default error response.").build().toString();
	}

	@Override
	public String getErrorPath() {
		log.info("get error path");
		return "/error";
	}

	private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

}