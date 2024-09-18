package com.formacionbdi.springboot.app.item.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;
import com.formacionbdi.springboot.app.item.models.service.IItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;



// Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

// Nota: La anotación "@RefreshScope" sólo es necesaria si se usa la anotación "@Value" para inyectar valores de propiedades de archivos de configuraciones. Si se usa una clase de configuración anotada con "@ConfigurationProperties" o se usa el bean que implementa la interfaz "Environment" para ese proposito, no es necesario usar la anotación "@RefreshScope"

@RefreshScope // Esta anotación nos permite actualizar en tiempo de ejecución y real los componentes como controladores,clases anotadas con @Component,@Service,@Configuration,etc...donde estamos inyectando propiedades de configuraciones con @Value
@RestController // Indicamos que esta clase se trata de un controlador Rest de Spring y,de esta manera,Spring lo va a almacenar en su memoria o contenedor como un bean.Esta anotación hace uso a su vez de las anotaciones de Spring @Controller y @ResponseBody para que los métodos handler implementados en este controlador devuelvan un Json con la información
public class ItemController {
	
	// Habilitamos el log en esta clase
	private static Logger log = LoggerFactory.getLogger(ItemController.class);
	
	@Autowired
	private CircuitBreakerFactory<?,?> cbFactory; // Para implementar el patrón "Circuit Breaker" de manera programática(Otra opción es usar anotaciones) usando Resilience4j
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "Environment".Esta interfaz es implementada por Spring
	@Autowired
	private Environment env; // Este bean de Spring contiene información sobre el contexto de desarrollo del proyecto.Lo vamos a usar para saber qué perfil o ámbito está activado,es decir, si está activado el entorno de Desarrollo o el de Producción
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "IItemService".Esta interfaz es implementada por la clase "itemServiceImpl"
	// Como tenemos dos implementaciones de la interfaz "IItemService",con la anotación @Qualfier indicamos qué bean queremos inyectar para implementar dicha interfaz.Esto no hace falta si decidimos usar la notación @Primary en la clase de la implementación de esta interfaz que queremos que se inyecte por defecto o como opción primaria 
	@Autowired
	@Qualifier("serviceFeign") // Descomentar si decidimos usar el bean "serviceFeign", que implementa un cliente API Rest Feign de Netflix
	//@Qualifier("serviceRestTemplate") // Descomentar si decidimos usar el bean "serviceRestTemplate", que implementa el cliente API Rest de Spring "RestTemplate"
	private IItemService itemService;  // Este bean representa la capa Servicio para la clase entidad "Item" que realiza peticiones de recursos al microservicio "servicio-productos" a través de un cliente API Rest de tipo RestTemplate de Spring

	// La anotación @Value nos permite obtener los valores de las propiedades configuradas en los archivos "application.properties" y aquellas que se encuentran en Spring Config Server
	// Con esta anotación obtenemos el valor de la propiedad "configuracion.texto" del archivo "servicio-items.properties" de Spring Config Server y se inyecta en la propiedad de esta clase "texto"
	@Value("${configuracion.texto: texto por defecto}")
	private String texto;
	
	// Este método handler escucha peticiones http de tipo Get para la url "/listar"
	// Dicho método va a devolver la lista de todos los items en formato Json
	// El parámetro opcional "nombre" y la cabecera opcional "token-request" de la petición http se establecen en el Api Gateway usando los filtros de fábrica "AddRequestParameter" y "AddRequestHeader" respectivamente
	@GetMapping("/listar")
	public List<Item> listar(@RequestParam(name = "nombre", required = false) String nombre, @RequestHeader(name = "token-request", required = false) String token){
		System.out.println("Parámetro \"nombre\" del request: " + nombre);
		System.out.println("Cabecera \"token-request\" del request: " + token);
		return itemService.findAll(); // retorna todos los items obtenidos del microservicio "servicio-productos" usando el método "findAll()" de la capa de Servicio "itemService"
	}
	
