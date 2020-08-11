package transformer

import java.util.Properties
import org.apache.flink.streaming.api.scala._

import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.slf4j.LoggerFactory
import utils.KafkaUtils

import scala.concurrent.ExecutionContext

object ObfuscateDataStream {

  implicit val ec: ExecutionContext = ExecutionContext.global
  val logger: org.slf4j.Logger = LoggerFactory.getLogger(getClass)

  def startStream(properties: Properties, env: StreamExecutionEnvironment): Unit = {

    val pathsToObfuscate: List[String] = properties.getProperty("pathsToObfuscate").split("/").toList
    val consumer = KafkaUtils.kafkaConsumer(properties)

    val mapSideOutputs = Map(
      "json-obfuscated" -> OutputTag[String]("json-obfuscated"),
      "json-error" -> OutputTag[String]("json-error")
    )

    val mapProducers = Map(
      "json-obfuscated" -> KafkaUtils.kafkaProducerObfuscated(properties),
      "json-error" -> KafkaUtils.kafkaProducerErrors(properties)
    )

    val stream: DataStream[String] = env.addSource(consumer).rebalance
    val streamProcessed: DataStream[String] =
      stream.process{new ProcessFunctionObf(mapSideOutputs, pathsToObfuscate, logger)}

    mapSideOutputs.foreach { case (key, outputtag) =>
      streamProcessed.getSideOutput(outputtag).addSink(mapProducers(key))
    }

    env.execute()
  }

}