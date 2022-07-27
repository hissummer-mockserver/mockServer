package com.hissummer.mockserver.mock.service;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

import com.hissummer.mockserver.mgmt.pojo.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hissummer.mockserver.mgmt.entity.HttpConditionRule;
import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.service.HttpConditionRuleServiceImpl;
import com.hissummer.mockserver.mock.service.jpa.MockRuleMongoRepository;
import com.hissummer.mockserver.mock.service.mockresponseconverters.GroovyScriptsHandler;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.TextConverterSetUpInterface;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseTearDownConverterInterface;
import com.hissummer.mockserver.mock.vo.MockResponse;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * MockServiceImpl
 *
 * @author lihao
 */
@Slf4j
@Service
public class MockServiceImpl {

    @Autowired
    List<TextConverterSetUpInterface> setUpConverters;
    @Autowired
    List<MockResponseTearDownConverterInterface> tearDownConverters;
    @Autowired
    MockRuleMongoRepository mockRuleRepository;

    @Autowired
    HttpConditionRuleServiceImpl httpConditionRuleServiceImpl;

    @Autowired
    GroovyScriptsHandler groovyScriptsHandler;

    private static final String NO_MATCHED = "Sorry , No rules matched.";


    public MockResponse getResponse(HttpServletRequest request, Map<String, String> requestHeaders, byte[] requestBody, String overrideInternalRequestUri, String overrideInternalRequestHost) {
        Date startTime = new Date();

        String requestQueryString = request.getQueryString();
        String requestMethod = request.getMethod();
        String requestHost = overrideInternalRequestHost == null ? request.getServerName() : overrideInternalRequestHost;
        String requestUri = overrideInternalRequestUri == null ? (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI) : overrideInternalRequestUri;
        //获取匹配的MockRule
        HttpMockRule matchedMockRule = getMatchedRule(requestHost, requestUri);
        //如果匹配了MockRule，查看是否命中MockRule中的条件规则conditionRule
        HttpCondition conditionRule = getHttpConditionRuleCondition(matchedMockRule, requestUri, requestMethod, requestQueryString, requestHeaders, requestBody);
        //根据命中的规则和条件规则获取响应
        MockResponse mockOrUpstreamReturnedResponse = getResponse(matchedMockRule, conditionRule, request, requestUri, requestMethod, requestQueryString, requestHeaders, requestBody);

        //修改响应头
        modifyResponseHeaders(mockOrUpstreamReturnedResponse, request);
        log.info("{} slaped Seconds: {}", requestUri, (Float.valueOf(new Date().getTime()) / 1000 - Float.valueOf(startTime.getTime()) / 1000) );

        return mockOrUpstreamReturnedResponse;
    }

    /**
     * 修改返回的响应头。1） 添加了一些自定义Header方便排查问题，例如 ClientAddress, IsMock, IsUpstream  2) 如果未设置内容类型，自动设置 content-type
     * 3) Upstream模式下，X-Forwarded-For 加入当前的Mockserver
     *
     * @param mockOrUpstreamReturnedResponse
     * @param request
     * @return
     */
    private void modifyResponseHeaders(MockResponse mockOrUpstreamReturnedResponse, HttpServletRequest request) {

        if (mockOrUpstreamReturnedResponse.isUpstream()) {
            // 如果通过了mockserver作为代理请求了Upstream上游服务，则需要把mockserver认为一个代理加入进去到X-Forwarded-For
            if (mockOrUpstreamReturnedResponse.getResponseHeaders() != null) {
                if (mockOrUpstreamReturnedResponse.getResponseHeaders().containsKey("X-Forwarded-For")) {

                    mockOrUpstreamReturnedResponse.getResponseHeaders().put("X-Forwarded-For", request.getRemoteAddr() + ","
                            + mockOrUpstreamReturnedResponse.getResponseHeaders().get("X-Forwarded-For"));
                } else {
                    mockOrUpstreamReturnedResponse.getResponseHeaders().put("X-Forwarded-For", request.getRemoteAddr());
                }
            }
        }

        if (mockOrUpstreamReturnedResponse.getResponseHeaders() == null) {
            mockOrUpstreamReturnedResponse.setResponseHeaders(new HashMap<>());
        }
        //添加自定义Header
        mockOrUpstreamReturnedResponse.getResponseHeaders().put("ClientAddress", request.getRemoteAddr() + ":" + request.getRemotePort());
        mockOrUpstreamReturnedResponse.getResponseHeaders().put("IsMock", mockOrUpstreamReturnedResponse.isMock() ? "true" : "false");
        mockOrUpstreamReturnedResponse.getResponseHeaders().put("IsUpstream", mockOrUpstreamReturnedResponse.isUpstream() ? "true" : "false");


        //设置默认的content-type类型
        if (!mockOrUpstreamReturnedResponse.getResponseHeaders().containsKey("Content-Type") && !mockOrUpstreamReturnedResponse.getResponseHeaders().containsKey("content-type")) {
            try {
                JSON.parse(JSON.toJSONString(mockOrUpstreamReturnedResponse.getResponseBody()));
                mockOrUpstreamReturnedResponse.getResponseHeaders().put("Content-Type", "application/json; charset=UTF-8");

            } catch (Exception e) {
                mockOrUpstreamReturnedResponse.getResponseHeaders().put("Content-Type", "text/plain; charset=UTF-8");
            }
        }


    }

