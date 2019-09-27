package com.hissummer.mockserver.mock.service.mockResponseConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.hissummer.mockserver.mock.service.mockResponseConverter.customFunction.CustomFunctionInterface;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(value = 1)
@Slf4j
public class CusotomFunctionExecuteConverterHandler implements MockResponseConverter {

	@Autowired
	ApplicationContext context;

	@Override
	public String converter(String originalResponse) {

		String pattern = "\\$\\{__([a-zA-Z0-9]*)\\((.*?)\\)\\}";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(originalResponse);
		
		//boolean firstMatch=true;
		
		String replaceString="";
		int offposition = 0;
		
		while (m.find()) {
			log.info("Found custom function: " + m.group(0));
			log.info("start{} end{}", m.start(), m.end());
			log.info("Found args: " + m.group(1));
			log.info("Found args: " + m.group(2));

			log.info("ran custom function");

			log.info(originalResponse);
			
			
			
			try {
			CustomFunctionInterface function = (CustomFunctionInterface) context.getBean("CustomFunction" + m.group(1));
			if(function != null)
			{
				
				int newStart=m.start();
				int newEnd = m.end();

				 newStart = m.start() + offposition;
				 newEnd = m.end() + offposition;

				// TODO bug need to be fixed
				replaceString = function.execute(this.getCustomFunctionArguments(m.group(2)));
				
				offposition = replaceString.length() - m.group(0).length();
				
				originalResponse = originalResponse.substring(0, newStart) + replaceString+ originalResponse.substring(newEnd);
			
			
			}
			log.info(originalResponse);
			}
			catch(NoSuchBeanDefinitionException e) {
				
				log.warn("bean customfunction '{}' is not found","CustomFunction" + m.group(1));
			}
			
		}

		return originalResponse;
	}
	
	private String[] getCustomFunctionArguments(String argumentsStr) {
		
	
		if(argumentsStr == null || argumentsStr.length() == 0)
		{
			return new String[0];
		}
		argumentsStr = argumentsStr.replace("\\,", "[comma]");
		
		String[] argumentArray = argumentsStr.split(",");
		
		log.info("argument length is {}",argumentArray.length);
		
		int i=0;
		
		for(String argument : argumentArray)
		{
			log.info("before resolve argument {}",argument);
			argument.replace("[comma]", ",");
			
			if(  (argument.startsWith("\"") && argument.endsWith("\""))  )
				
			{
				argumentArray[i] = argument.substring(1, argument.length()-1);

			}
			else if((argument.startsWith("'") && argument.endsWith("'")) )
			{
				argumentArray[i]  = argument.substring(1, argument.length()-1);
			}			
			
			log.info("after resolve argument {}",argumentArray[i] );
			
			i++;
		}
		
		
		return argumentArray;
	}

	public static void main(String args[]) {

		new CusotomFunctionExecuteConverterHandler()
				.converter("${__randomString(a,b)} sjdfksjkdfjskdjfkkke\r\nkekekff  ${__randomString(c,d)}");

	}

}
