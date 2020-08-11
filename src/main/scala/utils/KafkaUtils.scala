package utils

import java.io.FileInputStream
import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

object KafkaUtils {

  def kafkaConsumer(properties: Properties): FlinkKafkaConsumer[String] = {

    new FlinkKafkaConsumer[String](
      properties.getProperty("kafkaConsumerTopic"),
      new SimpleStringSchema(),
      properties
    )
  }

  def kafkaProducerErrors(properties: Properties): FlinkKafkaProducer[String] = {
    new FlinkKafkaProducer[String](
      properties.getProperty("kafka.broker"),
      properties.getProperty("kafkaProducerErrorsTopic"),
      new SimpleStringSchema
    )
  }

  def kafkaProducerObfuscated(properties: Properties): FlinkKafkaProducer[String] = {
    new FlinkKafkaProducer[String](
      properties.getProperty("kafka.broker"),
      properties.getProperty("kafkaProducerObfuscatedTopic"),
      new SimpleStringSchema
    )
  }

  def getProperties(): Properties ={
    val props = new Properties()
    props.load(new FileInputStream("./config.properties"))
    props
  }

}
