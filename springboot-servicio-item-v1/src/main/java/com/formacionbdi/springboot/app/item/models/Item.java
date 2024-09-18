package com.formacionbdi.springboot.app.item.models;

import java.io.Serializable;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

// Esta clase no tiene las anotaciones @Enity y @Table porque no se va a mapear con ninguna tabla de la base de datos
// Como buena práctica,implementamos la interfaz Serializable que es recomendable siempre y cuando una instancia de una clase entidad se quiere usar de manera remota o se quiere pasar o serializar a bytes

public class Item implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7470918330317541608L;
	
	private Producto producto;
	private Integer cantidad;
	
	public Item() {
		
	}
	
	public Item(Producto producto, Integer cantidad) {
		this.producto = producto;
		this.cantidad = cantidad;
	}

	public Producto getProducto() {
		return producto;
	}
	
	public void setProducto(Producto producto) {
		this.producto = producto;
	}
	
	public Integer getCantidad() {
		return cantidad;
	}
	
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}
	
	public Double getTotal() {
		return producto.getPrecio() * cantidad.doubleValue();
	}
	
}
