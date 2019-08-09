package com.hissummer.mockserver.mockplatform;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.hissummer.mockserver.mockplatform.MockRule.MockRuleBuilder;

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
