package obfuscate

import java.util.{Properties, UUID}

import akka.Done
import com.dimafeng.testcontainers.{ForAllTestContainer, KafkaContainer}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.{FlatSpec, Matchers}
import org.testcontainers.containers.Network
import transformer.ObfuscateDataStream
import utils.KafkaUtils

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}


class ObfuscateTest extends FlatSpec with Matchers with ForAllTestContainer{

  val network = Network.newNetwork()

  override val container = KafkaContainer().configure { c =>
    c.withNetwork(network).withNetworkAliases("kafka")
    c.withEmbeddedZookeeper()
  }

  def kafkaContainer = container.container

  kafkaContainer.start()


  val producer: KafkaProducer[String, String] = {

    val configProperties = new Properties()
    configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers)
    configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    configProperties.put("enable.auto.commit","true")
    configProperties.put("auto.offset.reset","earliest")

    new KafkaProducer[String, String](configProperties)
  }

  val callback: Callback = new Callback {
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
      Option(exception) match {
        case Some(ex) => //logger.error("error at producing message", ex)
        case None     => Done
      }
  }

  def consumerProperties(consumerId: String): java.util.Map[String, Object] = {
    Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> kafkaContainer.getBootstrapServers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      "group.id" -> consumerId
    ).asJava
  }

  def consumer(consumerGroupId: String): KafkaConsumer[String, String] = {
    new KafkaConsumer[String, String](consumerProperties(consumerGroupId))
  }

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
  Thread.sleep(2000)
  producer.send(record, callback)
  Thread.sleep(2000)
  producer.send(record, callback)
  Thread.sleep(2000)
  producer.send(invalidRecord, callback)
  Thread.sleep(2000)
  producer.send(invalidRecord, callback)
  Thread.sleep(2000)
  producer.send(invalidRecord2, callback)
  Thread.sleep(2000)


  "json and invalidJson" should "be posted in kafkaProducerObfuscatedTopic and kafkaProducerErrorsTopic" in {

    val ansValid = consValid.poll(60000).asScala
    Thread.sleep(5000)
    ansValid.size shouldBe 3
    ansValid.foreach { x =>
      val jsonObj = ujson.read(x.value())
      jsonObj("order")("user")("name").value shouldBe "X".toString
      jsonObj("order")("user")("lastname").value shouldBe "X".toString
      println("\n valid record: " + x.value() + "\n")
    }

    val ansInvalid = consInvalid.poll(60000).asScala
    Thread.sleep(5000)
    ansInvalid.foreach { x => println("\n invalid record: " + x.value() + "\n") }

    ansInvalid.size shouldBe 3
  }


}

