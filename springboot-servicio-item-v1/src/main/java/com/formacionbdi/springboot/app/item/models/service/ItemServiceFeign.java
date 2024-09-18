package com.formacionbdi.springboot.app.item.models.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.formacionbdi.springboot.app.item.clientes.ProductoClienteRest;
import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

//Indicamos que esta clase se trata de una clase servicio de Spring.De esta manera,Spring va a almacenar un bean de esta clase en su memmoria o contenedor para poderlo inyectar en otra parte del proyecto
@Service("serviceFeign") // Como tenemos dos implementaciones de la interfaz "IItemService",establecemos el nombre "serviceFeign" al bean que almacene Spring en su contenedor para esta implementación para después poder indicarlo en la anotación @Qualifier cuando se inyecte en otra parte del proyecto con @Autowired.Esto es una opción alternativa a usar la anotación @Primary en una de las clases que implementen la interfaz "IItemService"
//Como tenemos dos implementaiones de la interfaz IItemService(una con el cliente API Rest de Spring "RestTemplate" y otra con el cliente API Rest Feign de Netflix),con esta anotaión indicamos que la implementación de esta clase,es decir,la del cliente API Rest Feign de Netflix,es la que Spring va a tener en cuenta como opción por defecto o primaria para inyectar en cualquier parte del proyecto donde se encuentre la anotación @Autowired para la interfaz "IItemService"(por ahora,solo en el controlador "ItemController")  
//@Primary // Otra alternativa a usar esta anotación es usar la anotación @Qualifier en las partes del proyecto donde se inyecte con @Autowired la implementación de la interfaz "IItemService".A la anotación @Qualifier hay que indicarle el nombre de la implementación que se quiere inyectar como un bean.Si este nombre no se indica en la anotación @Service,por defecto es el nombre de la clase de la imlementación comenzando en minúscula(para este caso sería "itemServiceFeign")
public class ItemServiceFeign implements IItemService{
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "ProductoClienteRest".Esta interfaz es implementada por Spring al usar la anotación @FeignClient en dicha interfaz
	@Autowired
	private ProductoClienteRest clienteFeign; // Este bean se trata de un cliente API Rest Feign que nos permite consumir recursos que se localizan en otros microservicios.En este caso,lo usamos para consumir recursos del microservicio "servicio-productos"

	@Override
	public List<Item> findAll() {
		// Usamos nuestro cliente API Rest Feign "clienteFeign" para consumir y obtener la lista de todos los productos que nos devuelve el microservicio "servicio-productos" a través del método "listar()"
		// Tenemos que devolver una lista de items a partir de la lista de productos obtenida
		// Para ello,usamos los streams y el operador map de Java 8 junto con funciones lambda
		// Creamos un stream a partir de la lista de productos,aplicamos el operador map con la función lambda "p -> new Item(p,1)" para transformar cada producto de la lista en un item con la cantidad por defecto 1.Por último,generamos una lista con cada uno de los items obtenidos de la transformación
		return clienteFeign.listar().stream().map(p -> new Item(p,1)).collect(Collectors.toList());
	}

	@Override
	public Item findById(Long id, Integer cantidad) {
		// Usamos nuestro cliente API Rest Feign "clienteFeign" para consumir y obtener el producto dado su id que nos devuelve el microservicio "servicio-productos" a través del método "detalle(id)"
		// Por último,creamos un item a partir del producto obtenido y de la cantidad pasada como parámetro en este método
		return new Item(clienteFeign.detalle(id),cantidad);
	}

	@Override
	public Producto save(Producto producto) {
		return clienteFeign.crear(producto);
	}

	@Override
	public Producto update(Producto producto, Long id) {
		return clienteFeign.editar(producto, id);
	}

	@Override
	public void delete(Long id) {
		clienteFeign.eliminar(id);
	}

}
