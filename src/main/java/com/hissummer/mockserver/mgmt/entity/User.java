package com.hissummer.mockserver.mgmt.entity;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "user")
@Builder
public class User {

	private String id;
	private String username;
	private String password;
	private Date expiredDate;
	private Date loginExpiredDate;
	private Date createDate;
	private Boolean enable;
	// private String version;
}
