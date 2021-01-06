package com.tangmiyi.future.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.security.Key;
import java.util.List;

/**
 * JWT 工具类
 */
@Component
@Slf4j
@RefreshScope
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = 1L;


    @Value("${jwt.header:Authorization}")
    private String tokenHeader;

    @Value("${jwt.token.head:Bearer}")
    private String tokenHead;

    private final static String KEY = "fekwjfklc";

    /**
     * 获取密钥
     *
     * @return
     */
    private Key getKeyInstance() {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        return signingKey;
    }

    /**
     * 解析jwtToken
     *
     * @param jwtToken
     * @return
     */
    public Claims verifyJWT(String jwtToken,String audience,String iss) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(getKeyInstance()).requireAudience(audience).requireIssuer(iss)
                    .parseClaimsJws(jwtToken).getBody();
        } catch (Exception e) {
            log.error("JWT signature does not match:"+ jwtToken);
            return null;
        }
        return claims;
    }

    /**
     * 从http head获取token
     * @return
     */
    public String getTokenFromHttpHead(HttpHeaders headers) {
        List<String> tokenHeader = headers.get(this.tokenHeader);
        if (!CollectionUtils.isEmpty(tokenHeader) && tokenHeader.get(0).startsWith(tokenHead)) {
            return tokenHeader.get(0).substring(tokenHead.length());
        }
        return null;
    }
}