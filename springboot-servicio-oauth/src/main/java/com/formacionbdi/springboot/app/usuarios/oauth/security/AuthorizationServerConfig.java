package com.formacionbdi.springboot.app.usuarios.oauth.security;

import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

// Esta clase se corresponde con la configuración de nuestro servidor de autorización con OAuth2


@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
@EnableAuthorizationServer // Con esta anotación habilitamos esta clase como un servidor de autorización
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
	
	// Recuperamos de la memoria o contendor de Spring el bean que implementa la interfaz "Environment".Esta interfaz es implementada por Spring
	@Autowired
	private Environment env; // Este bean de Spring contiene información sobre el contexto de desarrollo del proyecto.Lo vamos a usar para obtener el cliente id,su password y la clave secreta para firmar el token JWT desde el archivo de propiedades "application.properties" del servidor Spring Config Server
	
	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "PasswordEncoder".Esta interfaz es implementada por la clase de Spring Security BCryptPasswordEncoder
	@Autowired
	private PasswordEncoder passwordEncoder; // Este bean se trata del codificador de passwords con el algoritmos BCrypt

	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "AuthenticationManager".Esta interfaz es implementada por internamente por Spring
	@Autowired
	private AuthenticationManager authenticationManager; // Este bean se corresponde con  el Authentication Manager de Spring Security

	// Recuperamos del contenedor o memoria de Spring el bean que implementa la interfaz "TokenEnhancer".Esta interfaz es implementada por nuestra clase InfoAdicionalToken
	@Autowired
	@Qualifier("infoAdicionalToken") // Debido a que hay varias implementaciones de la interfaz "TokenEnhancer",una por el bean devuelto por el método "accessTokenConverter()" y otra por el bean de la clase "InfoAdicionalToken",tenemos que indicar con esta anotación que queremos inyectar el bean "infoAdicionalToken" con la implementación de la clase "InfoAdicionalToken" que es la crea un token potenciador con nuestra información personalizada adicional
	private TokenEnhancer tokenEnhancer; // Este bean se corresponde con el token potenciador,es decir,aquel que tiene información adicional del usuario para añadirselo al token JWT original
	
	// Sobrescribimos este método para configurar los permisos de nuestros endpoints del servidor de autorización(OAuth2) para generar el token JWT y para validarlo
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security.tokenKeyAccess("permitAll()") // El acceso al endopoint para pedir la generación del token JWT es público.Todo el mundo puede acceder a dicha solicitud.Hay que enviar una petición http Post a la ruta "/oauth/token"
		.checkTokenAccess("isAuthenticated()"); // El acceso al endpoint para validar el token JWT solo está permitido para usuarios autenticados
		// Estos dos endpoints están protegido por autenticación Header Authorization Basic utilizando las credenciales de la aplicación cliente(Client Id:Client Secret).Estas credenciales se envían en las cabeceras de cada petición http
	}

	// Sobrescribimos este método de AuthorizationServerConfigurerAdapter para registrar los clientes que pueden consumir los recursos de nuestra Api Rest
	// En nuestro caso,se va aconfigurar para que haya que enviar dos tipos de credenciales;una a nivel de aplicación cliente("forontendapp":"12345") y otra a nivel de usuario
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		/* Hay 3 tipos de autorización:
		 * "password" -> Además de las credenciales de la aplicación cliente,también se pide las credenciales del usuariocomo si fuera vía página de login(username y password).Si todo es correcto,se obtiene el token JWT
		 * "code" -> La autorización se realiza a través de un código de autorización que nos lo da el backend. Si es un código de autorización válido,se entrega el token JWT
		 * "implicit" -> Se trata de una autorización mucho más débil que el resto donde la aplicación cliente simplemente envía su id y su password y directamente se le proporciona el token JWT.Se suele utilizar en aplicaciones públicas que no requieren mucha seguridad */
		clients.inMemory() // El registro lo vamos a hacer en memoria con el uso del método "inMemory()",pero podría ser con JDBC o cualquier tipo de almacenamiento
		.withClient(env.getProperty("config.security.oauth.client.id")) // Registramos el cliente cuyo id lo obtenemos del la propiedad "config.security.oauth.client.id" del archivo "application.properties" de Spring Config Server
		.secret(passwordEncoder.encode(env.getProperty("config.security.oauth.client.secret"))) // Este cliente se registra con la contraseña, que se obtiene de la propiedad "config.security.oauth.client.secret" del archivo "application.properties" de Spring Config Server, codificada con BCrypt usando nuestro codificador de passwords
		.scopes("read","write") // Indicamos el alcance de este cliente a nuetra Api Rest,es decir,el tipo de peticiones http que puede realizar a nuestra Api Rest.En este caso,se permite peticiones http Get("read"),Post("write"),Put("write") y Delete("write")
		.authorizedGrantTypes("password","refresh_token") // Indicamos el tipo de autorización por el cual vamos a obtener el token JWT.En nuestro caso,vamos a usar el tipo "password" que consiste en enviar el username y la contraseña del usuario como credenciales para obtener el token JWT.Además,vamos a tener la concesión "refresh_token" que es un token adicioanl que nos permite generar un nuevo token JWT.Se suele utilizar para renovar el token JWT justo antes de que expire
		.accessTokenValiditySeconds(3600) // Establecemos un tiempo de expiración del token JWT en 3600seg(1 hora)
		// También establecemos un tiempo de expiración para el token de refresco en 3600seg(1 hora)
		// Normalmente el tiempo de expiración del token de refresco debe ser mayor que el tiempo de expiración del token JWT porque la funcionalidad del token de refesco es solictar un nuevo token JWT cuando haya exiprado el tiempo de validez del anteior token JWT
		.refreshTokenValiditySeconds(3600); // Para solicitar un nuevo token JWT a partir del token de refresco,en el body de la petición http Post al endpoint ".../oauth/token" el campo "grant_type" tiene que tener el valor "refresh_token" y debe existir otro campo llamado "refresh_token" con el token de refresco
		// a partir de aquí,usando el método "and()" junto con "withClient()" podemos seguir configurando otros clientes para que puedan usar nuestra Api Rest
	}

	// Sobrescribimos este método de AuthorizationServerConfigurerAdapter para registrar el Authentication Manger,el Token Storage como un JWT(Jason Web Token) y el Access Token Converter,que se encarga de generar el JWT guardando en él los datos del usuario codificados en base 64
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		// Esta instancia nos permite concatenar o añadir nuestro token potenciador(token con información adicional del usuario) al token JWT original
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		// Unimos nuestro token potenciador al token JWT original.Para ello,tenemos que pasar el token potenciador, en "tokenEnhancer", y el token original, en "accessTokenConverter()", como una lista al método "setTokenEnhancers()"
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer,accessTokenConverter())); // El método "accessTokenConverter()" nos da el token JWT original
		
		endpoints.authenticationManager(authenticationManager) 	// Registramos el Authentication Manager
		.tokenStore(tokenStore()) // Registramos nuestro Token Storage
		.accessTokenConverter(accessTokenConverter()) // Registramos nuestro Access Token Converter
		.tokenEnhancer(tokenEnhancerChain); // Registramos nuestro Token Enhancer o Token Potenciador
	}
	
	// Este método se encarga de convertir un token en un token JWT(Jason Web Token)
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método(se trata del conversor a nuestro token JWT) como un bean en su contenedor o memoria
	public JwtAccessTokenConverter accessTokenConverter() {
		// Creamos una instancia de JwtAccessTokenConverter que es el conversor a un token JWT
		JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
		// Establecemos la clave sereta con la que se va a firmar el token JWT.Esta clave se obtiene de la propiedad "onfig.security.oauth.jwt.key" del archivo "application.properties" de Spring Config Server
		// Códificamos la clave secreta en base 64 para que sea algo más robusta
		tokenConverter.setSigningKey(Base64.getEncoder().encodeToString(env.getProperty("config.security.oauth.jwt.key").getBytes()));
		// Devolvemos el conversor
		return tokenConverter;
	}
	
	// Este método se encarga de crear la instancia que genera y almacena el token JWT a partir de un Access Token Converter
	@Bean // Con esta anotación indicamos a Spring que almacene la instancia que devuelve este método como un bean en su contenedor o memoria
	public JwtTokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter()); // Crea y devuelve la instancia que genera y almacena el token JWT a partir de nuestro Access Token Converter(conversor a nuestro token JWT)
	}

	
}
