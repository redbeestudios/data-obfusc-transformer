FROM anapsix/alpine-java
COPY ./target/scala-2.11/data-obf-transformer-assembly-0.1.jar /home/data-obf-transformer.jar
COPY ./config.properties ./home/config.properties
COPY /start.sh /home/start.sh

WORKDIR /home
RUN chmod a+rx start.sh
ENTRYPOINT ["./start.sh"]
#ENTRYPOINT ["java","-Dkafka.broker=$KAFKA_BROKER", "-jar","./data-obf-transformer.jar"]
