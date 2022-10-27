package com.sk.signet.onm.apigw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

import com.sk.signet.onm.apigw.handler.GlobalExceptionHandler;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class OnmApigwApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnmApigwApplication.class, args);
	}
	
	@Bean
	public ErrorWebExceptionHandler globalExceptionHandler() {
		return new GlobalExceptionHandler();
	}

	@Bean
	public KeyResolver tokenKeyResolver() {
		return exchange -> Mono.just(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0));
	}

}