    /**
     * 根据hostName和请求Url地址，获取到Mock报文或者Upstream上游节点返回
     * <p>
     * requestHeaders     请求headers
     * requestHostName    请求的Host
     * requestMethod      Http请求方法 GET POST DELETE ...
     * requestQueryString 请求查询串 a=b&c=d
     * requestUri         请求资源路径  /path/path
     * requestBody        请求内容体  POST or PUT request body content
     */
    /*
    @Deprecated
    public MockResponse getResponse(Map<String, String> requestHeaders, String requestHostName, String requestMethod,
                                    String requestUri, String requestQueryString, byte[] requestBody) {

        MockResponse returnResponse = null;
        if (!requestHostName.equals(requestHeaders.get("Host"))) {

            // TODO  transparent proxy mode， 这里其实考虑的逻辑有问题，实际上connect的ip地址和请求的host不一致可能会出现，但通常客户端不会出现不一致 ?
            log.warn("requestHostName = {}, header Host value={} not equal", requestHostName,
                    requestHeaders.get("Host"));
        }

        // 第一次匹配规则
        HttpMockRule matchedMockRule = getMatchedMockRulesByHostnameAndUrl(requestHostName, requestUri);
        HttpCondition conditionRule = null;

        if (matchedMockRule != null) {
            // 如果找到了匹配的mock规则，然后查找是否有命中的条件规则。
            HttpConditionRule conditionRulesOnMockRule = httpConditionRuleServiceImpl
                    .getHttpConditionRulesByHttpMockRuleId(matchedMockRule.getId());

            if (conditionRulesOnMockRule != null) {
                conditionRule = VerifiedConditionRule(conditionRulesOnMockRule.getConditionRules(),
                        requestUri, requestMethod, requestQueryString, requestHeaders, requestBody);
            }
        }

        returnResponse = getResponse(matchedMockRule, conditionRule, requestUri, requestMethod, requestQueryString, requestHeaders, requestBody);

        return returnResponse;
    }
*/
    private HttpMockRule getMatchedRule(String requestHostName, String requestUri) {

        return getMatchedMockRulesByHostnameAndUrl(requestHostName, requestUri);

    }

    private HttpCondition getHttpConditionRuleCondition(HttpMockRule matchedMockRule, String requestUri, String requestMethod, String requestQueryString, Map<String, String> requestHeaders, byte[] requestBody) {

        if (matchedMockRule == null) {
            return null;
        } else {
            HttpConditionRule conditionRulesOnMockRule = httpConditionRuleServiceImpl
                    .getHttpConditionRulesByHttpMockRuleId(matchedMockRule.getId());

            if (conditionRulesOnMockRule != null) {
                return VerifiedConditionRule(conditionRulesOnMockRule.getConditionRules(),
                        requestUri, requestMethod, requestQueryString, requestHeaders, requestBody);
            } else {
                return null;
            }
        }
    }


