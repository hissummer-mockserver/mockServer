package com.hissummer.mockserver.mock.service.mockResponseConverter;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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

	@Override
	public String converter(String originalResponse, Map<String, String> requestHeders, String requestBody) {

		
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");
		engine.put("response", originalResponse);
		try {
			engine.eval(originalResponse);
		} catch (ScriptException e) {
			engine.put("response", e.getMessage()+e.getStackTrace().toString());
			e.printStackTrace();
		}
		
		
		return engine.get("response").toString();
	}

}
