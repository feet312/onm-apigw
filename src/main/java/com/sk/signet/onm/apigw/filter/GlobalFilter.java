package com.sk.signet.onm.apigw.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 전역 필터 
 * @packagename : com.sk.signet.onm.apigw.filter
 * @filename 	: GlobalFilter.java 
 * @since 		: 2022.10.27 
 * @description : 
 * =================================================================
 * Date				Author			Version			Note			
 * -----------------------------------------------------------------
 * 2022.10.27 		Heo, Sehwan		1.0				최초 생성
 * -----------------------------------------------------------------
 */
@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
	
		 public GlobalFilter(){
			 super(Config.class);
		 }
		 
//		 private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
//			 Flux body = serverHttpRequest.getBody();
//			 AtomicReference bodyRef = new AtomicReference<>();
//			 body.subscribe(buffer -> {
//			 CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
//			 DataBufferUtils.release(buffer);
//			 bodyRef.set(charBuffer.toString());
//			 });
//			 return bodyRef.get();
//		 }
	 
	 	@Override
	    public GatewayFilter apply(Config config) {
	        return (exchange, chain) -> {
	            ServerHttpRequest request = exchange.getRequest(); // reactive포함된거로 import
	            ServerHttpResponse response = exchange.getResponse();

//	            log.info("APIGW Global Filter baseMessgae: {}", config.getBaseMessage());

	            // Global pre Filter
	            if (config.isPreLogger()){
	            	log.info("Global Filter : [{}] {} {}" , request.getId(), request.getMethodValue(), request.getPath());	            	
	            		            		                
	            }

	            // Global Post Filter
	            //Mono는 webflux에서 단일값 전송할때 Mono값으로 전송
	            return chain.filter(exchange).then(Mono.fromRunnable(()->{

	                if (config.isPostLogger()){
	                    log.info("Global Filter End: response statuscode -> {}" , response.getStatusCode());
	                    log.info("Global Filter End: response Token -> {}" , response.getHeaders().get(HttpHeaders.AUTHORIZATION));
	                }
	            }));

	        };
	    }

	    @Data
	    public static class Config {
	        private String baseMessage;
	        private boolean preLogger;
	        private boolean postLogger;
	    }
}
