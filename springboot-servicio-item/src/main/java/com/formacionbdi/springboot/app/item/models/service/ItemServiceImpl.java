package com.formacionbdi.springboot.app.item.models.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

//Indicamos que esta clase se trata de una clase servicio de Spring.De esta manera,Spring va a almacenar un bean de esta clase en su memmoria o contenedor para poderlo inyectar en otra parte del proyecto
@Service("serviceRestTemplate") // Indicamos el nombre("serviceRestTemplate") del bean que Spring va a almacenar en su contenedor o memoria porque ahora tenemos dos implementaciones de la interfaz "IItemService" y es necesario especificar en las partes de este proyecto donde se inyecte con @Qualifier qué implementación queremos usar.Esto es una opción alternativa a usar la anotación @Primary en una de las clases que implementen la interfaz "IItemService"
public class ItemServiceImpl implements IItemService{
	
	// Recuperamos del contenedor o memoria de Spring el bean de tipo "RestTemplate"
	@Autowired
	private RestTemplate clienteRest; // Este bean es un cliente que trabaja con API Rest y nos permite acceder a recursos que están en otros microservicios.En este caso vamos a consumir recursos del microservicio "servicio-productos"

	@Override
	public List<Item> findAll() {
		// Obtenemos la lista de productos del microservicio "servicio-productos"
		// Para ello,usamos el bean "clienteRest" mediante su método "getForObject" indicándole la url donde se localiza el recurso de dicho microservicio y el tipo de dato que retorna
		// Como no es posible indicar "List<Producto>" en el tipo de dato que retorna el microservicio,tenemos que usar un array de Producto con "Producto[].class" y después hacer un casting usando el método "asList" de la clase "Arrays"
		// Sin usar el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando host:port
		// List<Producto> productos = Arrays.asList(clienteRest.getForObject("http://localhost:8001/listar",Producto[].class));
		// Usando el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando el nombre de dicho microservicio
		List<Producto> productos = Arrays.asList(clienteRest.getForObject("http://servicio-productos/listar",Producto[].class));
		// Tenemos que devolver una lista de items a partir de la lista de productos obtenida
		// Para ello,usamos los streams y el operador map de Java 8 junto con funciones lambda
		// Creamos un stream a partir de la lista de productos,aplicamos el operador map con la función lambda "p -> new Item(p,1)" para transformar cada producto de la lista en un item con la cantidad por defecto 1.Por último,generamos una lista con cada uno de los items obtenidos de la transformación
		return productos.stream().map(p -> new Item(p,1)).collect(Collectors.toList());
	}

	@Override
	public Item findById(Long id, Integer cantidad) {
		// Creamos un HashMap para pasarle el id a la ruta o path indicada en el método "getForObject" del cliente API Rest "clienteRest"
		Map<String,String> pathVariables = new HashMap<String,String>();
		// Añadimos el id al HashMap asociado a la clave "id"
		pathVariables.put("id",String.valueOf(id.longValue()));
		// Obtenemos el producto del microservicio "servicio-productos" a través del cliente API Rest "clienteRest"
		// Para ello,usamos el método "getForObject" de dicho cliente pasándole la url o path del recurso del microservicio "servicio-productos",el tipo de dato que retorna y,como la url o path tiene una parte variable que se corresponde con el id del producto,un HashMap con un par (clave,valor) para esa variable
		// Sin usar el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando host:port
		// Producto producto = clienteRest.getForObject("http://localhost:8001/ver/{id}",Producto.class,pathVariables);
		// Usando el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando el nombre de dicho microservicio
		Producto producto = clienteRest.getForObject("http://servicio-productos/ver/{id}",Producto.class,pathVariables);
		// Por último,creamos y devolvemos un objeto Item a partir del producto obtenido anteriormente y de la cantidad recibida como parámetro en este método
		return new Item(producto,cantidad);
	}

