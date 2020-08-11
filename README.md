### data-obfusc-transformer

Este proyecto consiste en una funcion de transformacion que ofusca datos sensibles en eventos con formato JSON.
Los parámetros para configurar el consumer, producer y tópicos de Kakfa se encuentran en "config.properties".
Para configurarlos:
1) Ir al archivo config.properties
2) En "kafkaConsumerTopic" colocar el nombre del tópico del Consumer desde donde provienen los eventos.
3) En "kafkaProducerObfuscatedTopic" colocar el nombre del tópico del Producer hacia donde irán los eventos transformados.
4) En el topic "kafkaProducerErrorsTopic" irán los eventos invalidos que no hayan podido ser transformados.
5) Configurar "bootstrap.servers" y "kafka.broker" segun los hosts y puertos correspondientes (también pueden pasarse como variables de entorno.)
