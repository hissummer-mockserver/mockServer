package com.hissummer.mockserver.mockplatform.mgmt.vo;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "eurekaMockrules")
@Builder
public class EurekaMockRule {

	private String id;
    private String port;
    private String hostName;
    private String serviceName;
    private String eurekaServer;
    private Boolean enable;
}
