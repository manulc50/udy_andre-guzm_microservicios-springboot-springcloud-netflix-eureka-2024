package com.formacionbdi.springboot.app.usuarios.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;
import com.formacionbdi.springboot.app.usuarios.oauth.services.IUsuarioService;

import brave.Tracer;
//import brave.Tracer;
import feign.FeignException;

// Esta clase se trata de un manejador de eventos que se va a disparar tanto si ha habido éxito o fracaso en el proceso de autenticación y va a realizar una tarea determinada para el caso de éxito y otra tarea para el caso de fallo 
// Tenemos que crear un bean de esta clase para poderlo inyectar y registrar en la clase de configuración de Spring Security

// Nota: Este evento se lanza 2 veces; una vez cuando se produce la autenticación del cliente(Por ejemplo: el cliente con id "frontendapp") y otra vez cuando se produce la autenticación del usuario

@Component // Indicamos que esta clase se trata de un componente de Spring para que lo almacene como un bean en su contenedor o memoria y,de esta forma,poder inyectarlo y usarlo en otra parte del proyecto.Concretamente,lo inyectaremos para poderlo registrar en la clase de configuración de Spring Security
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
	
	// Habilitamos el log para esta clase
	private Logger log = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "IUsuarioService".Esta interfaz es implementada por nuestra clase "UsuarioService"
	@Autowired
	private IUsuarioService usuarioService; // Es un bean de la capa de servicio que se encarga de obtener los datos de un usuario(a partir de su username) o de actualizarlos(a partir de los datos de la petición http y de su id) usando nuestro cliente Api Rest Feign de Netflix que consume los recursos del microservicio "servicio-usuarios"

	// Este bean,recuperado de la memoria o contenedor de Spring, nos permite añadir atributos o tags personalizados a las trazas generadas por Spring Cloud Sleuth para este micro-servicio
	@Autowired
	private Tracer tracer;
	
	// Sobrescribimos esta método para implementar la tarea a realizar en caso de autenticación con éxito
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		// Si se trata del evento sobre la autenticación del cliente(Por ejemplo: el cliente con id "frontendapp"), no hacemos nada porque sólo nos interesa realizar esta tarea cuando la autenticación del usuario es correcta
		// 2 formas de verificar ésto:
		// Primera forma - El problema de esta forma es que es específica para cada cliente id. Si tuvieramos varios clientes registrados, tendríamos que verificar cada uno de ellos
		// La forma de abajo es mejor porque es genérica para todos los clientes
		/*if(authentication.getName().equalsIgnoreCase("frontendapp"))
			return;
		*/
		// Segunda forma
		if(authentication.getDetails() instanceof WebAuthenticationDetails)
			return;
		
		// Obtenemos los datos del usuario a través de nuestra capa de servicio "usuarioService" que a su vez hace uso de nuestro cliente Api Rest Feign para consumir los recursos del microservicio "servicio-usuarios"
		// No hace falta poner un try-catch para controlar la excepción "Feign Exception",como sí ocurre en el método "publishAuthenticationFailure()",porque como estamos en el método que controla el evento para casos de éxito en la autenticación,obviamente el usuario existe en el sistema
		Usuario usuario = usuarioService.findByUsername(authentication.getName()); // El username lo podemos obtener a través del método "getName()" o "getPrincipal()".Lo que pasa es que el método "getName()" es una manera más ràpida de obtener el usuario en comparación con "getPrincipal()" que requiere hacer un cast a "UserDetails"
		// Creamos un mensaje con el texto "Success Login: " + el nombre del usuario autenticado con éxito para mostrarlo por consola y escribirlo en el log como información
		String mensaje = "Success Login: " + usuario.getUsername();
		// Mostramos por consola el texto de "mensaje"
		System.out.println(mensaje);
		// Escribimos en el log como información el texto de "mensaje"
		log.info(mensaje);
		
		// Si el usuario ya tenía previamente intentos de autenticación fallidos y como estamos en el evento que maneja la utenticación para casos de éxito,significa que esta vez el usuario sí se ha autenticado correctamente en el sistema,y por eso,restablecemos su contador de intentos a 0
		if(usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			// Por último,haciendo uso de nuestra capa de servicio "usuarioService" actualizamos los datos de este usuario en el microservicio "servicio-usuarios" mediante el método "update()" que a su vez hace uso de nuestro cliente Api Rest Feign para consumir los recursos de dicho microservicio
			usuarioService.update(usuario,usuario.getId());
		}
	}

	// Sobrescribimos esta método para implementar la tarea a realizar en caso de autenticación fallida
	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		// Si se trata del evento sobre la autenticación del cliente(Por ejemplo: el cliente con id "frontendapp"), no hacemos nada porque sólo nos interesa realizar esta tarea cuando la autenticación del usuario es incorrecta
		// 2 formas de verificar ésto:
		// Primera forma - El problema de esta forma es que es específica para cada cliente id. Si tuvieramos varios clientes registrados, tendríamos que verificar cada uno de ellos
		// La forma de abajo es mejor porque es genérica para todos los clientes
		/*if(authentication.getName().equalsIgnoreCase("frontendapp"))
			return;
		*/
		// Segunda forma
		if(authentication.getDetails() instanceof WebAuthenticationDetails)
			return;
		
		// Creamos un mensaje con el texto "Error en el Login: " + el mensaje de error obtenido de la excepción ocurrida por la autenticación fallida del usuario
		String mensaje = "Error en el Login: " + exception.getMessage();
		// Escribimos en el log como error el texto de "mensaje"
		log.info(mensaje);
		// Mostramos por consola el texto de "mensaje"
		System.out.println(mensaje);
		
		// Intenta obtener los datos del usuario a través de nuestra capa de servicio "usuarioService" que a su vez hace uso de nuestro cliente Api Rest Feign para consumir los recursos del microservicio "servicio-usuarios"
		try {
			// Creamos un StringBuilder para ir concatenando los mensajes de error
			StringBuilder errors = new StringBuilder();
			// Añadimos al StringBuilder de errores el texto contenido en "mensaje"
			errors.append(mensaje);
			
			Usuario usuario = usuarioService.findByUsername(authentication.getName()); // El username lo podemos obtener a través del método "getName()" o "getPrincipal()".Lo que pasa es que el método "getName()" es una manera más ràpida de obtener el usuario en comparación con "getPrincipal()" que requiere hacer un cast a "UserDetails"
			// Si se trata de la primera vez que el usuario se ha autenticado mal en el sistema,inicializamos el número de intento a 0
			if(usuario.getIntentos() == null)
				usuario.setIntentos(0);
			
			// Mostramos en el log como información el texto "El número de intentos actual del usuario es:" + el número de intentos actual del usuario
			log.info("El número de intentos actual del usuario es: " + usuario.getIntentos());
			
			// Incrementamos el número de intentos actual del usuario en una unidad
			usuario.setIntentos(usuario.getIntentos()+1);
			
			// Mostramos en el log como información el texto "El número de intentos del usuario después de actualizarlo es:" + el número de intentos del usuario tras actualizarlo
			log.info("El número de intentos del usuario después de actualizarlo es: " + usuario.getIntentos());
			
			// Añadimos al StringBuilder de errores el texto " - Intentos del login: " + el número actualizado de intentos de hacer login del usuario
			errors.append(" - Intentos del login: " + usuario.getIntentos());
			
			// El usuario tiene 3 intentos para autenticarse correctamente en el sistema
			// Si el número de intentos actualizado del usuario alcanza este número total de intentos,le deshabilitamos sus cuente poniendo su propiedad "enabled" a false
			if(usuario.getIntentos() == 3) {
				String errorMaxIntentos = String.format("El usuario %s ha sido deshabilitado por alcanzar el máximo número de intentos",usuario.getUsername());
				// Mostramos en el log como error el texto "El usuario %s deshabilitado por alcanzar el máximo número de intentos" + el username del usuario
				log.error(errorMaxIntentos);
				// Añadimos al StringBuilder de errores el texto " - " + el texto anterior contenido en "errorMaxIntentos"
				errors.append(" - " + errorMaxIntentos);
				usuario.setEnabled(false);
			}
			
			// Haciendo uso de nuestra capa de servicio "usuarioService", actualizamos los datos de este usuario en el microservicio "servicio-usuarios" mediante el método "update()" que a su vez hace uso de nuestro cliente Api Rest Feign para consumir los recursos de dicho microservicio
			usuarioService.update(usuario,usuario.getId());
			
			// Por último, añadimos a la traza generada por Spring Cloud Sleuth el tag personalizado "error.mensaje" con los mensajes de error contenidos en el StringBuilder de errores
			tracer.currentSpan().tag("error.mensaje", errors.toString());
		}
		// En el caso de que el intento anterior devuelva una excepción de tipo "FeignException",escribimos en el log como un error el texto "El usuario %s no existe en el sistema" + el username del usuario obtenido con "authentication.getName()"
		catch(FeignException e) {
			log.error(String.format("El usuario %s no existe en el sistema",authentication.getName()));
		}
	}

}
