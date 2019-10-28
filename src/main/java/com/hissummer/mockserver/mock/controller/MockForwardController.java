package com.hissummer.mockserver.mock.controller;

import java.lang.reflect.Field;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockserviceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")

@RestController
public class MockForwardController implements ErrorController {

	@Autowired
	MockserviceImpl mockservice;

	/**
	 * forward to the mocked rules or upstream.
	 * @param request : HttpServletRequest
	 * @param headers : request headers
	 * @param requestBody :  nullAble ( requestBody is null when request with HTTP get method)
	 * @param response 
	 * @return
	 */
	@RequestMapping(value = "/forward",produces={"application/json"})
	public Object forward(HttpServletRequest request, @RequestHeader Map<String, String> headers,
			@Nullable @RequestBody String requestBody, HttpServletResponse response) {


		log.info("headers: {}",JSON.toJSONString(headers));
		

		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String host = request.getServerName();
		
		log.info("{},{},{}",status,errorMessage,host);

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {

				// 404 errors
				ResponseFacade responsefacade = (ResponseFacade) response;

				try {

					// Here use reflect to modify return http status code 404 to 200.
					// More details please linke into https://blog.hissummer.com/2019/07/%E5%88%A9%E7%94%A8springboot%E5%AE%9E%E7%8E%B0http-mock-%E6%9C%8D%E5%8A%A1/
					Field innerResponse = getField(responsefacade.getClass(), "response");
					innerResponse.setAccessible(true);
					Response innterResponseObject = (Response) innerResponse.get(responsefacade);
					org.apache.coyote.Response coyoteResponse = innterResponseObject.getCoyoteResponse();
					Field httpstatus = getField(coyoteResponse.getClass(), "status");
					httpstatus.setAccessible(true);
					httpstatus.set(coyoteResponse, 200);
					
					Field httpContentType = getField(coyoteResponse.getClass(), "contentType");
					httpContentType.setAccessible(true);
					httpContentType.set(coyoteResponse, "application/json");					
					
					// This code should not throw exception.
					
				} catch (NoSuchFieldException e) {
					log.info(e.getMessage());
				} catch (IllegalArgumentException e) {
					log.info(e.getMessage());
				} catch (IllegalAccessException e) {
					log.info(e.getMessage());
				} catch (Exception e) {
					log.info(e.getMessage());
				}

				return mockservice.getResponse(headers, host, request.getMethod(),
						(String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI), requestBody);

			} else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				// 5xx errors
				return MockRuleMgmtResponseVo.builder().status(0).success(false).message(errorMessage)
						.build().toString();
			}
		}
		//if statusCode is not 404 or 500 errors , will return default response.
		return MockRuleMgmtResponseVo.builder().status(0).success(false).message(errorMessage)
				.build().toString();
	}

	/**
	 * This is not used actually. "server.error.path=/forward" in application.properties will define the error path to be the "/forward".
	 */
	@Override
	public String getErrorPath() {
		log.info("get error path");
		return "/forward";
	}

	/**
	 * Get java object field by the field Name.
	 * @param clazz
	 * @param fieldName
	 * @return Field
	 * @throws NoSuchFieldException
	 */
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