    private MockResponse getResponse(HttpMockRule matchedMockRule, HttpCondition conditionRule, HttpServletRequest request, String requestUri, String requestMethod, String requestQueryString, Map<String, String> requestHeaders, byte[] requestBody) {

        String nomatchresponse = JSON
                .toJSONString(MockRuleMgmtResponseVo.builder().status(0).success(true).message(NO_MATCHED).build());
        MockResponse returnResponse = MockResponse.builder().responseBody(nomatchresponse)
                .mockRule(HttpMockRule.builder().uri("null").host("*").build()).build();
        if (matchedMockRule == null) {
            return returnResponse;
        }
        // 如果存在条件规则，则直接看是否命中某一条规则
        HttpMockWorkMode workMode = conditionRule == null ? matchedMockRule.getWorkMode() : conditionRule.getWorkMode();
        if (workMode == null) {
            workMode = HttpMockWorkMode.MOCK;
        }
        switch (workMode) {
            case UPSTREAM:
                String requestUriWithQueryString = requestUri;
                if (!StringUtils.isEmpty(requestUri) && !StringUtils.isEmpty(requestQueryString) && !requestQueryString.equals("null")) {
                    requestUriWithQueryString = requestUriWithQueryString + "?" + requestQueryString;
                }
                // mock rule 的工作模式为upstream模式. 后期将upstream作为hostname的rule单独管理，这里的代码将会移除！
                returnResponse = getUpstreamResponse(matchedMockRule, conditionRule, requestHeaders, requestMethod,
                        requestUriWithQueryString, requestBody);
                break;

            case MOCK:
                // mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
                returnResponse = MockResponse.builder()
                        .responseBody(interpreter(conditionRule == null ? matchedMockRule.getMockResponse() : conditionRule.getMockResponse(), requestHeaders, requestUri,
                                requestQueryString, requestBody, false))
                        .mockRule(matchedMockRule).isMock(true).isUpstream(false)
                        .responseHeaders(conditionRule == null ? matchedMockRule.getResponseHeaders() : conditionRule.getResponseHeaders()).build();
                break;

            case INTERNAL_FORWARD:
                // 修改requestUri 然后内部转发(forward)到getResponse重新获取新的匹配规则
                UpstreamNode node = null;
                if (conditionRule != null) {
                    node = conditionRule.getUpstreams().getNodes().iterator().next();
                } else {
                    node = matchedMockRule.getUpstreams().getNodes().iterator().next();
                }
                String nodeUri = node.getUri();
                String internalForwardHost = node.getAddress();
                String forwardedUri = getActualRequestUpstreamUri(requestUri, matchedMockRule.getUri(), nodeUri);
                returnResponse = this.getResponse(request, requestHeaders, requestBody, forwardedUri, internalForwardHost);
                break;
            default:
                break;

        }

        return returnResponse;
    }


    private HttpCondition VerifiedConditionRule(List<HttpCondition> conditionRules, String requestUri,
                                                String requestMethod, String requestQueryString, Map<String, String> requestHeaders, byte[] requestBody) {
        String[] conditionsResult = {""};

        for (HttpCondition condition : conditionRules) {

            // 默认的顺序 TODO 这里后续需要确认存入的顺序和读出的顺序是否一致。

            conditionsResult[0] = "response = ";
            condition.getConditionExpression().forEach(conditionExpression -> {

                conditionsResult[0] = conditionsResult[0]
                        + ConditionConverter.converToGroovyExpression(conditionExpression.getToBeCompareValue(),
                        conditionExpression.getCompareCondition(), conditionExpression.getConditionValue());

            });

            String result = interpreter(conditionsResult[0], requestHeaders, requestUri, requestQueryString,
                    requestBody, true);

            if (result.equals("true")) {
                return condition;
            }
        }

        return null;

    }

