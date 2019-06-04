package models

import play.api.data.Form
import play.api.data.Forms._

case class BasicForm(id: String, amount: String, year: String)

object BasicForm {
  val form: Form[BasicForm] = Form(
    mapping(
      "id" -> text,
      "amount" -> text,
      "year" -> text

    )(BasicForm.apply)(BasicForm.unapply)
  )
}


