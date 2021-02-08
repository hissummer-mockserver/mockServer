package com.hissummer.mockserver.mock.controller;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hissummer.mockserver.mgmt.entity.RequestLog;
import com.hissummer.mockserver.mgmt.pojo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mgmt.service.jpa.RequestLogMongoRepository;
import com.hissummer.mockserver.mock.service.MockserviceImpl;
import com.hissummer.mockserver.mock.vo.MockResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")

@RestController
public class MockForwardController implements ErrorController {

	@Autowired
	MockserviceImpl mockservice;

	@Autowired
	RequestLogMongoRepository requestLogMongoRepository;

	@Autowired
	JmsTemplate jmsTemplate;

	/**
	 * forward to the mocked rules or upstream.
	 * 
	 * @param request
	 *            : HttpServletRequest
	 * @param requestHeaders
	 *            : request headers
	 * @param requestBody
	 *            : nullAble ( requestBody is null when request with HTTP get
	 *            method)
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/forward")
	public ResponseEntity<Object> forward(HttpServletRequest request, @RequestHeader Map<String, String> requestHeaders,
			@Nullable @RequestBody byte[] requestBody, final HttpServletResponse response) {

		String requestQueryString = request.getQueryString();
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String requestHost = request.getServerName();
		HttpHeaders responseHeaders = new HttpHeaders();

		if (status != null && Integer.valueOf(status.toString()) == HttpStatus.NOT_FOUND.value()) {

			try {
				/**
				 * change HTTP response code 404(NOT_FOUND) to 200.
				 */
				ResponseFacade responsefacade = (ResponseFacade) response;
				Field innerResponse = getField(responsefacade.getClass(), "response");
				// 强制修改inneResponse的可见
				innerResponse.setAccessible(true);
				Response innterResponseObject = (Response) innerResponse.get(responsefacade);
				org.apache.coyote.Response coyoteResponse = innterResponseObject.getCoyoteResponse();
				Field httpstatus = getField(coyoteResponse.getClass(), "status");
				// 强制修改httpStatus的可见，并将httpStatus改为200错误，而不是404。
				httpstatus.setAccessible(true);
				httpstatus.set(coyoteResponse, 200);
			}

			catch (Exception e) {
				log.info(e.getMessage());
			}

			String requestUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
			// 404 not found
			MockResponse mockOrUpstreamReturnedResponse = mockservice.getResponse(requestHeaders, requestHost,
					request.getMethod(), requestUri, requestQueryString, requestBody);

			if (mockOrUpstreamReturnedResponse.getHeaders() != null
					&& mockOrUpstreamReturnedResponse.getHeaders().containsKey("X-Forwarded-For")) {

				mockOrUpstreamReturnedResponse.getHeaders().put("X-Forwarded-For", request.getRemoteAddr() + ","
						+ mockOrUpstreamReturnedResponse.getHeaders().get("X-Forwarded-For"));
			}

			if (mockOrUpstreamReturnedResponse.getHeaders() != null) {
				mockOrUpstreamReturnedResponse.getHeaders().keySet().forEach(
						header -> responseHeaders.add(header, mockOrUpstreamReturnedResponse.getHeaders().get(header)));
			}
			responseHeaders.remove("ClientAddress");
			responseHeaders.add("ClientAddress", request.getRemoteAddr() + ":" + request.getRemotePort());

			try {
				if (responseHeaders.getContentType() == null) {

					JSON.parse(mockOrUpstreamReturnedResponse.getResponseBody());
					responseHeaders.setContentType(new MediaType("application", "json"));

				}
			} catch (Exception e) {
				responseHeaders.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
			}
			String actualFullRequestUri = requestUri
					+ (requestQueryString == null || requestQueryString.equals("null") || requestQueryString.equals("")
							? ""
							: "?" + requestQueryString);

			RequestLog requestLog = RequestLog.builder().requestHeaders(requestHeaders)
					.hittedMockRuleUri(mockOrUpstreamReturnedResponse.getMockRule().getUri())
					.requestUri(actualFullRequestUri)
					.hittedMockRuleHostName(mockOrUpstreamReturnedResponse.getMockRule().getHost())
					.isMock(mockOrUpstreamReturnedResponse.isMock()).createTime(new Date()).build();
			String contentType = requestHeaders.get("content-type");

			if (requestBody == null) {
				requestLog.setRequestBody("无请求Body报文数据！");
			} else if (requestBody != null && contentType != null
					&& (contentType.contains("application/json")
							|| contentType.contains("application/x-www-form-urlencoded")
							|| contentType.contains("application/xml") || contentType.contains("text/html")
							|| contentType.contains("text/plain"))) {
				requestLog.setRequestBody(checkUTF8(requestBody) ? new String(requestBody, StandardCharsets.UTF_8)
						: "非utf-8编码请求报文，此处不做记录");
			} else {
				requestLog.setRequestBody("非纯文本的content-type类型，不记录请求报文。");
			}

			Map<String, String> responseHeaderMap = responseHeaders.entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getKey, e -> String.join(",", e.getValue())));

			requestLog.setResponseHeaders(responseHeaderMap);

			contentType = responseHeaders.getContentType().getType() + "/"
					+ responseHeaders.getContentType().getSubtype();

			if (mockOrUpstreamReturnedResponse.getResponseBody() == null) {
				requestLog.setResponseBody("无返回Body数据！");

			} else if (mockOrUpstreamReturnedResponse.getResponseBody() != null && contentType != null
					&& (contentType.contains("application/json")
							|| contentType.contains("application/x-www-form-urlencoded")
							|| contentType.contains("application/xml") || contentType.contains("text/html")
							|| contentType.contains("text/plain"))) {
				requestLog.setResponseBody(mockOrUpstreamReturnedResponse.getResponseBody());
			} else {
				requestLog.setResponseBody("非纯文本的content-type类型，不记录请求报文。");
			}

			// Send a message with a POJO - the template reuse the message converter
			jmsTemplate.convertAndSend("requestlog", requestLog);
			log.info("send the requestlog message to requestlog destination.");

			return new ResponseEntity<>(mockOrUpstreamReturnedResponse.getResponseBody(), responseHeaders,
					HttpStatus.OK);

		}
		responseHeaders.setContentType(new MediaType("application", "json"));
		return new ResponseEntity<>(
				MockRuleMgmtResponseVo.builder().status(0).success(false).message(errorMessage).build().toString(),
				responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private boolean checkUTF8(byte[] barr) {

		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		ByteBuffer buf = ByteBuffer.wrap(barr);

		try {
			decoder.decode(buf);

		} catch (CharacterCodingException e) {
			return false;
		}

		return true;
	}

	/**
	 * This is not used actually. "server.error.path=/forward" in
	 * application.properties will define the error path to be the "/forward".
	 */
	@Override
	public String getErrorPath() {
		log.debug("get forward path");
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