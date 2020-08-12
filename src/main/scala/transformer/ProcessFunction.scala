package transformer

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector
import ujson.Value

import scala.util.{Failure, Success, Try}

class ProcessFunctionObf(sideOutputsMap: Map[String, OutputTag[String]],
                         pathsToObfuscate: List[String],
                         logger: org.slf4j.Logger) extends ProcessFunction[String, String] {

  override def processElement(data: String,
                              ctx: ProcessFunction[String, String]#Context,
                              out: Collector[String]): Unit = {
    /**
     * Procesa el HarnessEvent para pasarlo a Json. Luego lo incluye en el correspondiente sideOutput
     */
      val newUjson = Try{ujson.read(data)}
    newUjson match {
        case Failure(ex: Exception) =>
          val msg = s"Error. Unable to parse json event: ${ex.getMessage}."  + "["+data+"]"
          logger.error(msg, ex)
          ctx.output(sideOutputsMap("json-error"), msg)

        case Success(ujson: Value) =>
          Try{
            //var auxCfg = ujson
            pathsToObfuscate.foreach{path =>
              val listPath = path.split('.').toList
              obfuscate(ujson, listPath, 0)
            }
            ujson
          } match {
            case Failure(ex) =>
              val msg = s"Error. Unable to obfuscate event: ${ex.getMessage}."  + "["+data+"]"
              logger.error(msg, ex)
              ctx.output(sideOutputsMap("json-error"), msg)

            case Success(ujson: Value) =>
              val jsonCfg: String = ujson.toString()
              ctx.output(sideOutputsMap("json-obfuscated"), jsonCfg)

          }
      }
    }

  def obfuscate(json: Value, path: List[String], keyIndex: Int): Unit = {
    val key: String = path(keyIndex)
    Try(json(key)) match {
      case Failure(ex) => throw new Exception(ex)
      case Success(_) => if (key == path.last) json(key) = "X" else obfuscate(json(key), path, keyIndex+1 )
    }
  }

}


