package com.revature.p1.web.util.security;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.web.dtos.Principal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenGenerator {
    private final JwtConfig jwtConfig;


    public TokenGenerator(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String createToken(Principal subject) {

        long now = System.currentTimeMillis();

        JwtBuilder tokenBuilder = Jwts.builder()
                .setId(subject.getId())
                .setSubject(subject.getUsername())
                .claim("privilege", subject.getUserPrivileges()) // specify the role of the user for whom this token is for
                .setIssuer("revature")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtConfig.getExpiration()))
                .signWith(jwtConfig.getSigAlg(), jwtConfig.getSigningKey());

        return jwtConfig.getPrefix() + tokenBuilder.compact();

    }

    public JwtConfig getJwtConfig() {
        return jwtConfig;
    }
}
