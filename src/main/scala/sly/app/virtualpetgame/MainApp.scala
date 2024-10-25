package sly.app.virtualpetgame
import scalafx.application.{JFXApp, Platform}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import javafx.{scene => jfxs}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.stage.{Modality, Stage, WindowEvent}
import sly.app.virtualpetgame.util.{Database, Pet, PetAttributeUtil, SoundManager}

object MainApp extends JFXApp {

  Database.setupDB()

  val petData = new ObservableBuffer[Pet]()
  petData ++= Pet.getTop5Players

  val rootResource = getClass.getResource("view/RootLayout.fxml")
  val loader = new FXMLLoader(rootResource, NoDependencyResolver)
  loader.load()
  val roots = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    title = "Virtual Pet Game"
    scene = new Scene {
      root = roots
    }
    onCloseRequest = (event: WindowEvent) => {
      PetAttributeUtil.stopDecrementingAttributes()
      SoundManager.stopMusic()
      Platform.exit()
    }
  }

  SoundManager.startMusic()

  def showWelcome() = {
    val resource = getClass.getResource("view/Welcome.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    this.roots.setCenter(roots)
  }

  def showChooseBreed() = {
    val resource = getClass.getResource("view/ChooseBreed.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    this.roots.setCenter(roots)
  }

  def showGameInterior(): Unit = {
    val resource = getClass.getResource("view/GameInterior.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    this.roots.setCenter(roots)
  }

  def showLeaderboardDialog(): Unit = {
    val resource = getClass.getResource("view/Leaderboard.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()

    val roots = loader.getRoot[jfxs.layout.AnchorPane]

    val dialogStage = new Stage {
      title = "Leaderboard"
      initModality(Modality.ApplicationModal)
      initOwner(stage)
      scene = new Scene {
        root = roots
      }
    }

    dialogStage.showAndWait()

  }

  showWelcome()
}