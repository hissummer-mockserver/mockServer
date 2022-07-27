package com.hissummer.mockserver.mock.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScriptEnginePool {
	private static final int DEFAULT_CAPACITY = 200;
	private int capacity;
	private LinkedBlockingQueue<ScriptEngine> sparePool;

	@PostConstruct
	public void initGroovyScriptEngine() {

		this.sparePool = new LinkedBlockingQueue<>();

		for (int i = 0; i < DEFAULT_CAPACITY; i++) {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
			this.sparePool.add(engine);

		}

	}

	/**
	 * if no spare engine, will hang.
	 */
	public ScriptEngine getSpareEngine() {

		try {
			return this.sparePool.poll(30L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		}

	}

	/**
	 * if no spare engine, will hang.
	 */
	public void releaseEngine(ScriptEngine engine) {

		if (!this.sparePool.offer(engine)) {
			log.error("add engine! ");
		}

	}

}
