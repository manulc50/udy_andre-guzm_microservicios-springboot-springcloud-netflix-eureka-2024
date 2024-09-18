package com.formacionbdi.springboot.app.usuarios.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

//Se utiliza la clase entidad "Usuario" de nuestra librería común de usuarios


// A la hora de crear un repositorio o Dao con Spring Data JPA,Spring Data JPA nos proporciona 3 tipos de interfaces que podemos extender para implementar este repositorio o Dao.Éstas son: CrudRepository(nos proporciona los métodos básicos para hacer el CRUD),PagingAndSortingRepository(a su vez extiende de CrudRepository y nos proporciona los mismos métodos que CrudRepository más funcionalidades para hacer paginación y ordenación  y JPARepository(a su vez extiende de PagingAndSortingRepository y nos proporicona todos los métodos indicados anteriormente más otras funcionalidades adicionales)
// Cuando usamos las interfaces de Spring Data JPA para implementar nuestro repositorio,no hace falta usar la anotación @Repository.Si usamos nuestra propia interfaz con nuestra propia implementación,sí tenemos que usar dicha anotación.
// "PagingAndSortingRepository" es una interfaz propia de Spring que nos proporciona los métodos básicos para realizar operaciones CRUD en la base de datos añadiendo funcionalidades de paginación y ordenación.Esta interfaz extiende a su vez de "CrudRepository",que es la que realmente nos proporicona las operaciones básicas para hacer CRUD
// Tenemos que indicar la clase entidad sobra la que vamos a realizar las operaciones CRUD en la base de datos y el tipo de la propiedad de dicha clase que representa la clave primaria

// En lugar de crear nuestra capa de Servicio para este Dao y después nuestro controlador,usando esta anotación nos permite crear automáticamente un controlador para hacer CRUD sobre la entidad Usuario(listar todos los usuarios,ver un usuario por id,crear un usuario,editar un usuario y eliminar un usuario) a partir de la información de este Dao
// Lo que hace es exportar la información de este Dao(incluidas las consultas personalizadas) a un endpoint indicado en el atributo "path" de esta anotación,es decir, a partir del endpoint "usuarios" podemos hacer CRUD usando las peticiones http Get,Post,Put Y Delete para la entidad Usuario
// Con esta anotación,si queremos invocar a una consulta personalizada,por defecto tiene que ser de esta manera;".../valor_atributo_path/search/nombre_metodo_consulta?paarm_entrada=valor".Por ejemplo,la ivocación a la consulta personalizada implementada por el método "findByUsername" con el username "andres" sería con "usuarios/search/findByUsername?username=andres"
// Usando esta anotación también,si queremos personalizar la ruta por defecto para invocar a una consulta personalizada,tenemos que indicarla en la anotación @RestResource a nivel de método
@RepositoryRestResource(path="usuarios")
public interface UsuarioDao extends PagingAndSortingRepository<Usuario,Long>{
	
	// Las consultas personalizadas en Spring Data JPA se pueden realizar de dos manera;una que,respetando la nomenclatura y estructura indicada por Spring Data JPA,se implementa automáticamente por debajo sin tener que escribirla nosotros
	// Y la otra manera es no respetar la nomenclatura y estructura indicada por Spring Data JPA y escribir o implementar nosotros mismos la consulta usando la anotación @Query
	
	// Creamos una consulta personalizada de manera automática(respetando la nomenclatura y estructura indicada por Spring Data JPA)que localiza y devuelve los datos de un usuario a partir de su username
	// Con esta anotación,podemos cambiar la ruta por defecto para invocar a esta consulta personalizada de "usuarios/search/findByUsername?username=andres" a "usuarios/search/buscar-username?username=andres"
	@RestResource(path="buscar-username")
	public Usuario findByUsername(@Param("username") String username); // Con la anotación @Param podemos cambiar el nombre por defecto del parámetro de la consulta personalizada en la ruta de la invocación 
	
	// Un ejemplo de una consulta personalizada manual(no respeta la nomenclatura indicada por Spring Data JPA) que también localiza y devuelve los datos de un usuario a partir de su username
	// Se usa el lenguaje JPQL. Este tipo de consultas se realizan usando los nombres de las clases entidades y no usando los nombres correspondientes de las tablas en la base de datos
	@Query("select u from Usuario u where u.username=?1") // "?1" hace referencia al primer argumento que se le pasa al método,es decir,a "String username".Si tuvieramos más filtros en la consulta con "?2","?3",...,harían referencia al segundo argumento,tercer argumento,...,y así sucesivamente
	public Usuario obtenerPorUsername(String username);
}
