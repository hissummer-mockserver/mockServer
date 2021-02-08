package com.hissummer.mockserver.mgmt.exception;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 340702896522456976L;
	private final int status;
	private final String serviceMessage;

}
