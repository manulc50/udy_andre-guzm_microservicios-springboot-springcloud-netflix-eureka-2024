package com.formacionbdi.springboot.app.usuarios.oauth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//Esta clase se corresponde con la configuración de nuestro servidor de autenticación con Spring Security

@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "AuthenticationEventPublisher".Esta interfaz es implementada por la clase "AuthenticationSuccessErrorHandler"
	@Autowired
	private AuthenticationEventPublisher eventPublisher; // Este bean se corresponde con el manejador de eventos para tratar los casos de autenticación con éxito y fallida por parte del usuario
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "UserDetailsService".Esta interfaz es implementada por la clase "UsuarioService"
	@Autowired
	private UserDetailsService usuarioService; // Es un bean de la capa de servicio que se encarga de crear un usuario autenticado de Spring Security a partir de los datos de un usuario obtenidos a partir de su username usando nuestro cliente Api Rest Feign que consume los recursos del microservicio "servicio-usuarios"

	// Método que devuelve una instancia de BCryptPasswordEncoder.Esta instancia es la encargada en encriptar una password con el algoritmo BCrypt
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método(se trata del codificador de passwords con BCrypt) como un bean en su contenedor o memoria para que se pueda inyectar en la clase correspondiente al servidor de autorización con OAuth2
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// Sobrescribimos este método de WebSecurityConfigurerAdapter para registrar nuestra implementación de la interfaz UserDetailsService en el Authentication Manager de Spring Security
	@Override
	@Autowired // Con esta anotación recuperamos el bean correspondiente al Authentication Manager de Spring Security y lo inyectamos en el parámetro de entrada que se le pasa al método
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Registramos nuestra implementación de la interfaz UserDetailsService en el Authentication Manager
		// En este registro utilizamos como codificador de passwords el algoritmo BCypt
		// Cuando un usuario envíe sus credenciales para el login,la password enviada se va a encriptar con este algoritmo y se hará una comparación con la password correspondiente en la base de datos, que también está encriptada con este algoritmo, para ver si coincide
		auth.userDetailsService(usuarioService).passwordEncoder(passwordEncoder())
		.and()
		// Registramos nuestro manejador de eventos "eventPublisher" para los casos de éxito y de error en la autenticación del usuario en la configuración de Spring Security
		.authenticationEventPublisher(eventPublisher);
	}

	// Sobrescribimos este método de WebSecurityConfigurerAdapter para añadir la anotación @Bean
	@Override
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este métocdo(se trata del Authentication Manager de Spring Security) como un bean en su contenedor o memoria para que se pueda inyectar en la clase correspondiente al servidor de autorización con OAuth2
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	

	
}
