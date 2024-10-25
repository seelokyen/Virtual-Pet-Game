package sly.app.virtualpetgame.view

import sly.app.virtualpetgame.MainApp
import sly.app.virtualpetgame.model.GameState
import sly.app.virtualpetgame.util.Pet
import scala.util.{Success, Failure}
import scalafxml.core.macros.sfxml
import scalafx.scene.control.{Alert, TextField}
import scalafx.scene.control.Alert.AlertType

@sfxml
class ChooseBreedController(
                           private val usernameField: TextField,
                           private val petNameField: TextField
                           ) {

  private def handleBreedChoice(breed:String): Unit = {
    if (validateFields()) {
      setGameState(breed)
      savePetToDatabase(breed)
      MainApp.showGameInterior()
    }
  }

  // Public methods now call the refactored handleBreedChoice method
  def handleBeagleChoice(): Unit = handleBreedChoice("Beagle")
  def handleHuskyChoice(): Unit = handleBreedChoice("Husky")

  private def validateFields(): Boolean = {
    if (usernameField.text.value.isEmpty || petNameField.text.value.isEmpty) {
      new Alert(AlertType.Warning) {
        title = "Missing Information"
        headerText = "Please fill in all fields"
        contentText = "Both username and pet name are required."
      }.showAndWait()
      false
    } else {
      true
    }
  }
  private def setGameState(breed: String): Unit = {
    GameState.selectedBreed = breed
    GameState.username = usernameField.text.value
    GameState.petName = petNameField.text.value
  }

  private def savePetToDatabase(breed: String): Unit = {
    val pet = new Pet(GameState.username, breed, GameState.petName, 1)
    pet.save() match {
      case Success(_) => println(s"Pet data for ${GameState.petName} has been saved.")
      case Failure(e) => println(s"Failed to save pet data: ${e.getMessage}")
    }
  }
}