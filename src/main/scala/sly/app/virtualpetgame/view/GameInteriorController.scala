package sly.app.virtualpetgame.view

import scalafx.application.Platform
import scalafx.scene.control.{Alert, Label, ProgressBar}
import scalafx.scene.image.{Image, ImageView}
import scalafxml.core.macros.sfxml
import scalafx.stage.{Stage, WindowEvent}
import scalafx.scene.control.Alert.AlertType
import sly.app.virtualpetgame.model.{Beagle, Dog, GameState, Husky}
import sly.app.virtualpetgame.util.PetAttributeUtil
import scalafx.animation.PauseTransition
import scalafx.util.Duration
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scalikejdbc._

@sfxml
class GameInteriorController (
                              private val petImageView: ImageView,
                              private val foodButton: ImageView,
                              private val ballButton: ImageView,
                              private val shampooButton: ImageView,
                              private val hungerLabel: Label,
                              private val hungerBar: ProgressBar,
                              private val happinessLabel: Label,
                              private val happinessBar: ProgressBar,
                              private val cleanlinessLabel: Label,
                              private val cleanlinessBar: ProgressBar,
                              private val levelLabel: Label
                            ) {
  private var myPet: Dog = _

  // These image variables store different states of the pet
  // They are used to update the pet's appearance based on its current action
  private var mainImage: Image = _
  private var eatingImage: Image = _
  private var playingImage: Image = _
  private var showeringImage: Image = _

  var dialogStage: Stage = _
  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
  private var zeroStateStartTime: Long = _

  def initialize(): Unit = {
    val breed = GameState.selectedBreed
    val username = GameState.username
    val petName = GameState.petName
    loadPetFromDatabase(username, breed, petName)
    setBreed(breed)
    scheduleGamerOverCheck()
  }

  private def loadPetFromDatabase(username: String, breed: String, petName: String): Unit = {
    // Define a method to fetch pet data from the database
    val petData = DB readOnly { implicit session =>
      sql"""
        SELECT * FROM Pet WHERE username = ${username} AND petName = ${petName}
      """.map(rs => (
        rs.string("petBreed"),
        rs.int("level")
      )).single.apply()
    }
    petData match {
      case Some((petBreed, level)) =>
        println(s"Loaded pet: ${petName} of breed ${petBreed} at level ${level}")
        myPet = breed match {
          case "Beagle" => new Beagle(petName, 50, 50, 50)
          case "Husky" => new Husky(petName, 50, 50, 50)
        }
        myPet.previousLevel = level
        levelLabel.text = s"Level ${level}"
      case None =>
        println(s"No pet found for $username with name $petName.")
        myPet = breed match {
          case "Beagle" => new Beagle(petName, 50, 50, 50)
          case "Husky" => new Husky(petName, 50, 50, 50)
        }
    }
  }

  def setBreed(breed: String): Unit = {
    myPet = breed match {
      case "Beagle" =>
        setBreedImages("beagle")
        petImageView.image = mainImage
        new Beagle(GameState.petName, 50, 50, 50)

      case "Husky" =>
        setBreedImages("husky")
        petImageView.image = mainImage
        new Husky(GameState.petName, 50, 50, 50)

    }
    PetAttributeUtil.startDecrementingAttributes(myPet, updateBars)
    updateBars()
  }

  private def setBreedImages(breed: String): Unit = {
    mainImage = new Image(s"/sly/app/virtualpetgame/view/images/${breed}_main.png")
    eatingImage = new Image(s"/sly/app/virtualpetgame/view/images/${breed}_eating.png")
    playingImage = new Image(s"/sly/app/virtualpetgame/view/images/${breed}_playing.png")
    showeringImage = new Image(s"/sly/app/virtualpetgame/view/images/${breed}_showering.png")
  }

  private def setImageDuration(newImage: Image, duration: Duration): Unit = {
    petImageView.image = newImage

    // Create a PauseTransition to wait for the specified duration
    val pause = new PauseTransition(duration) {
      onFinished = _ => petImageView.image = mainImage
    }

    // Play the transition
    pause.play()
  }

  def handleFoodClick(): Unit = {
    if (myPet.isFull) {
      showWarningDialog("Your pet is already full!")
    } else {
      setImageDuration(eatingImage, Duration(3000))
      myPet.feed()
      updateBars()
      updateLevel()
    }
  }

  def handlePlayClick(): Unit = {
    if (myPet.isHappy) {
      showWarningDialog("Your pet is already happy!")
    } else {
      setImageDuration(playingImage, Duration(3000))
      myPet.play()
      updateBars()
      updateLevel()
    }
  }

  def handleShowerClick(): Unit = {
    if (myPet.isClean) {
      showWarningDialog("Your pet is already clean!")
    } else {
      setImageDuration(showeringImage, Duration(3000))
      myPet.shower()
      updateBars()
      updateLevel()
    }
  }

  def showWarningDialog(message: String): Unit = {
    new Alert(AlertType.Warning) {
      initOwner(dialogStage)
      title = "Warning"
      headerText = "Thank You But No More!"
      contentText = message
    }.showAndWait()
  }

  private def updateBars(): Unit = {
    hungerBar.progress = myPet.hunger / 100.0
    happinessBar.progress = myPet.happiness / 100.0
    cleanlinessBar.progress = myPet.cleanliness / 100.0
  }

  private def updatePetState(): Unit = {
    updateBars()
    if (myPet.hunger < 20) {
      println("I'm still hungry!")
    }

    if (myPet.happiness < 20) {
      println("I still want to play!")
    }

    if (myPet.cleanliness < 20) {
      println("I still need a shower!")
    }
  }

  def updateLevel(): Unit = {
    val newLevel = myPet.getLevel()
    levelLabel.text = "Level " + newLevel.toString

    // Call this method only if the level has changed
    if (newLevel > myPet.previousLevel) {
      showLevelUpDialog(newLevel)
      myPet.previousLevel = newLevel // Update the previous level
    }
  }

  def showLevelUpDialog(level:Int): Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = {
        new Alert(AlertType.Information) {
          initOwner(dialogStage)
          title = "Congratulations!"
          headerText = "LEVEL UP!"
          contentText = s"Your pet has reached level $level. Well Done!"
        }.showAndWait()
      }
    })
  }

  // Schedule a task to check if pet is in zero state for more than 5 minutes
  private def scheduleGamerOverCheck(): Unit = {
    scheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        println(s"Scheduler check at ${System.currentTimeMillis()}")

        if (myPet.hunger <= 0 || myPet.happiness <= 0 || myPet.cleanliness <= 0) {
          if (zeroStateStartTime == 0) {
            zeroStateStartTime = System.currentTimeMillis()
            println("Zero state detected, starting timer.")
          } else {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - zeroStateStartTime
            println(s"Zero state time elapsed: $elapsedTime ms")

            if (elapsedTime >= 300000) { // 5 minutes
              println("Game Over: Pet's attribute has been 0 for 5 minutes.")
              showGameOverDialog()
              scheduler.shutdown()
            }
          }
        } else {
          zeroStateStartTime = 0
        }
      }
    }, 0, 1, TimeUnit.MINUTES)
  }

  def showGameOverDialog(): Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = {
        val alert = new Alert(AlertType.Error) {
          initOwner(dialogStage)
          title = "Game Over"
          headerText = "YOU LOST!"
          contentText = "Your pet was left hungry, sad, or dirty for too long. :("
        }
        alert.showAndWait() match {
          case Some(_) =>
            PetAttributeUtil.stopDecrementingAttributes()
            scheduler.shutdown()
            dialogStage.close()
            Platform.exit()
          case None =>
        }
      }
    })
  }
  initialize()
}
