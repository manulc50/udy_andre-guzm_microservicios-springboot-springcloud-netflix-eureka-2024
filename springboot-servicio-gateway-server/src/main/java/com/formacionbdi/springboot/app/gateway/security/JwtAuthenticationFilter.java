package com.formacionbdi.springboot.app.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.google.common.net.HttpHeaders;

import reactor.core.publisher.Mono;

// Filtro para obtener el token JWT de las cabeceras de las peticiones http que requieran autenticación

@Component
public class JwtAuthenticationFilter implements WebFilter {
	
	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		// Usamos el método "justOrEmpty" porque hay peticiones http que no requieren autenticación y, por lo tanto, no llevan un token JWT en sus cabeceras. En éstos casos, se devuleve un flujo reactivo Mono vacío(Mono<Void>)
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
				.filter(authHeader -> authHeader.startsWith("Bearer "))
				// Si la petición http no lleva un token JWT en sus cabeceras, ejecutamos los siguientes filtros de la cadena de filtros y, después, devolvemos un flujo reactivo Mono vacío
				.switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
				// En caso contrario, eliminamos el prefijo "Bearer " de la cabecera "Authorization" para obtener el token JWT
				.map(token -> token.replace("Bearer ", ""))
				// Validamos el token JWT y generamos un nuevo flujo reactivo Mono con la autenticación
				.flatMap(token -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(null, token)))
				// Ejecutamos los siguientes filtros de la cadena de filtros y registramos la autenticación en el contexto de Spring Security
				.flatMap(authentication -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
	}

}
