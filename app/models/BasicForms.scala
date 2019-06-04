package models

import play.api.data.Form
import play.api.data.Forms._

case class BasicForm(name: String, value: String)

object BasicForm {
  val form: Form[BasicForm] = Form(
    mapping(
      "name" -> text,
      "value" -> text
    )(BasicForm.apply)(BasicForm.unapply)
  )
}