	// USANDO CIRCUIT BREAKER DE FORMA PROGRAMÁTICA
	// Nota: Usando Circuit Breaker de forma programática, funcionan las configuraciones indicadas tanto de forma progrmática mediante clases de configuraciones como aquellas indicas en archivos de configuraciones como .yml, .yaml o .properties
	// Este método handler escucha peticiones http de tipo Get para la url "/ver/{id}/cantidad/{cantidad}"
	// Dicho método va a devolver un item en formato Json dado su id y su cantidad
	// Con la anotación @PathVariable pasamos las variables "id" y "cantidad" de la ruta o path a los argumento "id" y "cantidad" de este método
	@GetMapping("/ver/{id}/cantidad/{cantidad}")
	public Item detalle(@PathVariable Long id,@PathVariable Integer cantidad) {
		// Creamos un "Circuit Breaker" con id "items"
		// El primer argumento del método "run" retorna el item obtenido del microservicio "servicio-productos" usando el método "findById(id, cantidad)" de la capa de Servicio "itemService"
		// El segundo argumento del método "run"(opcional) ejecuta el método alternativo "metodoAlternativo"  en caso de que el microservicio "servicio-productos" falle, sea una llamada lenta o la llamada tarde más del timeout del Time Limiter de Resilience4j. Es un camino alternativo que evita que se produzcan errores en cascada
		return cbFactory.create("items")
				.run(() -> itemService.findById(id, cantidad),
						e -> metodoAlternativo(id, cantidad, e)); // Opcionalmente, podemos pasar al método alternativo la excepción producida
	}
	
	// USANDO CIRCUIT BREAKER CON ANOTACIONES
	// Nota: Usando Circuit Breaker con anotaciones, sólo funcionan las configuraciones indicadas en archivos de configuraciones como .yml, .yaml o .properties
	// Este método handler escucha peticiones http de tipo Get para la url "/ver/{id}/cantidad/{cantidad}"
	// Dicho método va a devolver un item en formato Json dado su id y su cantidad
	// Con la anotación @PathVariable pasamos las variables "id" y "cantidad" de la ruta o path a los argumento "id" y "cantidad" de este método
	// En este caso, la anotación "@CircuitBreaker" crea un "Circuit Breaker" con id "items"
	@CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo") // (atributo "fallbackMethod" opcional)ejecuta el método alternativo "metodoAlternativo"  en caso de que la llamada al microservicio "servicio-productos" falle o se trate de una llamada lenta. Es un camino alternativo que evita que se produzcan errores en cascada
	@GetMapping("/ver2/{id}/cantidad/{cantidad}")
	public Item detalle2(@PathVariable Long id,@PathVariable Integer cantidad) {
		return itemService.findById(id, cantidad); // retorna el item obtenido del microservicio "servicio-productos" usando el método "findById(id, cantidad)" de la capa de Servicio "itemService"
	}
	
	// USANDO TIME LIMITER CON ANOTACIONES
	// Nota: Usando "Time Limiter" con anotaciones, sólo funcionan las configuraciones indicadas en archivos de configuraciones como .yml, .yaml o .properties
	// Nota: En este caso, sólo aplica "Time Limiter" pero no hace "Circuit Breaker" porque no estmos usando la anotación "@CircuitBreaker"
	// Este método handler escucha peticiones http de tipo Get para la url "/ver/{id}/cantidad/{cantidad}"
	// Dicho método va a devolver un item en formato Json dado su id y su cantidad
	// Con la anotación @PathVariable pasamos las variables "id" y "cantidad" de la ruta o path a los argumento "id" y "cantidad" de este método
	// En este caso, la anotación "@TimeLimiter" crea un "Time Limiter" con id "items"
	@TimeLimiter(name = "items", fallbackMethod = "metodoAlternativo2") // (atributo "fallbackMethod" opcional)ejecuta el método alternativo "metodoAlternativo2"(la respuesta de este método también tiene que ser envuelta en un "CompletableFuture" para que sea compatible con "@TimeLimiter)  en caso de que la llamada al microservicio "servicio-productos" tarde más del timeout del Time Limiter de Resilience4j. Es un camino alternativo que evita que se produzcan errores en cascada
	@GetMapping("/ver3/{id}/cantidad/{cantidad}")
	public CompletableFuture<Item> detalle3(@PathVariable Long id,@PathVariable Integer cantidad) {
		// Nota: Para que la anotación "@TimeLimiter" funcione correctamente y pueda contar el tiempo que transcurre para el timeout, es necesario envolver la llamada al microservicio en un "CompletableFuture" para que esa llamada sea asíncrona
		return CompletableFuture.supplyAsync(() -> itemService.findById(id, cantidad)); // retorna el item obtenido del microservicio "servicio-productos" usando el método "findById(id, cantidad)" de la capa de Servicio "itemService"
	}
	
