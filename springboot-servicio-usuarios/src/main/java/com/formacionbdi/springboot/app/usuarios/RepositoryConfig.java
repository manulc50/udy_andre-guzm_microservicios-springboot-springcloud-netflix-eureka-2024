package com.formacionbdi.springboot.app.usuarios;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Role;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

// Se utilizan las clases entidad "Usuario" y "Role" de nuestra librería común de usuarios

// Esta clase de configuración es para que se muestre(por defecto no lo hace) la propiedad "id" de los objetos de las entidades "Usuario" y "Role" en los Json que devuelven las peticiones http Get,Post,Put y Delete cuando el controlador y la capa de servicio son implementados automáticamnete por Spring a través de la anotación @RepositoryRestResource

@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
public class RepositoryConfig implements RepositoryRestConfigurer {

	// Sobrescribimos este método de la clase RepositoryRestConfigurer para que se expongan los ids de las entidades "Usuario" y "Role" en los Json devueltos por las peticiones http al controlador implementado por Spring con la anotación RepositoryRestResource
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Usuario.class,Role.class);
	}

}
