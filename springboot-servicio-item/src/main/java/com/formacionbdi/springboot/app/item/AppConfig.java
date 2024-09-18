package com.formacionbdi.springboot.app.item;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration // Indicamos que esta clase es una clase de Configuración de Spring y,de esta manera,Spring va a almacenar un bean de esta clase en su contenedor o memoria
public class AppConfig {
	
	// Este método devuelve una instancia de RestTemplate que es un cliente que trabaja con API Rest y nos permite acceder a recursos que están en otros microservicios
	@Bean("clienteRest") // Spring va a almacenar el retorno de este método como un bean en su contenedor o memoria y lo va a llamar "clienteRest"
	@LoadBalanced // Con esta anotación, de manera automática,va a integrar el balanceador de carga Ribbon de Netflix con el cliente API Rest de Spring "RestTemplate"
	public RestTemplate registarRestTemplate() {
		return new RestTemplate(); // 
	}
	
	// Bean de Spring con nuestra configuración personalizada del "CircuitBreaker" Resilience4J
	// Nota: Parámetros por defecto: "slidingWindowsSize"=100, "failureRateThreshold"=50, "waitDurationInOpenState"=60000ms, "permittedNumberOfCallsInHalfOpenState"=10, "slowCallRateThreshold"=100 y "slowCllDurationThreshold"=60000ms
	// Nota: Timeout por defecto: 1 seg(propiedad "timeoutDuration" de Time Limiter)
	// Nota: Las configuraciones de Resilience4j indicadas en un archivo .yml, .yaml o .properties tienen prioridad respecto a las configuraciones de Resilience4j indicadas programáticamente en clases de configuración
    //       Por esta razón, la configuración del archivo "application.yml" tiene prioridad sobre la configuración de este bean "defaultCustomizer"
	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
		// Esta función lambda implementa la interfaz funcional "Customizer"(implementa el método "customize" de dicha interfaz)
		// El argumento de entrada "id" se corresponde con un id de un Circuit Breaker(podemos tener más de uno). De esta forma, es posible configurar que cada Circuit Breaker tenga su propia configuración(En este caso, todos los Circuit Breakers tiene la misma configuración)
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				// Personalizamos los parámetros del Circuit Breaker(se usa el método "custom")
				// El método "ofDefaults" es para usar los parámetros por defecto
				.circuitBreakerConfig(CircuitBreakerConfig.custom()
						.slidingWindowSize(10)
						.failureRateThreshold(50)
						.waitDurationInOpenState(Duration.ofSeconds(10l))
						.permittedNumberOfCallsInHalfOpenState(5)
						.slowCallRateThreshold(50)
						.slowCallDurationThreshold(Duration.ofSeconds(2l)) // Si una llamada supera este tiempo, se considera una llamada lenta y se tiene en cuenta para abrir el circuito 
						.build())
				// Usamos los parámetros por defecto para el Time Limiter
				// Personalizamos el Time Limiter(se usa el método "custom")
				// Nota: Si el tiempo del timeout es igual al tiempo de la propiedad "slowCallDurationThreshold", tiene prioridad ese timeout. Por lo tanto, para que las llamadas lentas se tengan en cuenta, el tiempo de ese timeout debe ser superior al tiempo establecido en la propiedad "slowCallDurationThreshold" y debe permitir que la llamada se complete y responda
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(6l)).build())
				.build()
		);
	}

}
