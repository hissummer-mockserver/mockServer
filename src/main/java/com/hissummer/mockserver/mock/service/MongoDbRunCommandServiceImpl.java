package com.hissummer.mockserver.mock.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Deprecated
public class MongoDbRunCommandServiceImpl {

	@Autowired
	MongoTemplate mongoTemplate;

	public String test() {

		Document test = mongoTemplate.executeCommand("{find: \"test\"}");
		log.info(test.toJson());
		return test.toJson();

	}

	public String runCommand(String runCommand) {

		log.info(runCommand);
		Document mongoResult = getDocumentByRunCommand(runCommand);
		log.info(mongoResult.toJson());
		return mongoResult.toJson();
	}

	public Document getDocumentByRunCommand(String runCommand) {
		log.info("getDocumentByRunCommand: {}", runCommand);
		return mongoTemplate.executeCommand(runCommand);
	}

}
