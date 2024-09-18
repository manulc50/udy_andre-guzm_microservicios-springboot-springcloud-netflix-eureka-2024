package com.formacionbdi.springboot.app.gateway.filters.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;


import reactor.core.publisher.Mono;


// Esta clase es un filtro de tipo Gateway Filter Factory que podemos añadir a rutas específicas configuradas en este Api Gateway(ver archivo "application.yml")

// Nota: El nombre de una clase que sea un filtro de tipo Gateway Filter siempre tiene que acabar con el texto "GatewayFilterFactory", y el texto que le precede se considera por defecto el nombre del filtro
// Por ejemplo, en este caso, como el nombre de la clase es "EjemploGatewayFilterFactory", el nombre por defecto del filtro es "Ejemplo"

// Nota: El nombre del filtro se puede cambiar sobrescribiendo el método "name" de la clase abstracta "AbstractGatewayFilterFactory"

// "AbstractGatewayFilterFactory" es una clase abstracta que recibe un genérico correspondiente a una clase de configuración

@Component
public class EjemploGatewayFilterFactory extends AbstractGatewayFilterFactory<EjemploGatewayFilterFactory.Configuracion> {
	
	private static final Logger logger = LoggerFactory.getLogger(EjemploGatewayFilterFactory.class);
	
	
	public EjemploGatewayFilterFactory() {
		super(Configuracion.class);
	}

	// Este filtro se ejecutara después en la cadena de filtros que el filtro global de la clase "EjemploGlobalFilter" porque tiene un número de orden mayor(2 con respecto a 1)
	// Nota: Un filtro con menor número para el orden entrará antes en la cadena de filtros que un filtro con mayor número pero saldrá después que ese filtro, es decir, siempre se sale de la cadena de filtros a la inversa respecto a su entrada
	// El argumento de entrada "config" de tipo "Configuracion" se corresponde con una instancia de nuestra clase de configuración "Configuracion"
	@Override
	public GatewayFilter apply(Configuracion config) {
		// Si no queremos indicar el orden de prioridad del filtro en la cadena de filtros, directamente implementamos la interfaz funcional "GatewayFilter" con una función lambda que implementa su método "filter"
		//return (exchange, chain) -> {
		
		// Si queremos indicar el orden de prioridad del filtro en la cadena de filtros, tenemos que devolver una instancia de la clase "OrderedGatewayFilter" que implementa las interfaces funcionales "GatewayFilter" y "Ordered"
		// La función lambda del primer argumento del constructor de esta clase implementa el método "filter" de dicha interfaz
		return new OrderedGatewayFilter((exchange, chain) -> {
			// Parte "pre" del filtro
			logger.info("Ejecutando pre gateway filter factory: " + config.getMensaje());
			
			return chain.filter(exchange).then(Mono.fromRunnable(() -> { // Parte "post" del filtro
				logger.info("Ejecutando post gateway filter factory: " + config.getMensaje());
				
				// Si el valor de la cookie está establecido en la clase de configuración, se crear una cookie en la respuesta de la petición http con ese valor y con el nombre indicado en esa clase de configuración
				Optional.ofNullable(config.getCookieValor()).ifPresent(valor -> 
						exchange.getResponse().addCookie(ResponseCookie.from(config.getCookieNombre(), valor).build()));
			}));
		}, 2); // Establece el número de orden 2 en la cadena de filtros
	}
	
	
	// Sobrescribimos este método para cambiar el nombre por defecto del filtro, "Ejemplo", por "EjemploCookie"
	@Override
	public String name() {
		return "EjemploCookie";
	}

	// Cuando aplicamos un filtro personalizado de tipo "Gateway Filter Factory" en un archivo de propiedades ".properties", ".yml" o ".yaml" usando la siguiente forma reducida:
	// "Ejemplo="Hola, este es mi mensaje personalizado", usuario, ManuelLorenzo"
	// Tenemos que sobrescribir este método para indicar el orden de pasada de los argumentos de ese filtro
	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList("mensaje", "cookieNombre", "cookieValor");
	}

	public static class Configuracion {
		private String mensaje;
		private String cookieValor;
		private String cookieNombre;
		
		public String getMensaje() {
			return mensaje;
		}
		
		public void setMensaje(String mensaje) {
			this.mensaje = mensaje;
		}
		
		public String getCookieValor() {
			return cookieValor;
		}
		
		public void setCookieValor(String cookieValor) {
			this.cookieValor = cookieValor;
		}
		
		public String getCookieNombre() {
			return cookieNombre;
		}
		
		public void setCookieNombre(String cookieNombre) {
			this.cookieNombre = cookieNombre;
		}
		
	}

}
