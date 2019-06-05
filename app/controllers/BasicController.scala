package controllers

import controllers.Helpers._
import javax.inject.{Inject, Singleton}
import models.BasicForm
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import play.api.mvc._
import scala.collection.JavaConverters._
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
          case "Create" => createData()
          case "Read" => getByField(formData.id, formData.amount)
          case "Update" => updateData(formData.id, formData.amount, formData.year)
          case "Delete" => deleteFromRepo(formData.id)
          case "Add" => addToRepo(formData.id, formData.amount, formData.year)
          case _ => createData()
        }
        Ok(views.html.basicForm(BasicForm.form))
      }
    )
  }

  //CREATE
  def createData() = {
    val writes: List[WriteModel[_ <: Document]] = List(
      InsertOneModel(Document(
        "_id" -> "Architects",
        "amount" -> "250",
        "year" -> "2019"
      )),
      InsertOneModel(Document(
        "_id" -> "Builders",
        "amount" -> "450",
        "year" -> "2018"
      )),
      InsertOneModel(Document(
        "_id" -> "Computing",
        "amount" -> "650",
        "year" -> "2017"
      )),
      InsertOneModel(Document(
        "_id" -> "Dentistry",
        "amount" -> "650",
        "year" -> "2017"
      )),
      InsertOneModel(Document(
        "_id" -> "Editing",
        "amount" -> "650",
        "year" -> "2017"
      )),
      InsertOneModel(Document(
        "_id" -> "Forestry",
        "amount" -> "650",
        "year" -> "2017"
      ))
    )
    collection.bulkWrite(writes).printHeadResult("Bulk write results: ")
  }

  //READ
  def getByField(id: String, name: String) = {

    print(s"\n\n\n ${collection.find(equal(s"$id", s"$name")).first().printHeadResult()} \n\n\n")

  }

  //UPDATE
  def updateData(id: String, dataToUpdate: String, newData: String) = {

    collection.updateOne(equal("_id", s"$id"), set(dataToUpdate, s"$newData")).printHeadResult("Update Result: ")

  }

  //DELETE
  def deleteFromRepo(formDataId: String): Unit = {
    val doc: Document = Document("_id" -> formDataId)

    collection.deleteOne(doc).results()
  }

  ///ADD
  def addToRepo(formDataId: String, formDataAmount: String, formDataYear: String): Unit = {
    val doc: Document = Document("_id" -> formDataId, "amount" -> formDataAmount, "year" -> formDataYear)

    collection.insertOne(doc).results()
  }
}