    /**
     * 解析文本文件
     *
     * @param toBeInterpreteredText 待解析处理的文本
     * @param requestHeders         请求头
     * @param requestUri            请求Uri
     * @param requestQueryString    请求查询字符串
     * @param requestBody           请求体
     * @param forceUseGroovyHandler 是否强制使用groovy脚本执行
     * @return
     */
    private String interpreter(String toBeInterpreteredText, Map<String, String> requestHeders, String requestUri,
                               String requestQueryString, byte[] requestBody, boolean forceUseGroovyHandler) {

        // multipart 暂不支持requestBody的解析，multipart的请求报文待确认后支持
//		if (requestHeders.get("content-type") == null || requestHeders.get("content-type").contains("multipart")) {
//			// requestBody = "";
//		}
        Map<String, String> requestQueryStringMap = new HashMap<>();

        if (requestQueryString != null && !requestQueryString.equals("null")) {
            String[] queryStrings = requestQueryString.split("&");

            for (String queryString : queryStrings) {
                String[] keyValue = queryString.split("=");
                if (keyValue.length == 1) {
                    requestQueryStringMap.put(keyValue[0], "");
                } else {
                    String value = keyValue[1];
                    if (keyValue[0] != null) {
                        requestQueryStringMap.put(keyValue[0], value);
                    }
                }

            }
        }
        String afterInterpretedText = toBeInterpreteredText;
        for (TextConverterSetUpInterface converter : setUpConverters) {
            // 内部变量替换
            afterInterpretedText = converter.converter(afterInterpretedText, requestHeders, requestUri,
                    requestQueryStringMap, requestBody);
        }

        if (toBeInterpreteredText.startsWith("//groovy") || forceUseGroovyHandler) {
            // groovy的脚本执行
            afterInterpretedText = groovyScriptsHandler.converter(afterInterpretedText, requestHeders,
                    requestQueryStringMap, requestBody);
        }

        for (MockResponseTearDownConverterInterface converter : tearDownConverters) {
            // 输出的格式化
            afterInterpretedText = converter.converter(afterInterpretedText, requestHeders, requestQueryStringMap,
                    requestBody);
        }

        return afterInterpretedText;
    }

    private MockResponse getUpstreamResponse(HttpMockRule matchedMockRule, HttpCondition condition,
                                             Map<String, String> requestHeaders, String requestMethod, String requestUri, byte[] requestBody) {
        // 获取到匹配的结果
        String upstreamAddress = "mockserver.hissummer.com";
        String protocol = "http";
        String upstreamUri = "/docs";
        try {
            if (matchedMockRule.getUpstreams().getNodes().get(0).getAddress() != null)
                upstreamAddress = condition != null ? condition.getUpstreams().getNodes().get(0).getAddress()
                        : matchedMockRule.getUpstreams().getNodes().get(0).getAddress();

            if (matchedMockRule.getUpstreams().getNodes().get(0).getProtocol() != null)
                protocol = condition != null ? condition.getUpstreams().getNodes().get(0).getProtocol()
                        : matchedMockRule.getUpstreams().getNodes().get(0).getProtocol();

            if (matchedMockRule.getUpstreams().getNodes().get(0).getUri() != null)
                upstreamUri = condition != null ? condition.getUpstreams().getNodes().get(0).getUri()
                        : matchedMockRule.getUpstreams().getNodes().get(0).getUri();

        } catch (Exception e) {
            log.error("{} mockrule : upstream data is not defined{}", matchedMockRule.getId(),
                    matchedMockRule.getUpstreams());
        }

        upstreamUri = getActualRequestUpstreamUri(requestUri, matchedMockRule.getUri(), upstreamUri);

        MockResponse upstreamResponse = requestToUpstream(protocol, requestHeaders, upstreamAddress, requestMethod,
                upstreamUri, requestBody);

        upstreamResponse.setMockRule(matchedMockRule);

        return upstreamResponse;
    }

    /**
     * 获取最后真实要转发或者请求upstream的uri。
     * <p>
     * 例子1：
     * 本次实际请求的requestUri = /test/mytest
     * 本次命中的规则mockRuleUri = /test
     * 本次命中的规则请求上游服务的upstreamUri = /test
     * 则返回的实际要请求upstream的uri为  /test/mytest
     * <p>
     * 例子2：
     * 本次实际请求的requestUri = /test/mytest
     * 本次命中的规则mockRuleUri = /test
     * 本次命中的规则请求上游服务的upstreamUri = /
     * 则返回的实际要请求upstream的uri为  /mytest
     * <p>
     * 例子3：
     * 本次实际请求的requestUri = /test/mytest
     * 本次命中的规则mockRuleUri = /
     * 本次命中的规则请求上游服务的upstreamUri = /
     * 则返回的实际要请求upstream的uri为  /test/mytest
     * <p>
     * 例子4：
     * 本次实际请求的requestUri = /test/mytest
     * 本次命中的规则mockRuleUri = /
     * 本次命中的规则请求上游服务的upstreamUri = /upstream
     * 则返回的实际要请求upstream的uri为  /upstream/test/mytest
     *
     * @param mockRuleUri 匹配到的mock规则 uri
     * @param requestUri  本次请求的uri
     * @param upstreamUri 上游的uri
     * @return
     */
    private String getActualRequestUpstreamUri(String requestUri, String mockRuleUri, String upstreamUri) {

        String handledRequestUri = requestUri;
        String handledUpstreamUri = upstreamUri;
        String handledMockRuleUri = mockRuleUri;

        if (requestUri.charAt(requestUri.length() - 1) == '/') {
            handledRequestUri = requestUri.substring(0, requestUri.length() - 1);
        }
        if (upstreamUri.charAt(upstreamUri.length() - 1) == '/') {
            handledUpstreamUri = upstreamUri.substring(0, upstreamUri.length() - 1);
        }
        if (mockRuleUri.charAt(mockRuleUri.length() - 1) == '/') {
            handledMockRuleUri = mockRuleUri.substring(0, mockRuleUri.length() - 1);
        }

        String suffixUri = handledRequestUri.substring(handledMockRuleUri.length());

        if (suffixUri.length() > 0 && suffixUri.charAt(0) != '/') {
            suffixUri = "/" + suffixUri;
        }

        return handledUpstreamUri + suffixUri;
    }

