package com.formacionbdi.springboot.app.usuarios.commons.models.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.UniqueConstraint;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

// Como buena práctica,implementamos la interfaz Serializable que es recomendable siempre y cuando una instancia de una clase entidad se quiere usar de manera remota o se quiere pasar o serializar a bytes

//Nota: Por convención, el nombre de una clase Java comienza en mayúscula y es singular.Sin embargo,en una tabla de la base de datos el nombre de una tabla comienza en minúscula y es en plural

//Si se desea que el nombre de la tabla de la base de datos coincida con el nombre de la clase,no hace falta poner la anotación @Table

@Entity // Indicamos que esta clase se trata de una clase de entidad o persistencia.Es aquella que se mapea con una tabla de la base de datos
@Table(name="usuarios") // Esta clase representa una entidad que va a ser mapeada con la tabla 'usuarios' de la base de datos
public class Usuario implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4002221912401133094L;
	
	// Las propiedades de la clase que no tengan la anotacion @Column van a tener el mismo nombre en relacion con los campos de la tabla de la base de datos
	
	@Id // Indica que esta propiedad va a tratarse de la clave primaria
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Indica el modo de generación de la clave primaria(Con 'GenerationType.IDENTITY' se genera de manera autoincremental)
	private Long id;
	
	// Restricción a nivel de tabla(se aplica cuando se crea la tabla en la base de datos) para que el campo username de la tabla sea único(no puede haber registros con usernames repetidos). Y también establecemos a nivel de campo de tabla un tamaño máximo de 20 caracteres
	@Column(unique=true,length=20)
	private String username;
	
	// Restricción a nivel de tabla(se aplica cuando se crea la tabla en la base de datos) para que el campo password de la tabla tenga un tamaño máximo de 60 caracteres, ya que vamos a almacenar las contraseñas encriptadas, que suelen ser de tamaños grandes
	@Column(length = 60)
	private String password;
	private Boolean enabled; // propiedad booleana para habilitar o deshabilitar al usuario
	private String nombre;
	private String apellido;
	
	// Restricción a nivel de tabla(se aplica cuando se crea la tabla en la base de datos) para que el campo email de la tabla sea único(no puede haber registros con emails repetidos). Y también establecemos a nivel de campo de tabla un tamaño máximo de 100 caracteres
	@Column(unique=true,length=100)
	private String email;
	
	// Propiedad que establece la relación entre esta entidad Usuario y la entidad Role.La relación entre ambas entidades es muchos a muchos
	// Un usuario puede tener muchos roles y un role puede estar asociado a muchos usurios,pero la relación es unidireccional desde este lado de la relación,es decir,desde esta entidad Usuario
	// Esto es así porque, desde el punto de vista de esta aplicación,nos interesa conocer los roles asociados a un usuario,pero no nos interesa saber los usuarios asociados a un role
	// Con esta anotación indicamos que la relación entre esta entidad Usuario y la entidad Role es muchos a muchos
	// La relación muchos a muchos supone la creación de una tercera tabla en la base de datos que establece las relaciones entre los usuarios y los roles.Para ello,esta tabla va a contener como claves foráneas las claves primarias de las tablas para usuarios y para roles
	// Por defecto,si no se indica otros nombres personalizados para las claves foráneas,el nombre de la clave foránea para la entidad Usuario va a ser el nombre de su tabla en la base de datos,que es "usuarios", seguido de un guión bajo más el nombre de la propiedad que represencta la clave primaria para esta entidad,que es "id".En definitiva,el nombre sería "usuarios_id"
	// Lo mismo ocurre para el nombre de la clave foránea para la entidad Role,es decir,sería "roles_id"
	// También,por defecto,si no se indica otro nombre personalizado,el nombre de esta tercera tabla, que establece las relaciones entre los usuarios y los roles, se forma con el nombre de la tabla para la entidad Usuario,que es "usuarios",seguido de un guión bajo más el nombre de la tabla para la entidad Role,que es "roles".En definitiva,el nombre sería "usuarios_roles"
	// Con el atributo "fetch" indicamos la manera de traer los datos de los roles para un usuario.
	// Puede ser "EAGER" que se trae todos los datos de los roles directamente,o al mismo tiempo, cuando se hace una consulta en la base de datos sobre un usuario
	// O puede ser "LAZY"(carga perezosa) que se trae los datos de los roles únicamente cuando se invoca al método "getRoles()" y no cuando se hace una consulta a la base de datos sobre un usuario
	// Como buena práctica,y siempre que sea posible,la mejor opción es usar el tipo "LAZY" para evitar problemas de sobrecarga en la base de datos que sí podría darse usando el tipo "EAGER"
	@ManyToMany(fetch = FetchType.LAZY)
	// Con esta anotación personalizamos el nombre de la tabla que se crea para establecer las relaciones entre usuarios y roles a "usuarios_roles".También personalizamos los nombres de las claves foráneas de esta tabla con "usuario_id",para hacer referencia a la clave primaria de la entidad Usuario,y con "role_id",para hacer referencia a la clave primaria de la entidad Role
	// En el atributo "joinColumns" ponemos el nombre de la clave foránea para la entidad dominante de la relación,es decir,esta clase entidad
	// En el atributo "inverseJoinColumns" ponemos el nombre de la clave foránea para la entidad del otro lado de la relación,es decir,la clase entidad Role
	// Al final hemos puesto los nombres por defecto para el nombre de la tabla y claves foráneas como nombres personalizados en la anotación @JoinTable para indicar un ejemplo de cómo se podría establecer otros nombres
	// Por último,con el atribtuo "uniqueConstraints" establecemos la restricción que indica que los pares de claves foráneas "usuario_id"-"role_id" tienen que ser únicos,es decir,en la tabla no puede haber más de un registro que tengan pares de "usuario_id"-"role_id" que se repitan.Esto es para que en la tabla no aparezca varias veces un usuario con el mismo role
	@JoinTable(name="usuarios_roles",joinColumns=@JoinColumn(name="usuario_id"),inverseJoinColumns=@JoinColumn(name="role_id"),uniqueConstraints= {@UniqueConstraint(columnNames= {"usuario_id","role_id"})})
	private List<Role> roles;
	
	private Integer intentos; // Propiedad que indica el número de intentos que tiene un usuario para realizar el proceso de autenticación.Si consume este número de intentos, su propiedad "enabled" se pondrá a false deshabilitándose su cuenta

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Integer getIntentos() {
		return intentos;
	}

	public void setIntentos(Integer intentos) {
		this.intentos = intentos;
	}
	
}
