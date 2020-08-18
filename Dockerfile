FROM anapsix/alpine-java
ENV KAFKA_BROKER ""
COPY ./target/scala-2.11/data-obf-transformer-assembly-0.1.jar /home/data-obf-transformer.jar
COPY ./config.properties ./home/config.properties
WORKDIR /home
ENTRYPOINT ["java","-Dkafka.broker=$KAFKA_BROKER", "-jar","./data-obf-transformer.jar"]
