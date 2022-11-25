package com.sk.signet.onm.apigw.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.netty.channel.ChannelException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 전역 Exception 핸들러 
 * @packagename : com.sk.signet.onm.apigw.handler
 * @filename 	: GlobalExceptionHandler.java 
 * @since 		: 2022.10.27 
 * @description : 
 * =================================================================
 * Date				Author			Version			Note			
 * -----------------------------------------------------------------
 * 2022.10.27 		Heo, Sehwan		1.0				최초 생성
 * -----------------------------------------------------------------
 */
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

	@Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        List<Class<? extends RuntimeException>> jwtExceptions =
                List.of(SignatureException.class,
                        MalformedJwtException.class,
                        UnsupportedJwtException.class,
                        IllegalArgumentException.class);
        Class<? extends Throwable> exceptionClass = ex.getClass();

        Map<String, Object> responseBody = new HashMap<>();
        if (exceptionClass == ExpiredJwtException.class) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            responseBody.put("code", 2101);
            responseBody.put("staus", exchange.getResponse().getStatusCode().value());
            responseBody.put("message", "인증토큰이 만료되었습니다!");
        } else if (jwtExceptions.contains(exceptionClass)){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            responseBody.put("code", 2102);
            responseBody.put("staus", exchange.getResponse().getStatusCode().value());
            responseBody.put("message", "인증토큰이 변조되었습니다!");
        } else if (ex.getCause().getClass() == ConnectException.class) {
        	exchange.getResponse().setStatusCode(exchange.getResponse().getStatusCode());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        	responseBody.put("code", 4000);
        	responseBody.put("staus", 503);
            responseBody.put("message", "서버가 응답이 없습니다. 관리자에게 문의 바랍니다.");
        } else {
            exchange.getResponse().setStatusCode(exchange.getResponse().getStatusCode());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            responseBody.put("code", 9999);
            responseBody.put("staus", exchange.getResponse().getStatusCode().value());
            responseBody.put("message", ex.getMessage());
            log.error("Unknown exception cause class name : {}", ex.getCause().getClass().getName());
        }

        DataBuffer wrap = null;
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
            wrap = exchange.getResponse().bufferFactory().wrap(bytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return exchange.getResponse().writeWith(Flux.just(wrap));
    }
}
