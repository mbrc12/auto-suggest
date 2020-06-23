package io.mbrc.autosuggest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AutoSuggestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoSuggestApplication.class, args);
	}

	public @Bean
	String appName () {
		return "auto-suggest";
	}
}
