package com.formacionbdi.springboot.app.productos.models.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.formacionbdi.springboot.app.productos.models.dao.ProductoDao;
import com.formacionbdi.springboot.app.commons.models.entity.Producto;

//Se utiliza la clase entidad "Producto" de nuestra librería común "commons" de productos

@Service // Indicamos que esta clase se trata de una clase servicio de Spring.De esta manera,Spring va a almacenar un bean de esta clase en su memmoria o contenedor para poderlo inyectar en otra parte del proyecto
public class ProductoServiceImpl implements IProductoService{
	
	// Es mejor establecer la transaccionalidad en la capa Servicio en lugar de la capa Dao
	// La transaccionabilidad a nivel de método garantiza que todas las operaciones CRUD indicada en dicho método tengan efecto simepre y cuando ninguna de ellas falle,es decir,si una de las operaciones CRUD falla,ninguna de las operaciones CRUD indicadas van a tener efecto en la base de datos
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "ProductoDao".Esta interfaz es implementada por Spring al extender de la interfaz "JpaRepository"
	@Autowired
	private ProductoDao productoDao; // Este bean se trata del Dao para realizar CRUD en la tabla "productos" mapeada con la clase entidad "Producto"

	// Esta anotación es para que la transaccionalidad sea gestionada por Spring
	@Transactional(readOnly = true) // Establecemos "readOnly" a true porque este método se encarga únicamente de leer y recuperar información de la base de datos.No altera la base de datos con insercciones,borrados o actualizaciones.
	public List<Producto> findAll() {
		return productoDao.findAll(); // accedemos a nuestra capa Dao de la entidad "Producto" para obtener todos los productos de la base de datos usando el método "findAll()"
	}

	// Esta anotación es para que la transaccionalidad sea gestionada por Spring
	@Transactional(readOnly = true) // Establecemos "readOnly" a true porque este método se encarga únicamente de leer y recuperar información de la base de datos.No altera la base de datos con insercciones,borrados o actualizaciones.
	public Producto findById(Long id) {
		return productoDao.findById(id).orElse(null); // accedemos a nuestra capa Dao de la entidad "Producto" para obtener el producto dado su id de la base de datos usando el método "findById(id)".Si no se localiza,devuelve null
	}

	// Esta anotación es para que la transaccionalidad sea gestionada por Spring
	@Transactional // No especificamos aquí "readOnly" a true porque este método va a persistir un objeto Producto en la base de datos y,por lo tanto,va a ser modificada y alterada
	@Override
	public Producto save(Producto producto) {
		return productoDao.save(producto); // accedemos a nuestra capa Dao de la entidad "Producto" para persistir(si no existe en la base datos,o dicho de otra manera,si el id del producto no está definido) o actualizar(si ya existe en la base de datos,o dicho de otra manera,si el id del producto sí está definido) un producto usando el método "save(producto)".Devuelve el objeto Producto después de su inserción o actualización
	}

	// Esta anotación es para que la transaccionalidad sea gestionada por Spring
	@Transactional // No especificamos aquí "readOnly" a true porque este método va a eliminar un producto de la base de datos y,por lo tanto,va a ser modificada y alterada
	@Override
	public void deleteById(Long id) {
		productoDao.deleteById(id); // accedemos a nuestra capa Dao de la entidad "Producto" para eliminar un producto dado su id usando el método "deleteById(id)"
	}

}
