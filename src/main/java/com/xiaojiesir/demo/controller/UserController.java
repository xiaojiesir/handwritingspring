package com.xiaojiesir.demo.controller;

import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.xiaojiesir.demo.pojo.User;
import com.xiaojiesir.demo.service.UserService;

@MyController
public class UserController {

	@MyAutowired
	private UserService userService;
	public void getUser(){
		User u = userService.getUser();
		System.out.println(u);
	}
}
