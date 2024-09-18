package com.formacionbdi.springboot.app.usuarios.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;
import com.formacionbdi.springboot.app.usuarios.oauth.clients.UsuarioFeignClient;

import brave.Tracer;
//import brave.Tracer;
import feign.FeignException;

// Se utiliza la clase entidad "Usuario" de nuestra librería común de usuarios

// Esta clase de la capa de servicio se encarga de implementar la interfaz UserDetailsService de Spring Security para crear un usuario autenticado de Spring Security a partir de la implementación del método loadUserByUsername

// Indicamos que esta clase se trata de una clase servicio de Spring.De esta manera,Spring va a almacenar un bean de esta clase en su memmoria o contenedor para poderlo inyectar en otra parte del proyecto
@Service 
public class UsuarioService implements UserDetailsService,IUsuarioService{
	
	// Habilitamos el log en esta clase
	private Logger log = LoggerFactory.getLogger(UsuarioService.class);
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "UsuarioFeignClient".Esta interfaz es implementada por Spring al usar la anotación @FeignClient en dicha interfaz
	@Autowired
	private UsuarioFeignClient clienteFeign; // Este bean se trata de un cliente API Rest Feign que nos permite consumir recursos que se localizan en otros microservicios.En este caso,lo usamos para consumir recursos del microservicio "servicio-usuarios"

	// Este bean,recuperado de la memoria o contenedor de Spring, nos permite añadir atributos o tags personalizados a las trazas generadas por Spring Cloud Sleuth para este micro-servicio
	@Autowired
	private Tracer tracer;
	
	// Sobrescribimos este método de la interfaz propia de Spring UserDetailsService para crear un usuario autenticado de Spring Security a partir de los datos de un usuario obtenidos a partir de su username usando nuestro cliente Api Rest Feign
	// UserDetails es un tipo de interfaz que representa un usuario de Spring Security,es decir,un usuario autenticado
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Intenta obtener los datos del usuario a partir de su username usando nuestro cliente Api Rest Feign que consume recursos del microservicio "servicio-usuarios"
		try {
			Usuario usuario = clienteFeign.findByUsername(username);

			// Para crear posteriormente un objeto User que implemente la interfaz UserDetails,tenemos que crear una lista de objetos GrantedAuthority a partir de los roles del usuario obtenido en el paso anterior.
			// Como los roles del usuario vienen en una lista de objetos Role,tenemos que convertir dicha lista en una lista de objetos GrantedAuthority.Para ello,hacemos uso de stream y map de Java 8.
			// GrantedAuthority es una interfaz que,en nuestro caso,se implementa con la clase SimpleGrantedAuthority.Entonces,con el operador map,por cada objeto Role de la lista de roles,creamos un objeto SimpleGrantedAuthority con el nombre del role
			// Después,del stream de objetos SimpleGrantedAuthority creado como resultado de aplicar el operador map,creamos una lista de objetos SimpleGrantedAuthority
			List<GrantedAuthority> authorities = usuario.getRoles().stream()
					.map(role -> new SimpleGrantedAuthority(role.getNombre()))
					.peek(authority -> log.info("Role: " + authority.getAuthority())) // peek nos permite realizar una determinada acción sobre un stream que no supone su  alteración o modificación.En este caso lo usamos para escribir en log como información el nombre de cada role del usuario 
					.collect(Collectors.toList());
			// Escribe en el log como información el username del usuario autenticado
			log.info("Usuario autenticado: " + username);
			// Creamos y devolvemos una instancia User,que es una clase que implementa la interfaz UserDetails,a partir del username,la password,la propiedad que indica si se trata de un usuario activo y los roles del usuario como una lista de objeto GrantedAuthority.El resto de parámetros puestos a true,son los valores por defecto
			return new User(usuario.getUsername(),usuario.getPassword(),usuario.getEnabled(),true,true,true,authorities);
	
		}
		// En el caso de que el intento anterior devuelva una excepción de tipo "FeignException",significa que el usuario para ese username no existe en el sistema
		catch(FeignException e) {
			String error = "Error en el login. No esiste el usuario '" + username + "' en el sistema";
			// Escribimos en el log como un error el texto "Error en el login. No esiste el usuario '" + el username del usuario que se le pasa como parámetro a este método + el texto "' en el sistema"
			log.error(error);
			// Añadimos a la traza generada por Spring Cloud Sleuth el tag personalizado "error.mensaje" con el texto anterior + el mensaje de la excepción
			tracer.currentSpan().tag("error.mensaje", error + ": " + e.getMessage());
			// Y lanzamos la excepción UsernameNotFoundException con el texto anterior
			throw new UsernameNotFoundException(error);
		}
	}

	// Método que localiza y devuelve los datos de un usuario dado su username
	@Override
	public Usuario findByUsername(String username) {
		// Obtenemos y devolvemos los datos del usuario a partir de su username usando nuestro cliente Api Rest Feign que consume recursos del microservicio "servicio-usuarios"
		return clienteFeign.findByUsername(username);
	}

	// Método que actualiza los datos de un usuario dado su id
	@Override
	public Usuario update(Usuario usuario, Long id) {
		// Actualizamos y devolvemos los datos del usuario a partir de su id usando nuestro cliente Api Rest Feign que consume recursos del microservicio "servicio-usuarios"
		return clienteFeign.update(usuario,id); 
	}

}
