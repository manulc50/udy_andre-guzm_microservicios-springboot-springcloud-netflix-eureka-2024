package com.formacionbdi.springboot.app.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

// Indicamos que esta clase se trata de un componente de Spring para almacene un bean de esta clase en su contenedor o memoria
// Como esta clase implementa la interfaz ZuulFilter de Netflix,Spring almacenará en su contenedor el bean como un filtro de Zuul
@Component
public class PostTiempoTranscurridoFilter extends ZuulFilter{
	
	// Habilitamos el log para usarlo en esta clase
	private static Logger log = LoggerFactory.getLogger(PostTiempoTranscurridoFilter.class);

	// Este método es para validar el filtro.Si devuelve true,se ejecuta la implementación del método "run".En caso contrario,no se ejecuta dicho método
	public boolean shouldFilter() {
		return true; // indicamos que se ejecute siempre el método "run"
	}

	// En este método se implementa el filtro tipo Post
	// La idea es obtener el tiempo de ejecución de la invocación a un microservicio
	// Para ello,vamos a usar dos filtros,uno de tipo Pre y otro Post,para que obtenga el tiempo inicial antes de dicha invocación en el request(filtro Pre) y el timpo final después de la invocación en el reponse(filtro Post)
	public Object run() throws ZuulException {
		// Obtenemos el contexto del request
		RequestContext ctx = RequestContext.getCurrentContext();
		// Obtenemos el request del contexto
		HttpServletRequest request = ctx.getRequest();
		// Escribimos en el log como información el texto "Entrando a post filter"
		log.info("Entrando a post filter");
		// Obtenemos el tiempo del atributo "tiempoInicio" del request que fue generado en el filtro tipo Pre
		Long tiempoInicio = (Long)request.getAttribute("tiempoInicio");
		// Calculamos el tiempo de ejecución de la invocación al microservicio restanto el tiempo actual en milisegundos del sistema al tiempo de inicio obtenido del request
		Long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
		// Escribimos en el log como información el tiempo en segundos como un decimal de la invocación al microservicio
		log.info(String.format("Tiempo transcurrido en segundos: %s seg",tiempoTranscurrido.doubleValue()/1000.00));
		// Escribimos en el log como información el tiempo en milisegundos de la invocación al microservicio
		log.info(String.format("Tiempo transcurrido en milisegundos: %s miliseg",tiempoTranscurrido));
		return null;
	}

	// Este método se sobrescribe para definir el tipo de filtro(Pre,Post,Route,Error)
	// El filtro tipo Pre(palabra clave a devolver "pre") se ejecuta antes de la comunicación con el microservicio
	// El filtro tipo Post(palabra clave a devolver "post") se ejecuta después de la comunicación con el microservicio
	// El filtro tipo Route(palabra clave a devolver "route") se ejecuta durante la comunicación con el microservicio
	// El filtro tipo Error se ejecuta si ocurre un error en la comunicación con el microservicio
	@Override
	public String filterType() {
		return "post"; // Para los filtros tipo Post siempre tiene que devolver el texto "post"
	}

	// Este método se sobrescribe para definir el orden de ejecución de los filtros(si tuvieramos más de uno)
	@Override
	public int filterOrder() {
		return 1;
	}

}
