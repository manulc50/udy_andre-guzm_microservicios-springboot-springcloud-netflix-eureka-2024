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
public class PreTiempoTranscurridoFilter extends ZuulFilter{
	
	// Habilitamos el log para usarlo en esta clase
	private static Logger log = LoggerFactory.getLogger(PreTiempoTranscurridoFilter.class);

	// Este método es para validar el filtro.Si devuelve true,se ejecuta la implementación del método "run".En caso contrario,no se ejecuta dicho método
	public boolean shouldFilter() {
		return true; // indicamos que se ejecute siempre el método "run"
	}

	// En este método se implementa el filtro tipo Pre
	// La idea es obtener el tiempo de ejecución de la invocación a un microservicio
	// Para ello,vamos a usar dos filtros,uno de tipo Pre y otro Post,para que obtenga el tiempo inicial antes de dicha invocación en el request(filtro Pre) y el timpo final después de la invocación en el reponse(filtro Post)
	public Object run() throws ZuulException {
		// Como estamos implementando un filtro tipo "Pre",el filtro se aplica al contexto del request(parte antes de invocar al microservicio)
		RequestContext ctx = RequestContext.getCurrentContext();
		// Obtenemos el request del contexto
		HttpServletRequest request = ctx.getRequest();
		// Escribimos en el log como información el tipo de filtro('request.getMethod()') y la url del request o petición('request.getRequestURL().toString()')
		log.info(String.format("%s request enrutado a %s",request.getMethod(),request.getRequestURL().toString()));
		// Obtenemos el timepo del sistema en milisegundos
		Long tiempoInicio = System.currentTimeMillis();
		// Añadimos el tiempo de inicio al request como un parámetro con nombre "tiempoInicio"
		request.setAttribute("tiempoInicio",tiempoInicio);
		return null;
	}

	// Este método se sobrescribe para definir el tipo de filtro(Pre,Post,Route y Error)
	// El filtro tipo Pre(palabra clave a devolver "pre") se ejecuta antes de la comunicación con el microservicio
	// El filtro tipo Post(palabra clave a devolver "post") se ejecuta después de la comunicación con el microservicio
	// El filtro tipo Route(palabra clave a devolver "route") se ejecuta durante la comunicación con el microservicio
	// El filtro tipo Error se ejecuta si ocurre un error en la comunicación con el microservicio
	@Override
	public String filterType() {
		return "pre"; // Para los filtros tipo Pre siempre tiene que devolver el texto "pre"
	}

	// Este método se sobrescribe para definir el orden de ejecución de los filtros(si tuvieramos más de uno)
	@Override
	public int filterOrder() {
		return 1;
	}

}
