package com.formacionbdi.springboot.app.usuarios.commons.models.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//Como buena práctica,implementamos la interfaz Serializable que es recomendable siempre y cuando una instancia de una clase entidad se quiere usar de manera remota o se quiere pasar o serializar a bytes

//Nota: Por convención, el nombre de una clase Java comienza en mayúscula y es singular.Sin embargo,en una tabla de la base de datos el nombre de una tabla comienza en minúscula y es en plural

//Si se desea que el nombre de la tabla de la base de datos coincida con el nombre de la clase,no hace falta poner la anotación @Table

@Entity // Indicamos que esta clase se trata de una clase de entidad o persistencia.Es aquella que se mapea con una tabla de la base de datos
@Table(name="roles") // Esta clase representa una entidad que va a ser mapeada con la tabla 'roles' de la base de datos
public class Role implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4467531611801172710L;
	
	// Las propiedades de la clase que no tengan la anotacion @Column van a tener el mismo nombre en relacion con los campos de la tabla de la base de datos
	
	@Id // Indica que esta propiedad va a tratarse de la clave primaria
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Indica el modo de generación de la clave primaria(Con 'GenerationType.IDENTITY' se genera de manera autoincremental)
	private Long id;
	
	// Restricción a nivel de tabla(se aplica cuando se crea la tabla en la base de datos) para que el campo nombre de la tabla sea único(no puede haber registros con nombres repetidos). Y también establecemos a nivel de campo de tabla un tamaño máximo de 30 caracteres
	@Column(unique=true,length=30)
	private String nombre;
	
	// Si en el ámbito o sentido de la aplicación, quisieramos conocer los usuarios asociados a un role,es decir,si quisieramos establecer una relación bidireccional entre usuarios y roles,tendriamos que añadir a esta entidad Role la propiedad "usuarios"
	// A esta propiedad también la tendríamos que anotar,al igual que ocurre en la entidad Usuario,con la anotación @ManyToMany,porque desde este lado de la relación la cardinalidad también es muchos a muchos
	// También tendriamos que indicar,al igual que ocurre en la entidad Usuario, el tipo de "fetch",que indica,en este caso,la manera de traer de la base de datos los datos de los usuarios cuando se hace una consulta sobre un role
	// Y además,en las relaciones bidireccionales,es necesario indicar en el atributo "mappedBy" cuál es la propiedad("roles") de la otra entidad de la relación(Usuario) involucrada en la relación con esta entidad
	// Comentamos esta propiedad para dejarla como un ejemplo de cómo sería indicar una relación bidireccional,pero en el ámbito o sentido de esta aplicación,no nos interesa conocer los usuarios asociados a un role.Entonces, la relación es unidireccional desde el lado de la entidad Usuario
	/*@ManyToMany(fetch = FetchType.LAZY,mappedBy="roles")
	private List<Usuario> usuarios;*/

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

}