	@Override
	public Producto save(Producto producto) {
		// Creamos un objeto "HttpEntity<Producto>" para almacenar los datos del producto a crear.Esto es necesario porque es requerido para hacer la invocación al método "exchange" del cliente API Rest "clienteRest"
		HttpEntity<Producto> body = new HttpEntity<Producto>(producto);
		// Creamos un nuevo producto haciendo uso del microservicio "servicio-productos" a través del cliente API Rest "clienteRest"
		// Para ello,usamos el método "exchange" indicándole la url o path del microservicio,el tipo de petición Post,el objeto "body" de tipo "HttpEntity<Producto>" con los datos del producto a crear,y el tipo de dato "Producto" que es el que devuelve el método
		// Sin usar el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando host:port
		// ResponseEntity<Producto> response = clienteRest.exchange("http://localhost:8001/crear",HttpMethod.POST,body,Producto.class);
		// Usando el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando el nombre de dicho microservicio
		ResponseEntity<Producto> response = clienteRest.exchange("http://servicio-productos/crear",HttpMethod.POST,body,Producto.class);
		// Por último,del objeto "ResponseEntity<Producto>" devuelto por el método "exchange" del cliente Api Rest "clienteRest",obtenemos y devolvemos el objeto "Producto" del body de la respuesta
		return response.getBody();
	}

	@Override
	public Producto update(Producto producto, Long id) {
		// Creamos un objeto "HttpEntity<Producto>" para almacenar los datos del producto a actualizar.Esto es necesario porque es requerido para hacer la invocación al método "exchange" del cliente API Rest "clienteRest"
		HttpEntity<Producto> body = new HttpEntity<Producto>(producto);
		// Creamos un HashMap para pasarle el id a la ruta o path indicada en el método "exchange" del cliente API Rest "clienteRest"
		Map<String,String> pathVariables = new HashMap<String,String>();
		// Añadimos el id al HashMap asociado a la clave "id"
		pathVariables.put("id",String.valueOf(id.longValue()));
		// Actualizamos un producto haciendo uso del microservicio "servicio-productos" a través del cliente API Rest "clienteRest"
		// Para ello,usamos el método "exchange" indicándole la url o path del microservicio,el tipo de petición Put,el objeto "body" de tipo "HttpEntity<Producto>" con los datos del producto a actualizar,el tipo de dato "Producto" que es el que devuelve el método,y,como la url o path tiene una parte variable que se corresponde con el id del producto,un HashMap con un par (clave,valor) para esa variable
		// Sin usar el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando host:port
		// ResponseEntity<Producto> response = clienteRest.exchange("http://localhost:8001/editar/{id}",HttpMethod.PUT,body,Producto.class,pathVariables);
		// Usando el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando el nombre de dicho microservicio
		ResponseEntity<Producto> response = clienteRest.exchange("http://servicio-productos/editar/{id}",HttpMethod.PUT,body,Producto.class,pathVariables);
		// Por último,del objeto "ResponseEntity<Producto>" devuelto por el método "exchange" del cliente Api Rest "clienteRest",obtenemos y devolvemos el objeto "Producto" del body de la respuesta
		return response.getBody();
	}

	@Override
	public void delete(Long id) {
		// Creamos un HashMap para pasarle el id a la ruta o path indicada en el método "exchange" del cliente API Rest "clienteRest"
		Map<String,String> pathVariables = new HashMap<String,String>();
		// Añadimos el id al HashMap asociado a la clave "id"
		pathVariables.put("id",String.valueOf(id.longValue()));
		// Eliminamos un producto dado su id haciendo uso del microservicio "servicio-productos" a través del cliente API Rest "clienteRest"
		// Para ello,usamos el método "delete" indicándole la url o path del microservicio y,como la url o path tiene una parte variable que se corresponde con el id del producto,un HashMap con un par (clave,valor) para esa variable
		// Sin usar el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando host:port
		// clienteRest.delete("http://localhost:8001/eliminar/{id}",pathVariables);
		// Usando el balanceador de carga Ribbon de Netflix -> Se indica la url o path del microservicio a consumir usando el nombre de dicho microservicio
		clienteRest.delete("http://servicio-productos/eliminar/{id}",pathVariables);
	}

}
