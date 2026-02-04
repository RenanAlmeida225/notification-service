package com.github.RenanAlmeida225.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootTest
@Import(NotificationServiceApplicationTests.TestMailConfig.class)
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Configuration
	static class TestMailConfig {
		@Bean
		JavaMailSender javaMailSender() {
			return new JavaMailSenderImpl();
		}
	}
}
