package com.sk.signet.onm.apigw.filter;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.sk.signet.onm.apigw.logging.RequestLoggingDecorator;
import com.sk.signet.onm.apigw.logging.ResponseLoggingDecorator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class LoggingWebFilter implements WebFilter {
	
	private String ignorePatterns;
	private boolean logHeaders;
	private boolean useContentLength;
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		
		final long startTime = System.currentTimeMillis();
		List<String> header = exchange.getRequest().getHeaders().get("Content-Length");
		
		ServerWebExchangeDecorator exchangeDecorator = new ServerWebExchangeDecorator(exchange) {
			@Override
			public ServerHttpRequest getRequest() {
				return new RequestLoggingDecorator(super.getRequest());
			}
		
			@Override
			public ServerHttpResponse getResponse() {
				return new ResponseLoggingDecorator(super.getResponse(), startTime);
			}
		};
		return chain.filter(exchangeDecorator)
				.doOnSuccess(aVoid -> {
					log.debug("LoggingWebFilter : doOnSuccess");
				})
				.doOnError(throwable -> {
					log.error("LoggingWebFilter : doOnError");
				});
		
	}

}
