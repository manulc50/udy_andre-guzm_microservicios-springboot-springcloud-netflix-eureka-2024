package com.formacionbdi.springboot.app.usuarios.oauth.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

// Se utiliza la clase entidad "Usuario" de nuestra librería común de usuarios

// Con esta anotación indicamos que esta interfaz es un cliente Feign
// Esta anotación también hace que Spring almacene una implementación de esta interfaz como un bean para pordelo inyectar en otra parte de este proyecto
// Tenemos que indicar en el atributo "name" el nombre del microservicio al que queremos conectarnos y en el atributo "url" la ruta o path donde se localiza dicho microservicio
// Como vamos a usar el balanceador de carga en este microservicio para las peticiones de recursos al microservicio "servicio-usuarios",el atributo "url" lo quitamos de la anotación @FeignClient
// @FeignClient(name="servicio-productos",url="localhost:8001")
@FeignClient(name="servicio-usuarios")
public interface UsuarioFeignClient {
	// Con las anotaciones @GetMapping,@PostMapping,@PutMapping,etc... de Spring tenemos que indicar en esta interfaz cuáles son los endpoitns donde consumir los recursos
	
	// Los métodos indicados en esta interfaz se van a implementar de manera automática por debajo en tiempo de ejecución
	// Para ello,los métodos definidos en esta interfaz tienen que tener la misma estructura(mismo número y tipo de datos de argumentos de entrada,mismo tipo de retorno,etc...) y el mismo nombre que los métodos del controlador del microservicio que va a consumir
	// En este caso,los métodos definidos en esta interfaz tienen que tener la misma estructura y el mismo nombre que los métodos del controlador del microservicio "servicio-usuarios"
		
	// El recurso a consumir en el microservicio "servicio-usuarios" se localiza en el endpoint "/usuarios/search/buscar-username" para peticiones http de tipo Get
	// Con la anotación @RequestParam obtenemos el parámetro "username" que viaja en la url o path de la petición http y la pasamos al parámetro de entrada "username" de este método
	@GetMapping("/usuarios/search/buscar-username")
	public Usuario findByUsername(@RequestParam(name="username") String username);
	
	// El recurso a consumir en el microservicio "servicio-usuarios" se localiza en el endpoint "/usuarios/{id}" para peticiones http de tipo Put
	// Con la anotación @RequestBody indicamos que los datos del usuario a actualizar vienen en el body de la petición Http como un Json y automáticamente va a mapear dichos datos a las propiedades del objeto Usuario pasado como argumento de entrada a este método handler
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	@PutMapping("/usuarios/{id}")
	public Usuario update(@RequestBody Usuario usuario,@PathVariable Long id);

}