    public static void main(String[] args) {
        System.out.println(
                new MockServiceImpl().getActualRequestUpstreamUri("/test/", "/test/jsdjfksdjf/sjdjfjf/", "/test2/"));

    }

    private MockResponse requestToUpstream(String protocol, Map<String, String> requestHeaders, String upstreamAddress,
                                           String requestMethod, String requestUri, byte[] requestBody) {

        Headers.Builder headerBuilder = new Headers.Builder();

        for (Entry<String, String> header : requestHeaders.entrySet()) {
            if (header.getKey().equalsIgnoreCase("host")) {
                headerBuilder.add(header.getKey(), getHost(upstreamAddress));
            } else {
                headerBuilder.add(header.getKey(), header.getValue());
            }
        }

        Headers requestUpstreamHeaders = headerBuilder.build();

        RequestBody okHttpRequestBody = null;
        if (requestBody != null) {
            okHttpRequestBody = RequestBody.create(requestBody, MediaType.parse(requestHeaders.get("content-type")));
        }
        Request request = new Request.Builder().url(protocol + "://" + upstreamAddress + requestUri)
                .method(requestMethod, okHttpRequestBody).headers(requestUpstreamHeaders).build();
        log.debug("upstream request: {} | {}", JSON.toJSONString(request.headers()), JSON.toJSONString(request.body()));
        Call call = HttpClientUtil.client.newCall(request);
        try {
            Response response = call.execute();
            log.debug("upstream response:{} | {} | {} ", JSON.toJSONString(response.code()),
                    JSON.toJSONString(response.headers()), JSON.toJSONString(response.body()));
            JSONObject responseJson = new JSONObject();
            Map<String, String> upstreamResponseHeaders = new HashMap<>();
            if (response.isSuccessful()) {
                Iterator<Pair<String, String>> headerIterator = response.headers().iterator();
                while (headerIterator.hasNext()) {
                    Pair<String, String> responseHeader = headerIterator.next();
                    upstreamResponseHeaders.put(responseHeader.getFirst(), responseHeader.getSecond());
                }

                if (upstreamResponseHeaders.containsKey("X-Forwarded-For")) {
                    upstreamResponseHeaders.put("X-Forwarded-For",
                            "hissummer-mockserver," + upstreamResponseHeaders.get("X-Forwarded-For"));
                } else {
                    upstreamResponseHeaders.put("X-Forwarded-For", "hissummer-mockserver");
                }
                byte[] rawdata = response.body().bytes();
                response.close();
                return MockResponse.builder().responseHeaders(upstreamResponseHeaders).responseBody(rawdata)
                        .isUpstream(true).isMock(false).build();
            } else {

                Map<String, List<String>> readableHeaders = response.headers().toMultimap();

                responseJson.put("code", response.code());
                responseJson.put("message", response.message());
                responseJson.put("networkResponse", response.networkResponse());
                responseJson.put("headers", readableHeaders);
                responseJson.put("body", response.body().string());

                response.close();
                return MockResponse.builder().responseBody(responseJson.toJSONString()).isUpstream(true).isMock(false)
                        .build();

            }

        } catch (IOException e) {
            return MockResponse.builder().responseBody(e.toString()).isUpstream(true).isMock(false).build();

        }


    }

    private String getHost(String upstream) {
        return upstream.split(":")[0];
    }


    private boolean isIpv4OrIpv6(String hostName) {

        return isIPv4(hostName) || isIPv6(hostName);

    }

