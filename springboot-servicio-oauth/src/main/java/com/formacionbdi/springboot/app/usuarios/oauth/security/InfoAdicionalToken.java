package com.formacionbdi.springboot.app.usuarios.oauth.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;
import com.formacionbdi.springboot.app.usuarios.commons.models.entity.Usuario;

//Se utiliza la clase entidad "Usuario" de nuestra librería común de usuarios

import com.formacionbdi.springboot.app.usuarios.oauth.services.IUsuarioService;

// Esta clase implementa lainterfaz TokenEnhancer para poder crear un token JWT potenciador,es decir,un token JWT con información adicional

@Component // Indicamos que esta clase se trata de un componente de Spring para que lo almacene como un bean en su contenedor o memoria y,de esta forma,poder inyectarlo y usarlo en otra parte del proyecto
public class InfoAdicionalToken implements TokenEnhancer {
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "IUsuarioService".Esta interfaz es implementada por la clase "UsuarioService"
	@Autowired
	private IUsuarioService usuarioService; // Es un bean de la capa de servicio que se encarga de obtener los datos de un usuario(a partir de su username) o de actualizarlos(a partir de los datos de la petición http y de su id) usando nuestro cliente Api Rest Feign de Netflix que consume los recursos del microservicio "servicio-usuarios"

	// Sobrescribimos este método de TokenEnhancer para añadir la información adicional de un usuario dado su username al accessToken
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		// Creamos un HashMap donde vamos a almacenar la información adicional que queremos meter en el token JWT
		Map<String,Object> info = new HashMap<String,Object>();
		// Obtenemos los datos de un usuario dado su username a partir de la capa de servicio "usuarioService" que a su vez hace uso de nuestro nuestro cliente Api Rest Feign que consume recursos del microservicio "servicio-usuarios"
		Usuario usuario = usuarioService.findByUsername(authentication.getName()); // authentication.getName() nos da el username del usuario
		// Insertamos en el HashMap el nombre,apellido y email del usuario obtenido por su username asociados a los atributos "nombre","apellido" y "email" respectivamente
		info.put("nombre",usuario.getNombre());
		info.put("apellido",usuario.getApellido());
		info.put("email",usuario.getEmail());
		// Añadimos la información adicional contenida en el HashMap "info" al accessToken
		// Como accessToken es de tipo OAuth2AccessToken, que es una interfaz,necesaitamos hacer un cast a la implementación de esta interfaz DefaultOAuth2AccessToken que posee el método "setAdditionalInformation()" para añadir información extra al token
		((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(info);
		return accessToken;
	}

}
