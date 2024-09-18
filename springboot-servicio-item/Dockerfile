# Partimos de la imagen openjdk con tag o versión 12
FROM openjdk:12

# Montamos el volumen /tmp porque el sevidor Tomcat embebido en las aplicaciones SpringBoot almacena los logs en la carpeta /tmp
VOLUME /tmp

# Exponemos el puerto 8002 del contenedor que es el puerto donde se va a ejecutar nuestra aplicación SpringBoot
EXPOSE 8002

# Copiamos nuestra aplicación SpringBoot a la raíz del contenedor con el nombre "servicio-item.jar"
ADD ./target/springboot-servicio-item-0.0.1-SNAPSHOT.jar servicio-item.jar

# Cuando se ejecute el contendor de esta imagen,se va a ejecutar el comando "java -jar servicio-item.jar" para que se ejecute nuestra aplicación "servicio-item.jar" en el puerto 8002 del contenedor
# Nuestra aplicación SpringBoot se encuentra en la raíz del contendor,así que ponemos "/servicio-item.jar",con la barra inclinada,para referirnos a él
ENTRYPOINT ["java","-jar","/servicio-item.jar"] 