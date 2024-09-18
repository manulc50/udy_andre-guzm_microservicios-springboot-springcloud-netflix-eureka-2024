package com.formacionbdi.springboot.app.usuarios.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.password.PasswordEncoder;


// Implementamos la interfaz CommandLineRunner para implementar y ejecutar una serie de tareas antes de que se lance este proyecto Spring Boot


@EnableEurekaClient // Opcional. Con tener la dependencia "spring-cloud-starter-netflix-eureka-client", es suficiente para registrarse en el servidor de descubrimiento Eureka
@EnableFeignClients // Esta anotación nos permite habilitar el cliente Feign que tengamos implmentado en este proyecto así como inyectar este cliente en cualquier parte del proyecto
@SpringBootApplication
public class SpringbootServicioOauthApplication implements CommandLineRunner{
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "PasswordEncoder".Esta interfaz es implementada por la clase de Spring Security BCryptPasswordEncoder
	@Autowired
	private PasswordEncoder passwordEncoder; // Este bean se trata del codificador de passwords con el algoritmos BCrypt

	public static void main(String[] args) {
		SpringApplication.run(SpringbootServicioOauthApplication.class, args);
	}

	// Sobrescribimos este método de CommandLineRunner para realizar la tarea de generar e imprimir por pantalla 4 passwords encriptada con BCrypt a partir del password="12345"
	@Override
	public void run(String... args) throws Exception {
		// Password a encriptar
		String password = "12345";
		// Generamos 4 passwords encriptadas con el algoritmo BCrypt y las sacamos por pantalla
		for(int i=0;i<4;i++) {
			String passwordBCrypt = passwordEncoder.encode(password);
			System.out.println(passwordBCrypt);
		}
			
		
	}

}
