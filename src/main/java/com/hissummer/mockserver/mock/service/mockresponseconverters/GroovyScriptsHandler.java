package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.ScriptsConverterInterface;

import groovy.transform.CompileStatic;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 
 * @author lihao
 *
 */
@Component
public class GroovyScriptsHandler implements ScriptsConverterInterface {

	private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");

	@Override
	@CompileStatic
	public String converter(String originalResponse, Map<String, String> requestHeders,
			Map<String, String> requestQueryString, byte[] requestBody) {

		engine.put("response", originalResponse);
		engine.put("requestBody", requestBody);
		engine.put("requestHeaders", requestHeders);
		try {
			engine.eval(originalResponse);
		} catch (ScriptException e) {
			engine.put("response", e.getMessage() + e.getStackTrace().toString());
			e.printStackTrace();
		}

		return engine.get("response").toString();
	}

	// @CompileStatic
	public static void main(String[] args) {
		System.out.println((new Date()).getTime());
		ScriptEngineManager mgr = new ScriptEngineManager();
		System.out.println((new Date()).getTime());
		ScriptEngine jsEngine = mgr.getEngineByName("groovy");
		System.out.println((new Date()).getTime());
		try {
			System.out.println((new Date()).getTime());
			jsEngine.put("response", "hi");
			System.out.println((new Date()).getTime());
			jsEngine.eval("response='hello'");
			System.out.println((new Date()).getTime());

		} catch (ScriptException ex) {
			ex.printStackTrace();
		}
		System.out.println(jsEngine.get("response").toString());
	}

}
