# Partimos de la imagen openjdk con tag o versión 12
FROM openjdk:12

# Montamos el volumen /tmp porque el sevidor Tomcat embebido en las aplicaciones SpringBoot almacena los logs en la carpeta /tmp
VOLUME /tmp

# Exponemos el puerto 8761 del contenedor que es el puerto donde se va a ejecutar nuestra aplicación SpringBoot
EXPOSE 8761

# Copiamos nuestra aplicación SpringBoot a la raíz del contenedor con el nombre "eureka-server.jar"
ADD ./target/springboot-servicio-eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar

# Cuando se ejecute el contendor de esta imagen,se va a ejecutar el comando "java -jar eureka-server.jar" para que se ejecute nuestra aplicación "eureka-server.jar" en el puerto 8761 del contenedor
# Nuestra aplicación SpringBoot se encuentra en la raíz del contendor,así que ponemos "/eureka-server.jar",con la barra inclinada,para referirnos a él
ENTRYPOINT ["java","-jar","/eureka-server.jar"] 