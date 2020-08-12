FROM anapsix/alpine-java
COPY ./target/scala-2.11/data-obf-transformer-assembly-0.1.jar /home/data-obf-transformer.jar
COPY ./config.properties ./home/config.properties
WORKDIR /home
CMD ["java","-jar","./data-obf-transformer.jar"]
