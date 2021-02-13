package com.hissummer.mockserver.mgmt.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Data
@Document(collection = "rulecategories")
@Builder
public class RuleCategory {

	private String id;

	private String category;

	private String description;
	
	private int parent;
	
}
