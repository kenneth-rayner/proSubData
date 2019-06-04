package controllers

import controllers.Helpers._
import javax.inject.{Inject, Singleton}
import org.mongodb.scala.model._
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, ServerAddress}
import play.api.mvc._

import scala.collection.JavaConverters._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def onPageLoad() = Action {
    implicit request: Request[AnyContent] =>

      val settings: MongoClientSettings = MongoClientSettings.builder()
        .applyToClusterSettings(b => b.hosts(List(new ServerAddress("localhost")).asJava))
        .build()

      val mongoClient: MongoClient = MongoClient(settings)

      val database: MongoDatabase = mongoClient.getDatabase("mydb")

      val collection: MongoCollection[Document] = database.getCollection("ken")

      val writes: List[WriteModel[_ <: Document]] = List(

        DeleteOneModel(Document("_id" -> 2))

      )

      collection.bulkWrite(writes).printHeadResult("Bulk write results: ")

      Ok(views.html.index())
  }

  def onSubmit() = Action {
    Ok(views.html.index())
  }
}
