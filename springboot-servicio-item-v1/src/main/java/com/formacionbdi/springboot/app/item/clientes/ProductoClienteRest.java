package com.formacionbdi.springboot.app.item.clientes;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

// Con esta anotación indicamos que esta interfaz es un cliente Feign de Netflix
// Esta anotación también hace que Spring almacene una implementación de esta interfaz como un bean para pordelo inyectar en otra parte de este proyecto
// Tenemos que indicar en el atributo "name" el nombre del microservicio al que queremos conectarnos y en el atributo "url" la ruta o path donde se localiza dicho microservicio
// Como vamos a usar el balanceador de carga Ribbon de Netflix en este microservicio para las peticiones de recursos al microservicio "servicio-productos",el atributo "url" lo quitamos de la anotación @FeignClient y lo pasamos al fichero de propiedades "application.properties"
// @FeignClient(name="servicio-productos",url="localhost:8001")
@FeignClient(name="servicio-productos")
public interface ProductoClienteRest {
	
	// Con las anotaciones @GetMapping,@PostMapping,@PutMapping,etc... de Spring tenemos que indicar en esta interfaz cuáles son los endpoitns donde consumir los recursos
	
	// Los métodos indicados en esta interfaz se van a implementar de manera automática por debajo en tiempo de ejecución
	// Para ello,los métodos definidos en esta interfaz tienen que tener la misma estructura(mismo número y tipo de datos de argumentos de entrada,mismo tipo de retorno,etc...) y el mismo nombre que los métodos del controlador del microservicio que va a consumir
	// En este caso,los métodos definidos en esta interfaz tienen que tener la misma estructura y el mismo nombre que los métodos del controlador "ProdcutoController" del microservicio "servicio-productos"
	
	// El recurso a consumir en el microservicio "servicio-productos" se localiza en el endpoint "/listar" para peticiones http de tipo Get
	@GetMapping("/listar")
	public List<Producto> listar();
	
	// El recurso a consumir en el microservicio "servicio-productos" se localiza en el endpoint "/ver/{id}" para peticiones http de tipo Get
	@GetMapping("/ver/{id}")
	public Producto detalle(@PathVariable Long id);
	
	// El recurso a consumir en el microservicio "servicio-productos" se localiza en el endpoint "/crear" para peticiones http de tipo Post
	@PostMapping("/crear")
	public Producto crear(@RequestBody Producto producto);
	
	// El recurso a consumir en el microservicio "servicio-productos" se localiza en el endpoint "/editar/{id}" para peticiones http de tipo Put
	@PutMapping("/editar/{id}")
	public Producto editar(@RequestBody Producto producto,@PathVariable Long id);
	
	// El recurso a consumir en el microservicio "servicio-productos" se localiza en el endpoint "/eliminar/{id}" para peticiones http de tipo Delete
	@DeleteMapping("/eliminar/{id}")
	public void eliminar(@PathVariable Long id);

}
