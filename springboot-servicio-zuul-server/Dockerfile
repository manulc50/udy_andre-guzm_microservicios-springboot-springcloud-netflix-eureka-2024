# Partimos de la imagen openjdk con tag o versión 12
FROM openjdk:12

# Montamos el volumen /tmp porque el sevidor Tomcat embebido en las aplicaciones SpringBoot almacena los logs en la carpeta /tmp
VOLUME /tmp

# Exponemos el puerto 8090 del contenedor que es el puerto donde se va a ejecutar nuestra aplicación SpringBoot
EXPOSE 8090

# Copiamos nuestra aplicación SpringBoot a la raíz del contenedor con el nombre "zuul-server.jar"
ADD ./target/springboot-servicio-zuul-server-0.0.1-SNAPSHOT.jar zuul-server.jar

# Cuando se ejecute el contendor de esta imagen,se va a ejecutar el comando "java -jar zuul-server.jar" para que se ejecute nuestra aplicación "zuul-server.jar" en el puerto 8090 del contenedor
# Nuestra aplicación SpringBoot se encuentra en la raíz del contendor,así que ponemos "/zuul-server.jar",con la barra inclinada,para referirnos a él
ENTRYPOINT ["java","-jar","/zuul-server.jar"] 