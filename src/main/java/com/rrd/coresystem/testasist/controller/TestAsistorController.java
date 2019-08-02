package com.rrd.coresystem.testasist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rrd.coresystem.testasist.requestVo.RedisRequestVo;
import com.rrd.coresystem.testasist.requestVo.RedisResponseVo;
import com.rrd.coresystem.testasist.requestVo.SqlRequestVo;
import com.rrd.coresystem.testasist.requestVo.SqlResponseVo;
import com.rrd.coresystem.testasist.service.Env;
import com.rrd.coresystem.testasist.service.EnvServiceImpl;
import com.rrd.coresystem.testasist.service.RedisServiceImpl;
import com.rrd.coresystem.testasist.service.SqlServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/api")
public class TestAsistorController {

	@Autowired
	SqlServiceImpl sqlServiceImpl;

	@Autowired
	RedisServiceImpl redisServiceImpl;

	@Autowired
	EnvServiceImpl envServiceImpl;

	@PostMapping(value = "/sql/{env}/{service}")
	@ResponseBody
	public SqlResponseVo sql(@PathVariable("env") String env, @PathVariable("service") String service,
			@RequestBody SqlRequestVo sqlRequestVo) {

		log.info(sqlRequestVo.getEnv());
		log.info(env);

		log.info(sqlRequestVo.toString());

		SqlResponseVo sqlResponseVo = sqlServiceImpl.runSql(env, service, sqlRequestVo.getSql());
		log.info(sqlResponseVo.toString());
		// sqlResponseVo.
		return sqlResponseVo;
		// return new ResponseEntity<>(sqlResponseVo, HttpStatus.OK);

	}

	@PostMapping(value = "/redis/{env}")
	@ResponseBody
	public ResponseEntity<RedisResponseVo> redis(@PathVariable("env") String env,
			@RequestBody RedisRequestVo redisRequestVo) {

		log.info(env);

		RedisResponseVo redisResponseVo = redisServiceImpl.executeRedisCommand(env, "redis",
				redisRequestVo.getCommand(), redisRequestVo.getArguments());

		return new ResponseEntity<>(redisResponseVo, HttpStatus.OK);

	}

	@GetMapping(value = "/envdetail/{env}")
	@ResponseBody
	public ResponseEntity<Env> envdetail(@PathVariable("env") String env) {

		log.info(env);

		Env envdetail = envServiceImpl.getDetailByEnvName(env);

		return new ResponseEntity<>(envdetail, HttpStatus.OK);

	}

	@GetMapping(value = "/eureka/{env}")
	@ResponseBody
	public ResponseEntity<String> enveureka(@PathVariable("env") String env) {

		log.info(env);

		String eurekaServer = envServiceImpl.getEurekaServerByEnvName(env);

		return new ResponseEntity<>(eurekaServer, HttpStatus.OK);

	}

	@GetMapping(value = "/mongodb/{env}")
	@ResponseBody
	public ResponseEntity<String> mongodb(@PathVariable("env") String env) {

		log.info(env);

		return new ResponseEntity<>("OK", HttpStatus.OK);

	}

}
