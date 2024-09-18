package com.formacionbdi.springboot.app.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Habilitamos Hystrix de Netflix que se basa en el patrón llamado "Circuit Breaker" y nos permite manejar errores en nuestros microservicios.Gestiona la comunicación entre microservicios y añade lógica de tolerancia a fallos,latencias,timeouts,etc...
@EnableCircuitBreaker
@EnableEurekaClient // Opcional. Con tener la dependencia "spring-cloud-starter-netflix-eureka-client", es suficiente para registrarse en el servidor de descubrimiento Eureka
// Habilitamos un cliente Ribbon de Netflix para el balanceador de carga de las peticiones de recursos al microservicio "servicio-productos"
// De manera automática,el cliente API Rest Feign de Netflix se integra con Ribbon también de Netflix
// Esta anotación no hace falta si vamos a registrar el microservicio en el servidor Eureka de Netflix.Esto es así porque dicho servidor ya se encuentra integrado por defecto con el balanceador de carga Ribbon
//@RibbonClient(name="servicio-productos")
@EnableFeignClients // Esta anotación nos permite habilitar el cliente Feign de Netflix que tengamos implmentado en este proyecto así como inyectar este cliente en cualquier parte del proyecto
@SpringBootApplication
//Sobrescribimos esta anotación incluida en la anotación anterior @SpringBootApplication para desactivar o excluir el proceso de auto-configuración del DataSource
//Esto lo hacemos porque la clase entidad "Producto" que usamos en este proyecto, incluida en la librería común "commons", contiene anotaciones de Spring Data JPA y,como consecuencia de ello,Spring Boot por defecto realiza un proceso de auto-configuración del DataSource, pero realmente no vamos a usar ninguna base de datos en este proyecto,así que no se necesita auto-configurar ningún DataSource
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class SpringbootServicioItemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioItemApplication.class, args);
	}

}