	// USANDO CIRCUIT BREAKER Y TIME LIMITER CON ANOTACIONES
	// Nota: Usando "Circuit Breaker" y "Time Limiter" con anotaciones, sólo funcionan las configuraciones indicadas en archivos de configuraciones como .yml, .yaml o .properties
	// Este método handler escucha peticiones http de tipo Get para la url "/ver/{id}/cantidad/{cantidad}"
	// Dicho método va a devolver un item en formato Json dado su id y su cantidad
	// Con la anotación @PathVariable pasamos las variables "id" y "cantidad" de la ruta o path a los argumento "id" y "cantidad" de este método
	// En este caso, la anotación "@CircuitBreaker" crea un "Circuit Breaker" con id "items"
	// En este caso, la anotación "@TimeLimiter" crea un "Time Limiter" con id "items"
	// Nota: Si se combina las anotaciones "@CircuitBreaker" y "@TimeLimiter" y se desea usar un método de fallback, este método de fallback debe indicarse únicamente en el atributo "fallbackMethod" de la anotación "@CircuitBreaker" para que funcione todo, es decir, para que funcione llamadas con errores, llamadas lentas y llamadas por timeout
	@CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo2") // (atributo "fallbackMethod" opcional)ejecuta el método alternativo "metodoAlternativo2"(la respuesta de este método también tiene que ser envuelta en un "CompletableFuture" para que sea compatible con "@TimeLimiter) en caso de que la llamda al microservicio "servicio-productos" falle, se trate de una llamada lenta o tarde más del timeout del Time Limiter de Resilience4j. Es un camino alternativo que evita que se produzcan errores en cascada
	@TimeLimiter(name = "items")
	@GetMapping("/ver4/{id}/cantidad/{cantidad}")
	public CompletableFuture<Item> detalle4(@PathVariable Long id,@PathVariable Integer cantidad) {
		// Nota: Para que la anotación "@TimeLimiter" funcione correctamente y pueda contar el tiempo que transcurre para el timeout, es necesario envolver la llamada al microservicio en un "CompletableFuture" para que esa llamada sea asíncrona
		return CompletableFuture.supplyAsync(() -> itemService.findById(id, cantidad)); // retorna el item obtenido del microservicio "servicio-productos" usando el método "findById(id, cantidad)" de la capa de Servicio "itemService"
	}
	
	// Método "fallback"(puede ser privado o público) que se encarga de generar un item por defecto como alternativa y así evitar errores en cascada
	public Item metodoAlternativo(Long id,Integer cantidad, Throwable e) {
		log.info(e.getMessage());
		Item item = new Item();
		Producto producto = new Producto();
		producto.setId(id);
		producto.setNombre("Camara Sony");
		producto.setPrecio(500.00);
		item.setProducto(producto);
		item.setCantidad(cantidad);
		return item;
	}
	
	// Método "fallback"(puede ser privado o público) que se encarga de generar un item por defecto como alternativa y así evitar errores en cascada
	public CompletableFuture<Item> metodoAlternativo2(Long id,Integer cantidad, Throwable e) {
		log.info(e.getMessage());
		Item item = new Item();
		Producto producto = new Producto();
		producto.setId(id);
		producto.setNombre("Camara Sony");
		producto.setPrecio(500.00);
		item.setProducto(producto);
		item.setCantidad(cantidad);
		return CompletableFuture.supplyAsync(() -> item);
	}
	
	// Este método handler escucha peticiones http de tipo Get para la url "/obtener-config"
	// El método va a devolver en un ResponseEntity las propiedades configuradas en los archivos "application.properties" y aquellas que se encuentran en Spring Config Server junto con el estado de la respuesta OK(200)
	// Con la anotación @Value obtenemos el valor de la propiedad "server.port" del archivo "servicio-items.properties" de Spring Config Server y se inyecta en el argumento de entrada "puerto" del método handler
	// El tipo de dato ResponseEntity es un tipo de dato que nos permite devolver los datos de la respuesta de un método handler en formato Json junto con el estado de la respuesta Http
	@GetMapping("/obtener-config")
	public ResponseEntity<Map<String,String>> obtenerConfig(@Value("${server.port}") String puerto) {
		// Escribimos en log como información el contenido de la variable "texto"
		log.info(texto);
		// Creamos un HashMap donde vamos a guardar los valores de las propiedades configuradas en los archivos "application.properties" y aquellas que se encuentran en Spring Config Server
		Map<String,String> json = new HashMap<String,String>();
		// Almacenamos en el HashMap la variable "texto" asociado al atributo "texto" que contiene el valor de la propiedad "configuracion.texto" del archivo "servicio-items.properties" de Spring Config Server
		json.put("texto",texto);
		// Almacenamos en el HashMap el argumento de entrada "puerto" asociado al atributo "puerto" que contiene el valor de la propiedad "server.port" del archivo "servicio-items.properties" de Spring Config Server
		json.put("puerto",puerto);
		
		// Si tenemos un entorno activo definido(en el archivo de propiedades "bootstrap.properties") y además es el de Desarrollo("dev"),
		if(env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")) {
			// Insertamos en el HashMap el nombre y email del desarrollador asociados a los atributos "autor.nombre" y "autor.email" respectivamente
			// Para ello,obtenemos los valores de las propiedades "configuracion.autor.nombre" y "configuracion.autor.email" que se encuentran en el archivo "servicio-items-dev.properties" de Spring Config Server a partir del bean "env"
			json.put("autor.nombre",env.getProperty("configuracion.autor.nombre"));
			json.put("autor.email",env.getProperty("configuracion.autor.email"));
			json.put("saludo.texto", env.getProperty("saludo.texto"));	
		}
		
		// Devolvemos un ResponseEntity con los datos almacenados en el HashMap y el estado de la respuesta OK(200)
		return new ResponseEntity<Map<String,String>>(json,HttpStatus.OK);
	}
	
	// Este método handler escucha peticiones http de tipo Post para la url "/crear"
	// Dicho método va a persistir o guardar un producto en la base de datos a través del microservicio "servicio-productos"
	// Con la anotación @RequestBody indicamos que los datos del producto a persistir en la base de datos vienen en el body de la petición Http como un Json y automáticamente va a mapear dichos datos a las propiedades del objeto Producto pasado como argumento de entrada a este método handler
	@PostMapping("/crear")
	@ResponseStatus(HttpStatus.CREATED) // Con esta anotación establecemos el estado de la respuesta http a CREATED(201)
	public Producto crear(@RequestBody Producto producto) {
		return itemService.save(producto); // persiste el producto en la base de datos, a partir de los datos obtenidos del body de la petición http, usando el método "save(producto)" de la capa de Servicio "itemService" que a su vez hace uso del microservicio "servicio-productos"
	}
	
	// Este método handler escucha peticiones http de tipo Post para la url "/editar/{id}"
	// Dicho método actualiza un producto dado su id en la base de datos a través del microservicio "servicio-productos"
	// Con la anotación @RequestBody indicamos que los datos del producto a actualizar en la base de datos vienen en el body de la petición Http como un Json y automáticamente va a mapear dichos datos a las propiedades del objeto Producto pasado como argumento de entrada a este método handler
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	@PutMapping("/editar/{id}")
	public Producto editar(@RequestBody Producto producto,@PathVariable Long id) {
		return itemService.update(producto,id); // actualiza el producto en la base de datos, a partir de los nuevos datos obtenidos del body de la petición http, usando el método "update(producto,id)" de la capa de Servicio "itemService" que a su vez hace uso del microservicio "servicio-productos"
	}
	
	// Este método handler escucha peticiones http de tipo Delete para la url "/eliminar/{id}"
	// Dicho método elimina un producto dado su id de la base de datos a través del microservicio "servicio-productos"
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	// Como este método handler no devuelve datos ya que devuelve void,el estado de la respuesta de la petición tiene que ser "NO_CONTENT(204)"
	@DeleteMapping("/eliminar/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // Con esta anotación establecemos el estado de la respuesta http a NO_CONTENT(204)
	public void eliminar(@PathVariable Long id) {
		itemService.delete(id); // elimina un producto dado su id de la base de datos usando el método "deleteById(id)" de la capa de Servicio "itemService" que a su vez hace uso del microservicio "servicio-productos"
	}
}
