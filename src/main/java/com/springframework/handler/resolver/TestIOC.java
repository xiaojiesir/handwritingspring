package com.springframework.handler.resolver;

import com.xiaojiesir.demo.controller.UserController;

public abstract class TestIOC {

	public static void main(String[] args) {
		MySpringApplicationContext applicationContext = new MySpringApplicationContext();

		UserController controller = (UserController) applicationContext.getBean("userController");
		controller.getUser();
	}

}
