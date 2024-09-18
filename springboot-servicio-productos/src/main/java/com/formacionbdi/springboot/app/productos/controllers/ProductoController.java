package com.formacionbdi.springboot.app.productos.controllers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.springboot.app.productos.models.service.IProductoService;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

@RestController // Indicamos que esta clase se trata de un controlador Rest de Spring y,de esta manera,Spring lo va a almacenar en su memoria o contenedor como un bean.Esta anotación hace uso a su vez de las anotaciones de Spring @Controller y @ResponseBody para que los métodos handler implementados en este controlador devuelvan un Json con la información
public class ProductoController {
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "Environment".Esta interfaz es implementada por Spring
	@Autowired
	private Environment env; // Este bean de Spring contiene información sobre el contexto de desarrollo del proyecto.Lo vamos a usar para obtener el número del puerto donde se ejecuta este microservicio en el Tomcat embebido de SpringBoot 
	
	// En vez de recuperar del contenedor de Spring el bean que implementa la interfaz "Environment" para obtener el número del puerto, una alternativa para poder hacer esto de manera más directa es utilizar la anotación @Value 
	// La anotación @Value nos permite obtener los valores de las propiedades configuradas en los archivos "application.properties" y aquellas que se encuentran en Spring Config Server
	// Con esta anotación obtenemos el valor de la propiedad "server.port" del archivo "application.properties" y se inyecta en la propiedad de esta clase "port"
	// NOTA: Si el valor de la propiedad "server.port" es 0, para indicar que se genere un puerto aleatorio disponible, tenemos que usar la alternativa de la interfaz "Environment" para obtener el puerto generado
	//       Si usamos la alternativa de la anotación "@Value" en este caso en concreto, vamos a obtener el valor 0 
	@Value("${server.port}")
	private Integer port;
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "IProductoService".Esta interfaz es implementada por la clase "ProductoServiceImpl"
	@Autowired
	private IProductoService productoService; // Este bean representa la capa Servicio para la clase entidad "Producto" que realiza operaciones CRUD en la base de datos a través de la capa Dao
	
	// Este método handler escucha peticiones http de tipo Get para la url "/listar"
	// Dicho método va a devolver la lista de todos los productos en formato Json
	@GetMapping("/listar")
	public List<Producto> listar(){
		// Retorna todos los productos de la base de datos usando el método "findAll()" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
		// Como tenemos que setear el número del puerto a cada producto de la lista obtenida,usamos los streams y el operador map de Java 8
		// Creamos un stream a partir de la lista de productos,aplicamos el operador map con una función lambda para setear el número del puerto a cada producto de la lista.Por último,generamos una lista con cada uno de los productos obtenidos de la transformación
		return productoService.findAll().stream().map(producto -> {
			// Usando el bean "env" que implementa la interfaz "Environment" de Spring
			producto.setPort(Integer.parseInt(env.getProperty("local.server.port"))); // Seteamos el número del puerto donde se ejecutará este microservicio.Para ello,obtenemos el valor de la propiedad "local.server.port" que nos lo da el bean de Spring que implementa la interfaz "Environment"
			// Usando el bean "port" inyectado con la anotación @Value
			//producto.setPort(port);
			return producto;
		}).collect(Collectors.toList());
	}
	
