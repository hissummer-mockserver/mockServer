package com.hissummer.mockserver.mgmt.controller;

import org.bouncycastle.cert.ocsp.Req;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@CrossOrigin(origins = "*",methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,RequestMethod.PUT,RequestMethod.OPTIONS})
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