    /*
     * isIp 判断是否是ipv4地址
     */
    private boolean __isIpv4(String host) {
        try {
            if (host == null || host.isEmpty()) {
                return false;
            }

            String[] parts = host.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            if (host.endsWith(".")) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }

    public static boolean isIPv4(String ipAddress) {
        boolean isIPv4 = false;

        if (ipAddress != null) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                isIPv4 = (inetAddress instanceof Inet4Address) && inetAddress.getHostAddress().equals(ipAddress);
            } catch (UnknownHostException ex) {
            }
        }

        return isIPv4;
    }

    public static boolean isIPv6(String ipAddress) {
        boolean isIPv6 = false;

        if (ipAddress != null) {
            try {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                isIPv6 = (inetAddress instanceof Inet6Address);
            } catch (UnknownHostException ex) {
            }
        }

        return isIPv6;
    }


    /*
     * Get matched mock rules by Hostname and url. If the hostname is ip address,
     * hostname is '*'.
     *
     *
     */
    private HttpMockRule getMatchedMockRulesByHostnameAndUrl(String hostName, String requestUri) {
        // 如果Host是ip地址,则查找mock规则时,则hostName是未定义,只根据uri进行查找匹配规则.
        if (isIpv4OrIpv6(hostName)) {
            hostName = "*";
            //如果是通过ip访问mockserver，则查找规则按照 hostName是* 即匹配所有hostName来查找mock规则
        }

        String requestUriFormat = requestUri;

        //如果请求最后有 / ， 则移除。
        if (requestUri != null && requestUri.length() > 0 && requestUri.charAt(requestUri.length() - 1) == '/') {
            requestUriFormat = requestUri.substring(0, requestUri.length() - 1);
        }
        if (requestUri != null && requestUri.length() > 0 && requestUri.charAt(0) == '/') {
            requestUriFormat = requestUri.substring(1, requestUri.length());
        }
        // /a/b/c  to  {'a','b','c'}
        List<String> matchRequestURI = new ArrayList<String>(Arrays.asList(requestUriFormat.split("/")));
        String matchRequestURIString = "/"; //if not matched , default matched uri request is /
        int loops = matchRequestURI.size();
        List<String> matchRequestURIList = new ArrayList<>();

        /**
         * uri: /1/2/3/4 size=4, loop=5 first loop: /1/2/3/4 second loop: /1/2/3 third
         * loop: /1/2 fourth loop: /1 fifth loop: /
         *
         */

        for (int i = 0; i <= loops; i++) {

            // Verify that "remove()" is used correctly. Here is correct, we don't use i in
            // loop. Every loop remove last element of the list.

            if (matchRequestURI.isEmpty()) {
                matchRequestURIList.add("/");
            } else {
                matchRequestURIList.add("/" + String.join("/", matchRequestURI));
                matchRequestURI.remove(matchRequestURI.size() - 1);

            }
        }

        List<HttpMockRule> foundMockRules = mockRuleRepository.findByHostAndUriIn(hostName, matchRequestURIList);
        Optional<HttpMockRule> matchedMockRule = foundMockRules.stream().max((mockRule1, mockRule2) -> Integer.compare(mockRule1.getUri().length(), mockRule2.getUri().length()));

        if (matchedMockRule.isPresent()) {
            return matchedMockRule.get();
        }
        // 如果第一次查找时,Host是域名,没有找到对应的规则,则会重新假设Host为*时,重新再查找一次.
        if (!hostName.equals("*")) {
            hostName = "*";
            // 如果第一次host不为null时没有查到匹配规则,则重新将host设置为null,重新查找一次规则.
            return getMatchedMockRulesByHostnameAndUrl(hostName, requestUri);
        } else {

            return null;
        }
    }

    public String testRule(HttpMockRule mockRule) {

        HttpMockWorkMode workMode = mockRule.getWorkMode();

        if (workMode.equals(HttpMockWorkMode.MOCK)) {

            // mock rule 的工作模式为mock模式，mock模式直接返回mock的报文即可
            return MockResponse.builder()
                    .responseBody(
                            interpreter(mockRule.getMockResponse(), Collections.emptyMap(), null, null, null, false))
                    .isMock(true).isUpstream(false).responseHeaders(mockRule.getResponseHeaders()).build().getResponseBody().toString();
        } else {
            return "upstream mode and internalForward mode not support test, please directly access the upstream address.";
        }

    }

}