	// Este método handler escucha peticiones http de tipo Get para la url "/ver/{id}"
	// Dicho método va a devolver un producto dado su id en formato Json
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	@GetMapping("/ver/{id}")
	public Producto detalle(@PathVariable Long id) {	
		// Para probar el "Circuit Breaker" por excepción(Hystrix o Resilience4j)
		if(id.equals(200L))
			throw new IllegalStateException("Producto no encontrado!");
		
		// Para probar el "Circuit Breaker" por timeoutHystrix o Resilience4j) o por llamada lenta(Resilience4j)
		if(id.equals(7L)) {
			try {
				TimeUnit.SECONDS.sleep(5l);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Producto producto =  productoService.findById(id); // retorna el producto de la base de datos dado su id usando el método "findById(id)" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
		// Usando el bean "env" que implementa la interfaz "Environment" de Spring
		producto.setPort(Integer.parseInt(env.getProperty("local.server.port"))); // Seteamos el número del puerto donde se ejecutará este microservicio.Para ello,obtenemos el valor de la propiedad "local.server.port" que nos lo da el bean de Spring que implementa la interfaz "Environment"
		// Usando el bean "port" inyectado con la anotación @Value
		//producto.setPort(port);
		
		// Simulamos o forzamos casos de error para probar el manejador de errores de Netflix Hystrix
		// Poner estado a 1 para probar el manejador de errores de Netflix Hystrix por excepción
		// Poner estado a 2 para probar el manejador de errores de Netflix Hystrix por timeout
		/*int estado = 1;
		if(estado == 1)
			throw new RuntimeException("No se pudo cargar el producto");
		else if(estado == 2) {
			try {
				Thread.sleep(2000L);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		
		return producto;
	}
	
	// Este método handler escucha peticiones http de tipo Post para la url "/crear"
	// Dicho método va a persistir o guardar un producto en la base de datos
	// Con la anotación @RequestBody indicamos que los datos del producto a persistir en la base de datos vienen en el body de la petición Http como un Json y automáticamente va a mapear dichos datos a las propiedades del objeto Producto pasado como argumento de entrada a este método handler
	@PostMapping("/crear")
	@ResponseStatus(HttpStatus.CREATED) // Con esta anotación establecemos el estado de la respuesta http a CREATED(201)
	public Producto crear(@RequestBody Producto producto) {
		// Como el objeto "producto" no tiene la propiedad "id" definida,el método "save(producto)" va a insertarlo en la base de datos en lugar de actualizarlo
		return productoService.save(producto); // persiste el producto en la base de datos, a partir de los datos obtenidos del body de la petición http, usando el método "save(producto)" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
	}
	
	// Este método handler escucha peticiones http de tipo Put para la url "/editar/{id}"
	// Dicho método actualiza un producto dado su id en la base de datos
	// Con la anotación @RequestBody indicamos que los datos del producto a actualizar en la base de datos vienen en el body de la petición Http como un Json y automáticamente va a mapear dichos datos a las propiedades del objeto Producto pasado como argumento de entrada a este método handler
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	@PutMapping("/editar/{id}")
	public Producto editar(@RequestBody Producto producto,@PathVariable Long id) {
		// Recuperamos el producto a actualizar de la base de datos, a partir del id obtenido del path o ruta de la petición http, usando el método "findById(id)" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
		Producto productoDb = productoService.findById(id);
		// Actualizamos los datos del producto recuperado de la base de datos con los nuevos datos que nos llegan en el body de la peticioón http
		productoDb.setNombre(producto.getNombre());
		productoDb.setPrecio(producto.getPrecio());
		// La propiedad "createAt" no se actualiza porque se entiende que es una propiedad que almacena la fecha de creación del producto en la base de datos,no la fecha de actualización
		// Como el objeto "productoDb" se ha recuperado de la base de datos con el método "findById(id)",la propiedad "id" está definida, y por lo tanto, el método "save(producto)" va a actualizarlo en lugar de hacer una nueva inserción o guardado
		return productoService.save(productoDb); // actualiza el producto en la base de datos, a partir de los nuevos datos obtenidos del body de la petición http, usando el método "save(producto)" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
	}
	
	// Este método handler escucha peticiones http de tipo Delete para la url "/eliminar/{id}"
	// Dicho método elimina un producto dado su id de la base de datos
	// Con la anotación @PathVariable pasamos la variable "id" de la ruta o path al argumento "id" de este método
	// Como este método handler no devuelve datos ya que devuelve void,el estado de la respuesta de la petición tiene que ser "NO_CONTENT(204)"
	@DeleteMapping("/eliminar/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // Con esta anotación establecemos el estado de la respuesta http a NO_CONTENT(204)
	public void eliminar(@PathVariable Long id) {
		productoService.deleteById(id); // elimina un producto dado su id de la base de datos usando el método "deleteById(id)" de la capa de Servicio "productoService" que a su vez accede a la capa Dao
	}
	

}
