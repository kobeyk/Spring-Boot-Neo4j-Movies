package com.appleyk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageIndexController {

	@RequestMapping("/")
	public String index() {
		return "index";
	}

}
