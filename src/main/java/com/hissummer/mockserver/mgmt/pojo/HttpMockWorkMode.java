package com.hissummer.mockserver.mgmt.pojo;

public enum HttpMockWorkMode {
/**
 * MOCK: 模拟返回的报文
 * UPSTREAM: 请求上游服务，将上游服务的返回返回给请求方
 * INTERNAL_FORWARD: 内部继续转发给一个MOCK或者UPSTREAM规则
 */
	MOCK, UPSTREAM, INTERNAL_FORWARD

}
