# Partimos de la imagen openjdk con tag o versión 12
FROM openjdk:12

# Montamos el volumen /tmp porque el sevidor Tomcat embebido en las aplicaciones SpringBoot almacena los logs en la carpeta /tmp
VOLUME /tmp

# En este caso no ponemos la instrucción EXPOSE para exponer un puerto del contenedor porque esta aplicación SpringBoot genera un puerto de ejecución aleatorio y no podemos saberlo

# Copiamos nuestra aplicación SpringBoot a la raíz del contenedor con el nombre "servicio-usuarios.jar"
ADD ./target/springboot-servicio-usuarios-0.0.1-SNAPSHOT.jar servicio-usuarios.jar

# Cuando se ejecute el contendor de esta imagen,se va a ejecutar el comando "java -jar servicio-usuarios.jar" para que se ejecute nuestra aplicación "servicio-usuarios.jar" en un puerto aleatorio del contenedor
# Nuestra aplicación SpringBoot se encuentra en la raíz del contendor,así que ponemos "/servicio-usuarios.jar",con la barra inclinada,para referirnos a él
ENTRYPOINT ["java","-jar","/servicio-usuarios.jar"] 