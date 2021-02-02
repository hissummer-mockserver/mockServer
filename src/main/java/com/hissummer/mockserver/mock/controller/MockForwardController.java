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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hissummer.mockserver.mgmt.vo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockserviceImpl;
import com.hissummer.mockserver.mock.vo.MockResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")

@RestController
public class MockForwardController implements ErrorController {

	@Autowired
	MockserviceImpl mockservice;

	/**
	 * forward to the mocked rules or upstream.
	 * 
	 * @param request
	 *            : HttpServletRequest
	 * @param headers
	 *            : request headers
	 * @param requestBody
	 *            : nullAble ( requestBody is null when request with HTTP get
	 *            method)
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/forward")

	// we don't specify method for this @RequestMapping , because we would like
	// support all methods for mock service.

	public ResponseEntity<?> forward(HttpServletRequest request, @RequestHeader Map<String, String> headers,
			@Nullable @RequestBody String requestBody, final HttpServletResponse response) {

		log.info("headers: {}", JSON.toJSONString(headers));

		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String host = request.getServerName();

		log.info("{},{},{}", status, errorMessage, host);

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				try {
					/**
					 * <<<<<<< HEAD change http response code 404(notfound) to 200. ======= change
					 * http response code 404(notfound) to 200. >>>>>>> refs/remotes/origin/v0.0.4
					 */
					ResponseFacade responsefacade = (ResponseFacade) response;
					Field innerResponse = getField(responsefacade.getClass(), "response");
					innerResponse.setAccessible(true);
					Response innterResponseObject = (Response) innerResponse.get(responsefacade);
					org.apache.coyote.Response coyoteResponse = innterResponseObject.getCoyoteResponse();
					Field httpstatus = getField(coyoteResponse.getClass(), "status");
					httpstatus.setAccessible(true);
					httpstatus.set(coyoteResponse, 200);
				}

				catch (NoSuchFieldException e) {
					log.info(e.getMessage());
				} catch (IllegalArgumentException e) {
					log.info(e.getMessage());
				} catch (IllegalAccessException e) {
					log.info(e.getMessage());
				} catch (Exception e) {
					log.info(e.getMessage());
				}

				// 404 not found
				HttpHeaders responseHeaders = new HttpHeaders();
				MockResponse responseVo = mockservice.getResponse(headers, host, request.getMethod(),
						(String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI), requestBody);

				if (responseVo.getHeaders() != null) {

					responseVo.getHeaders().keySet().forEach(header -> {

						responseHeaders.add(header, responseVo.getHeaders().get(header));

					});
				}

				try {
					if (responseHeaders.getContentType() == null) {

						responseHeaders.setContentType(new MediaType("application", "json"));

					}
				} catch (Exception e) {

					responseHeaders.setContentType(new MediaType("application", "json"));
				}
				return new ResponseEntity<>(responseVo.getResponseBody(), responseHeaders, HttpStatus.OK);

			} else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {

				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(new MediaType("application", "json"));
				// 5xx errors
				return new ResponseEntity<>(MockRuleMgmtResponseVo.builder().status(0).success(false)
						.message(errorMessage).build().toString(), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(new MediaType("application", "json"));

		// if statusCode is not 404 or 500 errors , will return default response.
		return new ResponseEntity<>(
				MockRuleMgmtResponseVo.builder().status(0).success(false).message(errorMessage).build().toString(),
				responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * This is not used actually. "server.error.path=/forward" in
	 * application.properties will define the error path to be the "/forward".
	 */
	@Override
	public String getErrorPath() {
		log.info("get forward path");
		return "/forward";
	}

	/**
	 * Get java object field by the field Name.
	 * 
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