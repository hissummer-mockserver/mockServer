package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.util.Date;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mock.service.ScriptEnginePool;
import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.ScriptsConverterInterface;

import groovy.transform.CompileStatic;

/**
 * 
 * @author lihao
 *
 */
@Component
public class GroovyScriptsHandler implements ScriptsConverterInterface {

	// private static final ScriptEngine engine = new
	// ScriptEngineManager().getEngineByName("groovy");

	@Autowired
	ScriptEnginePool scriptEnginePool;

	@Override
	@CompileStatic
	public String converter(String originalResponse, Map<String, String> requestHeders,
			Map<String, String> requestQueryString, byte[] requestBody) {

		ScriptEngine engine = scriptEnginePool.getSpareEngine();

		if (engine == null) {
			return "Groovy engine is busy now,please try again later.";
		}

		engine.put("response", originalResponse);
		if (requestHeders.containsKey("content-type")
				&& (requestHeders.get("content-type").contains("application/x-www-form-urlencoded")
						|| requestHeders.get("content-type").contains("application/json"))) {
			engine.put("requestBody", requestBody);
		} else {
			engine.put("requestBody", null);
		}
		engine.put("requestHeaders", requestHeders);
		try {
			engine.eval(originalResponse);
			return engine.get("response").toString();
		} catch (ScriptException e) {
			engine.put("response", e.getMessage() + e.getStackTrace().toString());
			return engine.get("response").toString();
		} finally {
			scriptEnginePool.releaseEngine(engine);
		}

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
