### data-obfusc-transformer

Este proyecto consiste en una funcion de transformacion que ofusca datos sensibles en eventos con formato JSON.
Los parámetros del consumer, producer, tópicos de Kakfa, hosts y puertos se pueden configurar en el archivo "config.properties", o bien pasarse como variables de entorno
al correr la imagen de Docker del proyecto.

Para cambiarlos desde el archivo de configuración:
1) Ir al archivo config.properties
2) En "kafkaConsumerTopic" colocar el nombre del tópico del Consumer desde donde provienen los eventos.
3) En "kafkaProducerObfuscatedTopic" colocar el nombre del tópico del Producer hacia donde irán los eventos transformados.
4) En el topic "kafkaProducerErrorsTopic" irán los eventos invalidos que no hayan podido ser transformados.
5) Configurar "bootstrap.servers", "kafka.broker" y "zookeeper.connect" segun los hosts y puertos correspondientes.
6) en "pathsToObfuscate" colocar los paths de aquellos campos que contengan datos sensibles, separados por ",". Por ejemplo, si un evento json tiene esta estructura:

{
 "order":
	{
   	 "user":
		{
      	 	"name": "Nombre",
      	 	"lastname": "Apellido"
   	 	},
   	 "products": ["item1", "item2", "item3"]
 	 }
}

y se quieren ofuscar los valores en "name" y "lastname", luego los paths serán order.user.name,order.user.lastname

Para buildear la imagen de Docker, ir al directorio del proyecto y ejectuar:
1) sbt assembly  (exporta el jar del proyecto)
2) docker build -t zelig-data-obfuscate:1.0.0-SNAPSHOT .    (genera la imagen de Docker)

Una vez hecho esto, se puede correr la imagen (incluyendo variables de entorno opcionalmente) con:

docker run -e bootstrap.server={host:port} \
	   -e kafka.broker={host:port} \
           -e zookeeper.connect={host:port} \
	   -e kafkaConsumerTopic={topic-name} \
           -e kafkaProducerErrorsTopic={topic-name} \
           -e kafkaProducerObfuscatedTopic={topic-name} \
           -e pathsToObfuscate={paths-to-obfuscate} \
           zelig-data-obfuscate:1.0.0-SNAPSHOT

