package transformer

import java.util.Properties

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.slf4j.LoggerFactory
import utils.KafkaUtils
import ujson.{Js, Value}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

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

    ObfuscateDataStream.startStream(properties, env)
  }
}


object testUJson extends App {

  val jsonStr =
    """{
      | "order" :{
      |   "user":{
      |      "name": "Nombre",
      |      "lastname": "Apellido"
      |   },
      |   "products": [
      |     "item1", "item2", "item3"
      |   ]
      | }
      |}""".stripMargin


  val path = "order.user.lastname".split('.').toList
  val json = ujson.read(jsonStr)

  def obfuscate(json: Value, path: List[String], keyIndex: Int): Unit = {
    val key: String = path(keyIndex)
    Try(json(key)) match {
      case Failure(ex) => throw new Exception(ex)
      case Success(_) => if (key == path.last) json(key) = "XXX" else obfuscate(json(key), path, keyIndex+1 )
    }
  }
  print(json.toString()+"\n")
  obfuscate(json, path, 0)
  print(json.toString())
}
