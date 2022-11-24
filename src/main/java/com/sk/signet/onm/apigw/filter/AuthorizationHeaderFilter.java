package com.sk.signet.onm.apigw.filter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.signet.onm.apigw.security.JwtTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * 인증 필터 
 * @packagename : com.sk.signet.onm.apigw.filter
 * @filename 	: AuthorizationHeaderFilter.java 
 * @since 		: 2022.10.27 
 * @description : 
 * =================================================================
 * Date				Author			Version			Note			
 * -----------------------------------------------------------------
 * 2022.10.27 		Heo, Sehwan		1.0				최초 생성
 * -----------------------------------------------------------------
 */
@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

	
	@Autowired
    private ObjectMapper objectMapper;
	
	private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthorizationHeaderFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            HttpHeaders headers = request.getHeaders();
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "인증토큰이 없습니다.", HttpStatus.UNAUTHORIZED);
            }
            

            String authorizationHeader = headers.get(HttpHeaders.AUTHORIZATION).get(0);

            // JWT 토큰 판별
            String token = authorizationHeader.replace("Bearer", "");
            
            log.debug("apigw: AuthoHeaderFilter token: " + token);

            jwtTokenProvider.validateJwtToken(token);

            String subject = jwtTokenProvider.getSubject(token);

            if (subject.equals("customer")) return chain.filter(exchange);
            
            // TODO: 아래 권한 체크 부분 처리하기 전까지 그냥 넘긴다.  
            if (subject.equals("user")) return chain.filter(exchange);

            // 권한 체크 부분 일단 주석처리 
//            if (false == jwtTokenProvider.getRoles(token).contains("customer")) {
//                return onError(exchange, "권한 없음", HttpStatus.UNAUTHORIZED);
//            }

            ServerHttpRequest newRequest = request.mutate()
//                    .header("user-id", subject)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    // Mono(단일 값), Flux(다중 값) -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg, HttpStatus httpStatus) {
        log.error(errorMsg);
        
        Map<String, Object> responseBody = new HashMap<>();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        responseBody.put("code", 2100);
        responseBody.put("staus", httpStatus.value());
        responseBody.put("message", errorMsg);
        
        DataBuffer wrap = null;
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseBody);
            wrap = exchange.getResponse().bufferFactory().wrap(bytes);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        exchange.getResponse().writeWith(Flux.just(wrap));
        
        return exchange.getResponse().writeWith(Flux.just(wrap));

    }
}
