package com.formacionbdi.springboot.app.gateway.security;

import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManagerJwt implements ReactiveAuthenticationManager {
	
	@Value("${config.security.oauth.jwt.key}")
	private String jwtKey;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		// El método "getCredentials" nos da el token JWT
		return Mono.just(authentication.getCredentials().toString())
				.map(token -> {
					// Códificamos la clave secreta en base 64 para que sea algo más robusta
					SecretKey key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtKey.getBytes()));
					// Valida el token JWT a partir de la clave secreta y obtenemos los claims
					return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
				})
				.map(claims -> {
					// Obtenemos el username y los roles de los claims
					String username = claims.get("user_name", String.class);
					@SuppressWarnings("unchecked")
					List<String> roles = claims.get("authorities", List.class);
					// Convertimos cada role en un objeto de tipo "SimpleGrantedAuthority" de Spring Security(Esta clase implementa la clase "GrantedAuthority")
					Collection<GrantedAuthority> authorities = roles.stream()
							.map(SimpleGrantedAuthority::new) // Versión simplificada de la expresión "role -> new SimpleGrantedAuthority(role)"
							.collect(Collectors.toList());
					
					// Creamos un token de Spring Security a partir del username y de la lista de authorities
					return new UsernamePasswordAuthenticationToken(username, null, authorities);
				});
	}

}
