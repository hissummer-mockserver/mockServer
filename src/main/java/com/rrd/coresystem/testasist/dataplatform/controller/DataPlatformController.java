package com.rrd.coresystem.testasist.dataplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rrd.coresystem.testasist.dataplatform.DataplatformRequestVo;
import com.rrd.coresystem.testasist.dataplatform.service.DataplatformServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author lihao
 *
 */
@Slf4j
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/api")
public class DataPlatformController {

	@Autowired
	DataplatformServiceImpl dataplatformServiceImpl;

	@GetMapping(value = "/dataplatform/test")
	@ResponseBody
	public ResponseEntity<String> test() {
		String response = dataplatformServiceImpl.test();
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping(value = "/dataplatform/mongodbruncommand")
	@ResponseBody
	public ResponseEntity<String> runCommand(@RequestBody DataplatformRequestVo dataplatformRequestVo) {

		log.info(dataplatformRequestVo.getCommand());
		String response = dataplatformServiceImpl.runCommand(dataplatformRequestVo.getCommand());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}
}
