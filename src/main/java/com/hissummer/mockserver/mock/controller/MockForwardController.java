package com.hissummer.mockserver.mock.controller;

import com.hissummer.mockserver.mgmt.entity.RequestLog;
import com.hissummer.mockserver.mgmt.pojo.MockRuleMgmtResponseVo;
import com.hissummer.mockserver.mock.service.MockServiceImpl;
import com.hissummer.mockserver.mock.vo.MockResponse;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*")

@RestController
public class MockForwardController implements ErrorController {

    @Autowired
    MockServiceImpl mockService;

    @Autowired
    JmsTemplate jmsTemplate;

    /**
     * forward to the mocked rules or upstream.
     *
     * @param request        : HttpServletRequest
     * @param requestHeaders : request headers
     * @param requestBody    : nullAble ( requestBody is null when request with HTTP get
     *                       method)
     * @param response :
     * @return ResponseEntity<Object>
     */
    @RequestMapping(value = "/forward")
    public ResponseEntity<Object> forward(HttpServletRequest request, @RequestHeader Map<String, String> requestHeaders,
                                          @Nullable @RequestBody byte[] requestBody, final HttpServletResponse response) {

        //Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        //log.info("response status code: ",response.getStatus());

        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            // 通过错误重定向过来的response上个请求的response相应。 如果http code码是404 NotFound， 则那么认为这个接口在mockserver不存在，然后去查找mock规则。
            try {
                //response.setStatus(200); not work!!!
                changeHttpCodeTo200(response); // reflection to change response status code from 404 to 200.
            } catch (Exception e) {
                log.info("changeHttpCode exception: {}",e.getMessage());
            }
			// 从mockserver配置获取返回mock响应或者upstream响应
            MockResponse mockOrUpstreamReturnedResponse = mockService.getResponse(request,requestHeaders, requestBody,null,null);
           //请求记录日志
            saveRequestLog(request,requestHeaders,requestBody,mockOrUpstreamReturnedResponse);

            // 转换成HttpHeader
            HttpHeaders responseHeaders = new HttpHeaders();
            if (mockOrUpstreamReturnedResponse.getResponseHeaders() != null) {
                mockOrUpstreamReturnedResponse.getResponseHeaders().keySet().forEach(
                        header -> responseHeaders.add(header, mockOrUpstreamReturnedResponse.getResponseHeaders().get(header)));
            }

			//接口返回
            return new ResponseEntity<>(mockOrUpstreamReturnedResponse.getResponseBody(), responseHeaders,
                    HttpStatus.OK);

        }
        else {

            //非404的其他错误，返回错误。
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(new MediaType("application", "json"));
            return new ResponseEntity<>(
                    MockRuleMgmtResponseVo.builder().status(0).success(false).message((String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE)).build().toString(),
                   responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*

     */
    private boolean checkUTF8(byte[] barr) {

        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer buf = ByteBuffer.wrap(barr);

        try {
            decoder.decode(buf);

        } catch (CharacterCodingException e) {
            return false;
        }

        return true;
    }

    private void changeHttpCodeTo200(HttpServletResponse response) throws NoSuchFieldException, IllegalAccessException {

        /*
         * change HTTP response code 404(NOT_FOUND) to 200(OK).
         */
        ResponseFacade responsefacade = (ResponseFacade) response;
        Field innerResponse = getField(responsefacade.getClass(), "response");
        // 强制修改innerResponse为可见
        innerResponse.setAccessible(true);
        Response innerResponseObject = (Response) innerResponse.get(responsefacade);
        org.apache.coyote.Response coyoteResponse = innerResponseObject.getCoyoteResponse();
        Field httpStatus = getField(coyoteResponse.getClass(), "status");
        // 强制修改httpStatus的可见，并将httpStatus由原来的404改为200。
        httpStatus.setAccessible(true);
        httpStatus.set(coyoteResponse, 200);

    }

    private void saveRequestLog(HttpServletRequest request, Map<String,String> requestHeaders, byte[] requestBody,MockResponse mockOrUpstreamReturnedResponse){

        String requestUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);

        log.info("{},{}",request.getRequestURI(),requestUri);

        String requestQueryString = request.getQueryString();
        String actualFullRequestUri = requestUri
                + (requestQueryString == null || requestQueryString.equals("null") || requestQueryString.equals("")
                ? ""
                : "?" + requestQueryString);

        RequestLog requestLog = RequestLog.builder().requestHeaders(requestHeaders)
                .hittedMockRuleUri(mockOrUpstreamReturnedResponse.getMockRule().getUri())
                .requestUri(actualFullRequestUri)
                .hittedMockRuleHostName(mockOrUpstreamReturnedResponse.getMockRule().getHost())
                .isMock(mockOrUpstreamReturnedResponse.isMock()).createTime(new Date()).build();
        //requestHeader is lowercase （@RequestHeader Map<String, String> requestHeaders）
        String contentType = requestHeaders.get("content-type");

        // 开始记录请求日志
        // 处理请求头和请求报文记录
        if (requestBody == null) {
            requestLog.setRequestBody("无请求Body报文数据！");
        } else if (contentType != null
                && (contentType.contains("application/json")
                || contentType.contains("application/x-www-form-urlencoded")
                || contentType.contains("application/xml") || contentType.contains("text/html")
                || contentType.contains("text/plain"))) {

            requestLog.setRequestBody(checkUTF8(requestBody) ? new String(requestBody, StandardCharsets.UTF_8)
                    : new String(requestBody));
        } else {
            requestLog.setRequestBody("非纯文本的content-type类型，不记录请求报文。");
        }

        //处理相应头和相应报文记录
        requestLog.setResponseHeaders(mockOrUpstreamReturnedResponse.getResponseHeaders());

        contentType = mockOrUpstreamReturnedResponse.getResponseHeaders().get("Content-Type");
//        contentType = responseHeaders.getContentType().getType() + "/"
//                + responseHeaders.getContentType().getSubtype();

        if (mockOrUpstreamReturnedResponse.getResponseBody() == null) {
            requestLog.setResponseBody("无返回Body数据！");

        } else if (mockOrUpstreamReturnedResponse.getResponseHeaders().containsKey("Content-Encoding")) {
            // 内容有压缩的，解压缩记录。但是返回的仍是原报文内容（即压缩的内容）
            requestLog.setResponseBody("Content-Encoding:" + mockOrUpstreamReturnedResponse.getResponseHeaders().get("Content-Encoding") + "暂不做记录");
        } else if (mockOrUpstreamReturnedResponse.getResponseBody() != null
                && (contentType == null || contentType.contains("application/json")
                || contentType.contains("application/x-www-form-urlencoded")
                || contentType.contains("application/xml") || contentType.contains("text/html")
                || contentType.contains("text/plain"))) {

            String responseData = "";
            if( mockOrUpstreamReturnedResponse.getResponseBody() instanceof String){
                responseData = (String) mockOrUpstreamReturnedResponse.getResponseBody();
            }
            else if (mockOrUpstreamReturnedResponse.getResponseBody() instanceof byte[])
            {
                responseData = checkUTF8((byte[]) mockOrUpstreamReturnedResponse.getResponseBody()) ? new String((byte[]) mockOrUpstreamReturnedResponse.getResponseBody(), StandardCharsets.UTF_8)
                        : new String((byte[]) mockOrUpstreamReturnedResponse.getResponseBody());
            }
            requestLog.setResponseBody(responseData);
        } else {
            requestLog.setResponseBody("非纯文本的content-type类型，不记录请求报文。");
        }

        // Send a message with a POJO - the template reuse the message converter
        jmsTemplate.convertAndSend("requestlog", requestLog);
        log.debug("send the requestlog message to requestlog destination.");
        //结束记录请求日志


    }


    /**
     * This is not used, actually. "server.error.path=/forward" in
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
     * @param clazz  class
     * @param fieldName fieldName
     * @return Field
     * @throws NoSuchFieldException  No such Field Exception
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