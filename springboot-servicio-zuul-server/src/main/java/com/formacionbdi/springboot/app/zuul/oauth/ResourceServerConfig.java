package com.formacionbdi.springboot.app.zuul.oauth;

import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Esta clase se trata de una clase de configuración de Spring que configura nuestro servidor de recursos para proteger todos los accesos a nuestros endpoints de los distintos microservicios o recursos y dar el acceso a los clientes en función del token JWT que se envie junto con la cabecera de cada petición http.También se encarga de validar que este token JWT sea el correcto a través de la misma firma con la que se generó en el servidor de autorización(Oauth2)

// Nota: La anotación "@RefreshScope" sólo es necesaria si se usa la anotación "@Value" para inyectar valores de propiedades de archivos de configuraciones. Si se usa una clase de configuración anotada con "@ConfigurationProperties" o se usa el bean que implementa la interfaz "Environment" para ese proposito, no es necesario usar la anotación "@RefreshScope"

@RefreshScope // Esta anotación nos permite actualizar en tiempo de ejecución y real los componentes como controladores,clases anotadas con @Component,@Service,@Configuration,etc...donde estamos inyectando propiedades de configuraciones con @Value
@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
@EnableResourceServer // Con esta anotación habilitamos esta clase como un servidor de recursos
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

	// La anotación @Value nos permite obtener los valores de las propiedades configuradas en los archivos "application.properties" y aquellas que se encuentran en Spring Config Server
	// Con esta anotación obtenemos el valor de la propiedad "onfig.security.oauth.jwt.key" del archivo "application.properties" de Spring Config Server y se inyecta en la propiedad de esta clase "jwtkey"
	@Value("${config.security.oauth.jwt.key}")
	private String jwtkey;
	
	// Sobrescribimos este método para configurar el token JWT que tiene que validar para cada petición http que se produzca.Este token JWT tiene que tener la misma estructura con la que se generó en el servidor de autorizacion(Oauth2)
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.tokenStore(tokenStore()); // registramos nuestro Token Storage que tiene que tener la misma estructura o la misma implementación que el Token Storage de la clase AuthorizationServerConfig(nuestro servidor de autorización Oauth2)
	}
	
	// Sobrescribimos este método de ResourceServerConfigurerAdapter para configurar los permisos a los endpoints o rutas de los recursos de nuestra Api Rest
	@Override
	public void configure(HttpSecurity http) throws Exception {
		// El acceso al endpoint "/api/security/oauth/**"("/**" significa cualquier ruta que contenga la base "/api/security/oauth/",pero en realidad nosotros solo tenemos disponible la ruta "/api/security/oauth/token" para peticiones http Post para la generación y obtencion del token JWT) está permitido a cualquier usuario.Todo el mundo puede solictar,mediante este endpoint,hacer login para la generación y obteneción del token JWT
		http.authorizeRequests().antMatchers("/api/security/oauth/**").permitAll()
		// El acceso a las petciones http Get a los endpoints "/api/productos/listar"(lista todos los productos),"/api/items/listar"(lista todos los items de productos),"/apit/items/ver/{id}/cantidad/{cantidad}"(ver el destalle de un item dado el id de un producto y su cantidad) y "/api/usuarios/usuarios"(lista todos los usuarios del sistema) está peritido a cualquier persona.Cualquier usuario puede ver el listado de productos,el listado de items y el listado de usuarios
		.antMatchers(HttpMethod.GET,"/api/productos/listar","/api/items/listar","/api/items/ver/{id}/cantidad/{cantidad}","/api/usuarios/usuarios").permitAll()
		// El acceso a las petciones http Get a los endpoints "/api/productos/ver/{id}"(ver el destalle de un producto dado su id) y "/api/usuarios/usuarios/{id}"(ver el detalle de un usuario dado su id) solo está permitido a usuarios autenticados con roles "ADMIN" y/o "USER"
		.antMatchers(HttpMethod.GET,"/api/productos/ver/{id}","/api/usuarios/usuarios/{id}").hasAnyRole("ADMIN","USER") // El método "hasRole(role)" es para indicar un único role y el método "hasAnyRole(roles)" es para indicar más de un role.En los nombre de los roles indicados en estos métodos no hace falta ponder el prefijo "ROLE_" ya que se añade por Spring automáticamente por debajo cunado se trata de la validación o verificación de roles
		// El acceso a las peticiones http Post a los endpoints "/api/productos/crear"(crear un nuevo producto),"/api/items/crear"(crear un nuevo item) y "/api/usuarios/usuarios"(crear un nuevo usuario) solo está permitido a usuarios autenticados con role "ADMIN"
		//.antMatchers(HttpMethod.POST,"/api/productos/crear","/api/items/crear","/api/usuarios/usuarios").hasRole("ADMIN") // El método "hasRole(role)" es para indicar un único role y el método "hasAnyRole(roles)" es para indicar más de un role.En los nombre de los roles indicados en estos métodos no hace falta ponder el prefijo "ROLE_" ya que se añade por Spring automáticamente por debajo cunado se trata de la validación o verificación de roles
		// El acceso a las peticiones http Put a los endpoints "/api/productos/editar/{id}"(actualizar un producto dado su id),"/api/items/editar/{id}"(actualizar un item dado el id de un producto) y "/api/usuarios/usuarios/{id}"(actualizar un usuario dado su id) solo está permitido a usuarios autenticados con role "ADMIN"
		//.antMatchers(HttpMethod.PUT,"/api/productos/editar/{id}","/api/items/editar/{id}","/api/usuarios/usuarios/{id}").hasRole("ADMIN") // El método "hasRole(role)" es para indicar un único role y el método "hasAnyRole(roles)" es para indicar más de un role.En los nombre de los roles indicados en estos métodos no hace falta ponder el prefijo "ROLE_" ya que se añade por Spring automáticamente por debajo cunado se trata de la validación o verificación de roles
		// El acceso a las peticiones http Delete a los endpoints "/api/productos/eliminar/{id}"(eliminar un producto dado su id),"/api/items/eliminar/{id}"(eliminar un item dado el id de un producto) y "/api/usuarios/usuarios/{id}"(eliminar un usuario dado su id) solo está permitido a usuarios autenticados con role "ADMIN"
		//.antMatchers(HttpMethod.DELETE,"/api/productos/eliminar/{id}","/api/items/eliminar/{id}","/api/usuarios/usuarios/{id}").hasRole("ADMIN"); // El método "hasRole(role)" es para indicar un único role y el método "hasAnyRole(roles)" es para indicar más de un role.En los nombre de los roles indicados en estos métodos no hace falta ponder el prefijo "ROLE_" ya que se añade por Spring automáticamente por debajo cunado se trata de la validación o verificación de roles
		// Los 3 últimos "antMatchers" los podemos simplificar con este único "antMatcher" usando como base de los endpoints "/api/productos/","/api/items/" y "/api/usuarios/" y a continuación la expresión regular "**",que signifca cualquier endpoint que tenga como base las mencionadas anteriormente,para cualquier tipo de petición http(Get,Post,Put o Delete)
		.antMatchers("/api/productos/**","/api/items/**","/api/usuarios/**").hasRole("ADMIN") // El método "hasRole(role)" es para indicar un único role y el método "hasAnyRole(roles)" es para indicar más de un role.En los nombre de los roles indicados en estos métodos no hace falta ponder el prefijo "ROLE_" ya que se añade por Spring automáticamente por debajo cunado se trata de la validación o verificación de roles
		// Cualquier otro endpoint o ruta que no se haya especificado anteriormente,requiere autenticación
		.anyRequest().authenticated()
		.and()
		// Habilitamos y aplicamos nuestra configuración Cors a nivel de Spring Security
		.cors().configurationSource(corsConfigurationSource());
	}
	
	// CORS o el intercambio de recursos de origen cruzado(que residen en distintos dominios).A grandes resgos,es un mecanismo que utiliza las cabeceras http para permitir que una aplicación cliente que reside en otro servidor o domino distinto al backend(nuestra Api Rest) tenga los permisos para acceder a los recursos del backend
	
	// Este método es para configurar las caracteristicas de nuestro Cors
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método como un bean en su contenedor o memoria
	public CorsConfigurationSource corsConfigurationSource() {
		// Esta instancia nos permite implementar nuestra configuración Cors
		CorsConfiguration corsConfig = new CorsConfiguration();
		// Se permite el acceso a los recursos de nuestra Api Rest a cualquier("*") dominio origen
		corsConfig.setAllowedOrigins(Arrays.asList("*"));
		// Las peticiones http "GET","POST","PUT","DELETE" y "OPTIONS" están permitidas
		corsConfig.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS")); // Es importante permitir el tipo de peticion "OPTIONS" porque por debajo lo utiliza Oauth2
		// El uso de credenciales en las peticiones también está permitido
		corsConfig.setAllowCredentials(true);
		// Las cabeceras permitidas de la peticiones http son "Authorization" y "Content-Type"
		corsConfig.setAllowedHeaders(Arrays.asList("Authorization","Content-Type"));
		// Esta instancia es para indicar a qué rutas vamos a aplicar nuestra configuración Cors
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// Aplicamos nuestra configuración Cors a cualquier("/**") ruta que nos llegue desde una aplicación origen
		source.registerCorsConfiguration("/**", corsConfig);
		return source;
	}
	
	// Este método aplica un filtro para que se aplique nuestra configuración de Cors a nivel global de la aplicación
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método como un bean en su contenedor o memoria
	public FilterRegistrationBean<CorsFilter> corsFilter(){
		// Creamos un filtro de tipo CorsFilter usando nuestra configuración Cors devuelta por nuestro método "corsConfigurationSource()"
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(corsConfigurationSource()));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Damos prioridad alta a nuestro filtro
		return bean;
	}
	
	/* Estos dos métodos de abajo también se encuentran en la clase AuthorizationServerConfig,que representa nuestro servidor de autorización con OAuth2,y tienen la misma implementación que los métodos de aquí.
	 * Esto es así,porque la estructura del token JWT que se genera en la clase AuthorizationServerConfig(nuestro servidor de autorización Oauth2) tiene que ser la misma que la estructura del token JWT que se valida en esta otra clase(nuestro servidor de recursos)*/
	
	// Este método se encarga de convertir un token en un token JWT(Jason Web Token)
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método(se trata del conversor a nuestro token JWT) como un bean en su contenedor o memoria
	public JwtAccessTokenConverter accessTokenConverter() {
		// Creamos una instancia de JwtAccessTokenConverter que es el conversor a un token JWT
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		// Establecemos la clave sereta con la que se va a firmar el token JWT.Esta clase secreta se obtiene de la propiedad de esta clase "jwtkey" que contiene el valor de la propiedad "config.security.oauth.jwt.key" del archivo "application.properties" de Spring Config Server
		// Códificamos la clave secreta en base 64 para que sea algo más robusta
		tokenConverter.setSigningKey(Base64.getEncoder().encodeToString(jwtkey.getBytes()));
		// Devolvemos el conversor
		return tokenConverter;
	}
	
	// Este método se encarga de crear la instancia que genera y almacena el token JWT a partir de un Access Token Converter
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método como un bean en su contenedor o memoria
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter()); // Crea y devuelve la instancia que genera y almacena el token JWT a partir de nuestro Access Token Converter(conversor a nuestro token JWT)
	}

}
