package com.formacionbdi.springboot.app.productos.models.service;

import java.util.List;

import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

public interface IProductoService {
	public List<Producto> findAll(); // Retorna todos los productos de la base de datos a través del Dao
	public Producto findById(Long id); // Retorna un producto a partir de su id de la base de datos a través del Dao
	public Producto save(Producto producto); // Persiste un nuevo producto en la base de datos a través del Dao y después lo retorna
	public void deleteById(Long id); // Elimina un producto dado su id de la base de datos a través del Dao
}
