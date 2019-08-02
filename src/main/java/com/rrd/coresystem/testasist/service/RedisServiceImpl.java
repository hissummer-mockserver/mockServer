package com.rrd.coresystem.testasist.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rrd.coresystem.testasist.requestVo.RedisResponseVo;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import lombok.extern.slf4j.Slf4j;
/*
 * 
 * @author lihao
 * 
 * Redis直接链接服务
 *
 */
@Slf4j
@Service
public class RedisServiceImpl {

	public RedisResponseVo executeRedisCommand(String env, String service, String command, String args) {

		RedisResponseVo redisResponseVo = new RedisResponseVo();

		String[] envargs = EnvConfig.envMap.get(env).serviceDbConf.get(service);

		String[] redisPorts = envargs[1].split("\\|"); // split argument is 'Regular expression' not string. | should be
														// escaped.

		RedisClusterClient redisClient = null;
		StatefulRedisClusterConnection<String, String> connection = null;
		RedisClusterCommands<String, String> syncCommands = null;

		if (command.toLowerCase().equals("keys")) {

			for (String port : redisPorts)

			{
				log.info("redis://{}:{}/", envargs[0], port);

				log.info("command:{},args:{}", command, args);
				redisClient = RedisClusterClient
						.create("redis://:YTMxYjFjYWIzYjc4NDdjNzk0OTJhODY3@" + envargs[0] + ":" + port + "/");

				connection = redisClient.connect();

				syncCommands = connection.sync();

				List<String> keys = syncCommands.keys(args);

				JSONArray listkeys = new JSONArray();

				if (keys.size() > 20) {
					keys = keys.subList(0, 19);
				}

				for (String key : keys) {
					if (syncCommands.type(key).equals("string")) {

						JSONObject jsonObject = new JSONObject();
						jsonObject.put("key", key);
						jsonObject.put("value", syncCommands.get(key));

						listkeys.add(jsonObject);
					}
				}

				redisResponseVo.setCount(keys.size());

				redisResponseVo.setData(listkeys);

				redisResponseVo.setStatus("0");

				if (connection != null)
					connection.close();
				if (redisClient != null)
					redisClient.shutdown();

			} // end of while
		}

		else {
			log.info("redis://{}:{}/", envargs[0], redisPorts[0]);

			log.info("command:{},args:{}", command, args);
			redisClient = RedisClusterClient
					.create("redis://:YTMxYjFjYWIzYjc4NDdjNzk0OTJhODY3@" + envargs[0] + ":" + redisPorts[0] + "/");

			connection = redisClient.connect();

			syncCommands = connection.sync();

			if (command.toLowerCase().equals("set")) {
				String[] splittedArgs = args.split(" ");
				String ret = syncCommands.setex(splittedArgs[0], 60 * 60L, splittedArgs[1]);

				if (ret.equals("OK")) {
					redisResponseVo.setStatus("0");
					redisResponseVo.setCount(1);
				} else {
					redisResponseVo.setStatus("-1");
				}
				redisResponseVo.setMessage("return value is " + ret);

			}

			else if (command.toLowerCase().equals("del")) {

				String[] splittedArgs = args.split(" ");
				Long ret = syncCommands.del(splittedArgs[0]);

				if (ret == 1L) {
					redisResponseVo.setStatus("0");
					redisResponseVo.setCount(1);
				} else {
					redisResponseVo.setStatus("-1");
				}
				redisResponseVo.setMessage("return value is " + String.valueOf(ret));

			}
			else if (command.toLowerCase().equals("get")) {

				String[] splittedArgs = args.split(" ");
				String ret = syncCommands.get(splittedArgs[0]);

				if (StringUtils.isNotEmpty(ret)) {
					redisResponseVo.setStatus("0");
					redisResponseVo.setCount(1);
					JSONArray data = new JSONArray();
					data.add(ret);
					redisResponseVo.setData(data);
					redisResponseVo.setMessage("got");
				} else {
					redisResponseVo.setStatus("-1");
				}
				
				
			   log.info(ret);

			}
			
			else {

				log.warn("redis command not recognize");

			}

		}
		if (connection != null)
			connection.close();
		if (redisClient != null)
			redisClient.shutdown();

		// new RedisURI("localhost", 6379, 60, TimeUnit.SECONDS);

		return redisResponseVo;

	}

	public String setCommand() {

		return null;
	}

	public Long delCommand() {

		return null;
	}

	public List<String> keysCommand() {

		return null;
	}

	public static void main(String[] args) {

		RedisClusterClient redisClient = null;
		StatefulRedisClusterConnection<String, String> connection = null;
		RedisClusterCommands<String, String> syncCommands = null;
		
		String[] envargs = EnvConfig.envMap.get("env_150").serviceDbConf.get("redis");

		String[] redisPorts = envargs[1].split("\\|");

		for (String port : redisPorts) {
			log.info(port);
		}
		
		 redisClient = RedisClusterClient
		   .create("redis://:YTMxYjFjYWIzYjc4NDdjNzk0OTJhODY3@" + envargs[0] + ":" + redisPorts[0] + "/");
		 
		 log.info("redis://:YTMxYjFjYWIzYjc4NDdjNzk0OTJhODY3@" + envargs[0] + ":" + redisPorts[0] + "/");

		connection = redisClient.connect();
		
		syncCommands = connection.sync();
		
		String ret = syncCommands.get("ROUTER:CONFIG:JSON");

		if (StringUtils.isNotEmpty(ret)) {

		} else {
			
		}
		//redisResponseVo.setMessage("return value is " + ret);
		
	   log.info(ret);

	}

}
