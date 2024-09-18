package com.formacionbdi.springboot.app.usuarios.oauth.services;

import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

//Se utiliza la clase entidad "Usuario" de nuestra librería común de usuarios

public interface IUsuarioService {
	
	public Usuario findByUsername(String username); // Método que localiza y devuelve los datos de un usuario dado su username

	public Usuario update(Usuario usuario,Long id); // Método que actualiza los datos de un usuario dado su id
}
