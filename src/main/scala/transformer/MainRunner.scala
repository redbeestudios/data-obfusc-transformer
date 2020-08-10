package transformer

import java.util.Properties

import com.typesafe.config.Config
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.slf4j.LoggerFactory
import utils.KafkaUtils

import scala.concurrent.ExecutionContext

object MainRunner extends App {
  /**
   * Al correr esta App, se crea el consumer que escucha el tópico de Kafka

   */
  override def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(100)
    val properties: Properties = KafkaUtils.getProperties()

    ObfuscateDataRunner.buildStream(properties, env)
  }
}

object ObfuscateDataRunner {

  implicit val ec: ExecutionContext = ExecutionContext.global
  val logger: org.slf4j.Logger = LoggerFactory.getLogger(getClass)

  def buildStream(properties: Properties, env: StreamExecutionEnvironment): Unit = {



  }

}
