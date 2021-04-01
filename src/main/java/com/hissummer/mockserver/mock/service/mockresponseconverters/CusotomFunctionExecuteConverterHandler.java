package com.hissummer.mockserver.mock.service.mockresponseconverters;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mock.service.mockresponseconverters.converterinterface.MockResponseSetUpConverterInterface;
import com.hissummer.mockserver.mock.service.mockresponseconverters.customfunction.CustomFunctionInterface;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(value = 2)
@Slf4j
public class CusotomFunctionExecuteConverterHandler implements MockResponseSetUpConverterInterface {

	@Autowired
	private ApplicationContext context;

	@Override
	public String converter(String originalResponse, Map<String, String> requestHeders,String requestUri,
			Map<String, String> requestQueryString, byte[] requestBody) {

		String pattern = "\\$\\{__([a-zA-Z0-9]*)\\((.*?)\\)\\}";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(originalResponse);

		// boolean firstMatch=true;

		String replaceString = null;
		int offposition = 0;

		while (m.find()) {
			log.debug("CusotomFunctionExecuteConverterHandler - Found custom function: " + m.group(0));
			log.debug("CusotomFunctionExecuteConverterHandler - start{} end{}", m.start(), m.end());
			log.debug("CusotomFunctionExecuteConverterHandler - Found args: " + m.group(1));
			log.debug("CusotomFunctionExecuteConverterHandler - Found args: " + m.group(2));

			log.debug(originalResponse);

			int newStart = m.start();
			int newEnd = m.end();

			newStart = m.start() + offposition;
			newEnd = m.end() + offposition;

			try {

				// 获取到自定义方法
				CustomFunctionInterface function = (CustomFunctionInterface) context
						.getBean("CustomFunction" + m.group(1));

				if (function != null) {

					replaceString = function.execute(this.getCustomFunctionArguments(m.group(2)));

					if (replaceString != null) {
						offposition = offposition + replaceString.length() - m.group(0).length();

						originalResponse = originalResponse.substring(0, newStart) + replaceString
								+ originalResponse.substring(newEnd);
					} else {

						replaceString = "!!Function_" + m.group(1) + "_Exception!!";
						offposition = offposition + replaceString.length() - m.group(0).length();

						originalResponse = originalResponse.substring(0, newStart) + replaceString
								+ originalResponse.substring(newEnd);

					}

				} else {

					replaceString = "!!Function_" + m.group(1) + "_NotFound!!";
					offposition = offposition + replaceString.length() - m.group(0).length();

					originalResponse = originalResponse.substring(0, newStart) + replaceString
							+ originalResponse.substring(newEnd);
				}
				log.debug(originalResponse);
			} catch (NoSuchBeanDefinitionException e) {

				replaceString = "!!Function_" + m.group(1) + "_NotFound!!";
				offposition = offposition + replaceString.length() - m.group(0).length();

				originalResponse = originalResponse.substring(0, newStart) + replaceString
						+ originalResponse.substring(newEnd);
			}

		}

		return originalResponse;
	}

	private String[] getCustomFunctionArguments(String argumentsStr) {

		if (argumentsStr == null || argumentsStr.length() == 0) {
			return new String[0];
		}
		argumentsStr = argumentsStr.replace("\\,", "[comma]");

		String[] argumentArray = argumentsStr.split(",");

		log.debug("argument length is {}", argumentArray.length);

		int i = 0;

		for (String argument : argumentArray) {
			log.debug("before resolve argument {}", argument);
			argument.replace("[comma]", ",");

			if ((argument.startsWith("\"") && argument.endsWith("\"")))

			{
				argumentArray[i] = argument.substring(1, argument.length() - 1);

			} else if ((argument.startsWith("'") && argument.endsWith("'"))) {
				argumentArray[i] = argument.substring(1, argument.length() - 1);
			}

			log.debug("after resolve argument {}", argumentArray[i]);

			i++;
		}

		return argumentArray;
	}

	// public static void main(String args[]) {
	//
	// new CusotomFunctionExecuteConverterHandler()
	// .converter("${__randomString(a,b)} sjdfksjkdfjskdjfkkke\r\nkekekff
	// ${__randomString(c,d)}");
	//
	// }

}
