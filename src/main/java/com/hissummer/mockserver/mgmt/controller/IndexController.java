package com.hissummer.mockserver.mgmt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*")
@Controller
public class IndexController {

	/*
	 * return index.html homepage
	 */
	@GetMapping(value = "/")
	public String index(Model model) {
		log.info("redirect to index.html page.");
		return "index";

	}

}