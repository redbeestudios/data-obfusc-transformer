package transformer

import java.util.Properties

import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import utils.KafkaUtils


object MainRunner extends App {
  /**
   * Al correr esta App, se crea el consumer que escucha el tópico de Kafka

   */
  override def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(100)
    val properties: Properties = KafkaUtils.getProperties()

    System.getProperty("bootstrap.servers") match {
      case prop: String => properties.setProperty("bootstrap.servers", prop)
      case _ => ""
    }
    System.getProperty("kafka.broker") match {
      case prop: String => properties.setProperty("kafka.broker", prop)
      case _ => ""
    }
    System.getProperty("zookeeper.connect") match {
      case prop: String => properties.setProperty("zookeeper.connect", prop)
      case _ => ""
    }

    System.getProperty("kafkaConsumerTopic") match {
      case prop: String => properties.setProperty("kafkaConsumerTopic", prop)
      case _ => ""
    }

    System.getProperty("kafkaProducerErrorsTopic") match {
      case prop: String => properties.setProperty("kafkaProducerErrorsTopic", prop)
      case _ => ""
    }

    System.getProperty("kafkaProducerObfuscatedTopic") match {
      case prop: String => properties.setProperty("kafkaProducerObfuscatedTopic", prop)
      case _ => ""
    }

    System.getProperty("pathsToObfuscate") match {
      case prop: String => properties.setProperty("pathsToObfuscate", prop)
      case _ => ""
    }

    ObfuscateDataStream.startStream(properties, env)
  }
}

