package obfuscate

import java.util.{Properties, UUID}

import io.redbee.recommender.events.{EventHelper}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.{FlatSpec, Matchers}
import transformer.{ObfuscateDataStream}
import utils.KafkaUtils
import ujson.Js
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


class ObfuscateTest extends FlatSpec with Matchers with EventHelper {
  implicit val ec = ExecutionContext.global
  val props: Properties = KafkaUtils.getProperties()

  props.setProperty("bootstrap.servers", kafkaContainer.getBootstrapServers)
  props.setProperty("group.id", "group_id_prueba")
  props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "obf_groupid")
  props.setProperty("kafka.broker", s"localhost:${kafkaContainer.getMappedPort(9092)}")


  val json =
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

  val invalidJson =
    """{
      | "order" :{
      |   "user":{
      |      "name": "Nombre",
      |      "XXXX": "Apellido"
      |   },
      |   "products": [
      |     "item1", "item2", "item3"
      |   ]
      | }
      |}""".stripMargin

  val invalidJson2 =
    """{
      | "order" :{
      |   "CCCCC":{
      |      "name": "Nombre",
      |      "lastname": "Apellido"
      |   },
      |   "products": [
      |     "item1", "item2", "item3"
      |   ]
      | }
      |}""".stripMargin

  val topic = props.getProperty("kafkaConsumerTopic")
  val record = new ProducerRecord[String, String](topic,"11111", json)
  val invalidRecord = new ProducerRecord[String, String](topic,"11111", invalidJson)
  val invalidRecord2 = new ProducerRecord[String, String](topic,"11111", invalidJson2)

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.enableCheckpointing(100)
  Future(ObfuscateDataStream.startStream(props, env))
  Thread.sleep(5000)

  val consValid = consumer(UUID.randomUUID().toString)
  consValid.subscribe(List(props.getProperty("kafkaProducerObfuscatedTopic") ).asJavaCollection)
  consValid.poll(1000).asScala.size shouldBe 0

  val consInvalid = consumer(UUID.randomUUID().toString)
  consInvalid.subscribe(List(props.getProperty("kafkaProducerErrorsTopic") ).asJavaCollection)
  consInvalid.poll(1000).asScala.size shouldBe 0

  producer.send(record, callback)
  producer.send(record, callback)
  producer.send(record, callback)
  producer.send(invalidRecord, callback)
  producer.send(invalidRecord, callback)
  producer.send(invalidRecord2, callback)

  Thread.sleep(10000)

  "json and invalidJson" should "be posted in kafkaProducerObfuscatedTopic and kafkaProducerErrorsTopic" in {

    val ansValid = consValid.poll(10000).asScala
    Thread.sleep(1000)
    ansValid.size shouldBe 3
    ansValid.foreach { x =>
      val jsonObj = ujson.read(x.value())
      jsonObj("order")("user")("name").value shouldBe "X".toString
      jsonObj("order")("user")("lastname").value shouldBe "X".toString
      println("\n valid record: " + x.value() + "\n")
    }

    val ansInvalid = consInvalid.poll(10000).asScala
    Thread.sleep(1000)
    ansInvalid.foreach { x => println("\n invalid record: " + x.value() + "\n") }

    ansInvalid.size shouldBe 3
  }


}

