package com.formacionbdi.springboot.app.item;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
public class AppConfig {
	
	// Este método devuelve una instancia de RestTemplate que es un cliente que trabaja con API Rest y nos permite acceder a recursos que están en otros microservicios
	@Bean("clienteRest") // Spring va a almacenar el retorno de este método como un bean en su contenedor o memoria y lo va a llamar "clienteRest"
	@LoadBalanced // Con esta anotación, de manera automática,va a integrar el balanceador de carga Ribbon de Netflix con el cliente API Rest de Spring "RestTemplate"
	public RestTemplate registarRestTemplate() {
		return new RestTemplate(); // 
	}

}
