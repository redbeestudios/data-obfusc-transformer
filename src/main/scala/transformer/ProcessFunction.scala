package transformer

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValueFactory}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

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
      val newCfg = Try{ConfigFactory.parseString(data)}
      newCfg match {
        case Failure(ex: Exception) =>
          val msg = s"Error. Unable to parse json event: ${ex.getMessage}."  + "["+data+"]"
          logger.error(msg, ex)
          ctx.output(sideOutputsMap("json-error"), msg)

        case Success(cfg: Config) =>
          Try{
            var auxCfg = cfg
            pathsToObfuscate.foreach{path =>
              auxCfg.getAnyRef(path)
              val obfs = UUID.randomUUID().toString
              auxCfg = auxCfg.withValue(path, ConfigValueFactory.fromAnyRef(obfs))}
            auxCfg
          } match {
            case Failure(ex) =>
              val msg = s"Error. Unable to obfuscate event: ${ex.getMessage}."  + "["+data+"]"
              logger.error(msg, ex)
              ctx.output(sideOutputsMap("json-error"), msg)

            case Success(obfsCfg: Config) =>
              val jsonCfg: String = obfsCfg.root().render(ConfigRenderOptions.concise())
              ctx.output(sideOutputsMap("json-obfuscated"), jsonCfg)

          }

      }
    }

  }