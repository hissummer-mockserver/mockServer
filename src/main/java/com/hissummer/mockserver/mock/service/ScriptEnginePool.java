package com.hissummer.mockserver.mock.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Component;

@Component
public class ScriptEnginePool {
	private static final int DEFAULT_CAPACITY = 20;
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
	public synchronized ScriptEngine getSpareEngine() {

		try {
			return this.sparePool.poll(60L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		}

	}

	/**
	 * if no spare engine, will hang.
	 */
	public synchronized void releaseEngine(ScriptEngine engine) {

		this.sparePool.add(engine);

	}

}
