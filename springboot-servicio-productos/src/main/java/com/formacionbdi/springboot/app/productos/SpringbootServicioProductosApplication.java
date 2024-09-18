package com.formacionbdi.springboot.app.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

// Como usamos la clase entidad "Producto" de nuestra librería común "commons", que pertenece a otro proyecto distinto a este,y como dicha clase entidad contiene anotaciónes de Spring Data JPA para la persistencia de objetos en la base de datos,pero se encuentra fuera del paquete raíz de este proyecto, que es "com.formacionbdi.springboot.app.productos",tenemos que usar la anotación @EntityScan
// En esta anotación tenemos que indicar el paquete de la librería común donde se encuentra la clase entidad "Producto" para que este proyecto escané dicha entidad y tenga en cuenta sus anotaciones de Spring Data JPA
@EntityScan({"com.formacionbdi.springboot.app.commons.models.entity"})
@EnableEurekaClient // Opcional. Con tener la dependencia "spring-cloud-starter-netflix-eureka-client", es suficiente para registrarse en el servidor de descubrimiento Eureka
@SpringBootApplication
public class SpringbootServicioProductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioProductosApplication.class, args);
	}

}
