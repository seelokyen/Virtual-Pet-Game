package sly.app.virtualpetgame.model

class Husky (_name: String, _hunger: Int, _happiness: Int, _cleanliness: Int) extends Dog (_name, _hunger, _happiness, _cleanliness) {
  override def breed: String = "Husky"
}