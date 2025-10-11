package com.capstone.pillmeup.global.exception.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

@Component
public class JwtProvider {

	private final Key key;

    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityInSeconds;

    // secret을 이용해 서명 키 초기화
    public JwtProvider(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성 (role은 항상 USER)
     * sub(주체)는 memberId 문자열
     */
    public String generateAccessToken(Long memberId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + (accessTokenValidityInSeconds * 1000));

        return Jwts.builder()
                .setSubject(String.valueOf(memberId)) // sub = memberId
                .claim("role", "USER")           // 고정 권한
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검증 (서명/형식/만료)
     * - 유효: true, 아니면 false
     * - 만료/변조/형식오류 모두 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException
                 | IllegalArgumentException e) {
            return false; // 서명/형식 문제
        } catch (ExpiredJwtException e) {
            return false; // 만료
        }
    }

    /** 토큰에서 memberId(sub) 추출 */
    public Long getMemberId(String token) {
        String sub = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.valueOf(sub);
    }

}
