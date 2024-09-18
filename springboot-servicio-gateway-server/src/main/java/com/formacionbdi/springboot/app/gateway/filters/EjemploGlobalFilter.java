package com.formacionbdi.springboot.app.gateway.filters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

// Esta clase es un filtro global que se aplica a todas las peticiones http que pasen por este Api Gateway

@Component
public class EjemploGlobalFilter implements GlobalFilter, Ordered {
	
	private static final Logger logger = LoggerFactory.getLogger(EjemploGlobalFilter.class);

	// El objeto "exchange" de tipo "ServerWebExchange" contiene el request y el response de la petición http
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		logger.info("Ejecutando filtro pre");
		
		// Por defecto, el request de la petición http es inmutable
		// Entonces, para poder modificar el request, tenemos que usar el método "mutate" para que se haga mutable
		// Añade la cabecera "token" con valor "12345" al request de la petición http
		exchange.getRequest().mutate().headers(h -> h.add("token", "12345"));
		
		// Ejecuta el siguiente filtro de la cadena de filtros hasta ejecutar la petición http
		// Cuando la petición http ha finalizado su ejecución, se ejecuta la implementación del método "then", es decir, la implementación del método "then" es la parte post filtro
		// El método "then" espera a que termine de emitirse el flujo reactivo original para comenzar a emitir un nuevo flujo reactivo(aquel que se indica dentro del "then")
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			logger.info("Ejecutando filtro post");
			
			// Obtenemos el valor de la cabecera "token" del request de la petición http y lo establecemos en la cabecera "token" de la respuesta de esa petición http
			String tokenValue = exchange.getRequest().getHeaders().getFirst("token");
			exchange.getResponse().getHeaders().add("token", tokenValue);
			
			// Añade la cookie "color" con valor "rojo" a la respuesta de la petición http
			exchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "rojo").build());
			
			// Modificamos el tipo de contenido de la respuesta de la petición http para devolver un texto plano en vez de un Json
			//exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
		}));
	}

	// Este filtro se ejecutara antes en la cadena de filtros que el filtro de tipo "Gateway Filter Factory" de la clase "EjemploGatewayFilterFactory" porque tiene un número de orden menor(1 con respecto a 2)
	// Nota: Un filtro con menor número para el orden entrará antes en la cadena de filtros que un filtro con mayor número pero saldrá después que ese filtro, es decir, siempre se sale de la cadena de filtros a la inversa respecto a su entrada
	// Nota: Cuando un filtro tiene una alta prioridad en la cadena de filtros debido a un número de orden negativo, la respuesta de la petición http es de sólo lectura y no es posible realizar modificaciones en esa respuesta
	//       Para poder modificar la respuesta de una petición http, el número de orden tiene que ser 0 o positivo
	@Override
	public int getOrder() {
		//return -1; // Respuesta de la petición http en modo sólo lectura
		return 1;
	}

}
