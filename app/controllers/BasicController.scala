package controllers

import controllers.Helpers._
import javax.inject.{Inject, Singleton}
import models.BasicForm
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class BasicController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {


  val settings: MongoClientSettings = MongoClientSettings.builder()
    .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
    .build()

  val mongoClient: MongoClient = MongoClient(settings)

  val database: MongoDatabase = mongoClient.getDatabase("mydb")

  val collection: MongoCollection[Document] = database.getCollection("ken")

  val myData: Future[Document] = collection.find(equal("_id", "myData")).first().head()
  val myData2: Future[Document] = collection.find(equal("_id", "myData2")).first().head()


  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def simpleForm() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.basicForm(BasicForm.form))
  }

  def databaseInteractions() = Action { implicit request =>
    BasicForm.form.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(views.html.basicForm(formWithErrors))
      },
      formData => {
        request.body.asFormUrlEncoded.get("Action").head match {
          case "Add" => addToDataArray(formData.name)
          case "Read" => getByYear(formData.name)
          case "Create" => createData()
          case "Create2" => createData2()
          case "Delete" => deleteData(formData.name)
          case "Update" => updateData(formData.name, formData.value )
          case _ => createData()
        }

        Ok(views.html.basicForm(BasicForm.form))
      }
    )
  }


  def addToDataArray(formData: String): Unit = {
    val writes: List[WriteModel[_ <: Document]] = List(
      UpdateOneModel(equal("_id", "myData"), Updates.addToSet("psub", s"$formData"))
    )

    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")
  }

  def updateData(formData: String,formDataUpdate:String): Future[Unit] = {

    val collectionAsList: Future[List[String]] = myData.map(_.get[BsonArray]("psub").get.getValues.asScala.map(_.asString().getValue).toList)

    collectionAsList.map {
      result: List[String] =>
        val getIndex: Int = result.indexOf(formData)
        collection.updateOne(equal(s"psub.$getIndex", s"$formData"), set(s"psub.$getIndex", s"$formDataUpdate")).printHeadResult("Update Result: ")
    }.recover {
      case e =>
        print(s"Got this error: $e")
    }
  }

  def getById(formData:String) = {

    print(s"\n\n\n ${collection.find(equal("_id", formData)).first().printHeadResult()} \n\n\n")

  }

  def getByAmount(formData:String) = {

    print(s"\n\n\n ${collection.find(equal("amount", formData)).first().printHeadResult()} \n\n\n")

  }

  def getByYear(formData:String) = {

    print(s"\n\n\n ${collection.find(equal("year", formData)).first().printHeadResult()} \n\n\n")

  }


  def deleteData(formData: String): Unit = {
    val writes: List[WriteModel[_ <: Document]] = List(
      UpdateOneModel(and(equal("_id", "myData")), Updates.pull("psub", s"$formData"))
    )

    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")
  }


  def createData(): Unit = {
    val writes: List[WriteModel[_ <: Document]] = List(
      InsertOneModel(Document(
        "_id" -> "myData",
        "psub" -> Seq(
          "123",
          "456",
          "789",
          "000",
          "matTest"
        )
      ))
    )

    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")
  }

  def createData2() = {
    val writes: List[WriteModel[_ <: Document]] = List(
      InsertOneModel(Document(
        "_id" -> "psub1",
        "amount" -> "20",
        "year" -> "2019"
      )),
      InsertOneModel(Document(
        "_id" -> "psub2",
        "amount" -> "20",
        "year" -> "2018"
      )),
      InsertOneModel(Document(
        "_id" -> "psub3",
        "amount" -> "20",
        "year" -> "2017"
      ))
    )

    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")

  }
}