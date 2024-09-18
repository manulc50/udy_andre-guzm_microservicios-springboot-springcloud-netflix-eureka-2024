package com.formacionbdi.springboot.app.item.models.service;

import java.util.List;

import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

public interface IItemService {
	
	public List<Item> findAll(); // Retorna todos los items a través de los productos devueltos por el microservicio "servicio-productos"
	public Item findById(Long id,Integer cantidad); // Retorna el item a partir de una cantidad dada y a través del producto dado su id devuelto por el microservicio "servicio-productos"
	public Producto save(Producto producto);
	public Producto update(Producto producto,Long id);
	public void delete(Long id);
	
}
