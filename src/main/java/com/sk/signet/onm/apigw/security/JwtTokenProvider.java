package com.sk.signet.onm.apigw.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 토큰 프로바이더 
 * @packagename : com.sk.signet.onm.apigw.security
 * @filename 	: JwtTokenProvider.java 
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
public class JwtTokenProvider {

	@Value("${token.access-expired-time}")
    private long ACCESS_EXPIRED_TIME;

    @Value("${token.refresh-expired-time}")
    private long REFRESH_EXPIRED_TIME;

    @Value("${token.secret}")
    private String SECRET;

    public String createJwtAccessToken(String userId, String uri, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(
                        new Date(System.currentTimeMillis() + ACCESS_EXPIRED_TIME)
                )
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .setIssuer(uri)
                .compact();
    	
    }

    public String createJwtRefreshToken() {
        Claims claims = Jwts.claims();
        claims.put("value", UUID.randomUUID());

        return Jwts.builder()
        		.setHeaderParam("typ", "JWT")
        		.setHeaderParam("regDate", System.currentTimeMillis())
                .addClaims(claims)
                .setExpiration(
                        new Date(System.currentTimeMillis() + REFRESH_EXPIRED_TIME)
                )
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
    
    public <T> String createToken(String key, T data, String subject){
		String jwt = Jwts.builder()
						 .setHeaderParam("typ", "JWT")
						 .setHeaderParam("regDate", System.currentTimeMillis())
						 .setSubject(subject)
						 .claim(key, data)
						 .setExpiration(new Date(System.currentTimeMillis()+ 1 * ACCESS_EXPIRED_TIME)) //30분
						 .signWith(SignatureAlgorithm.HS256, SECRET)
						 .compact();
//						 .signWith(SignatureAlgorithm.HS256, this.generateKey())
//						 .compact();
		return jwt;
	}
    
    public String refreshToken(String jwt) {
    	try {
    		Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(jwt).getBody();
    		
    		String subject = claims.getSubject();
			Map<String,Object> data=(Map<String,Object>)claims.get("userInfo");
	
			String token=this.createToken("data", data, subject);
			
			claims.clear(); //기존 생성 토큰 제거(무조건 신규 생성 함) 
			return token;
		} catch (Exception e) {
			if(log.isInfoEnabled()){
				e.printStackTrace();
			}else{
				log.error(e.getMessage());
			}
			throw e;
		}
    }



    public String getSubject(String token) {
        return getClaimsFromJwtToken(token).getSubject();
    }

    private Claims getClaimsFromJwtToken(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public String getRefreshTokenId(String token) {
        return getClaimsFromJwtToken(token).get("value").toString();
    }

    public List<String> getRoles(String token) {
        return (List<String>) getClaimsFromJwtToken(token).get("roles");
    }

    public void validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
        } catch (SignatureException  | MalformedJwtException |
                UnsupportedJwtException | IllegalArgumentException | ExpiredJwtException jwtException) {
        	log.error("JWT Token Validate Error token: {} ", token);
            throw jwtException;
        }
    }

    public boolean equalRefreshTokenId(String refreshTokenId, String refreshToken) {
        String compareToken = this.getRefreshTokenId(refreshToken);
        return refreshTokenId.equals(compareToken);
    }
}
