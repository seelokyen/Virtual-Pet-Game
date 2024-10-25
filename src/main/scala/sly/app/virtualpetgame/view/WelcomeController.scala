package sly.app.virtualpetgame.view

import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import sly.app.virtualpetgame.MainApp
import scalafxml.core.macros.sfxml

@sfxml
class WelcomeController() {
  // Navigates to the pet breed selection screen
  // This method is expected to change the current view to allow the user to choose their pet's breed
  def getStart(): Unit = {
    MainApp.showChooseBreed()
  }

  // Displays a dialog box with game instructions
  def getInstruction(): Unit = {
    val instructions =
      """|1. Choose your pet: Pick either a Beagle or a Husky.
         |2. Take care of your pet:
         |   - Feed it when it’s hungry.
         |   - Play with it when it’s sad.
         |   - Shower it when it’s dirty.
         |3. Keep your pet happy, clean, and full to help it grow and level up!
         |4. Your pet's hunger, sadness, and dirtiness will increase every minute.
         |5. If you leave it hungry, sad, or dirty for more than 5 minutes, you’ll lose the game.
         |""".stripMargin

    val alert = new Alert(AlertType.Information) {
      initOwner(MainApp.stage)
      title = "Game Instructions"
      headerText = "How to Play"
      contentText = instructions
    }

    alert.showAndWait()
  }

  // Navigates to the leaderboard screen
  // This method is expected to display a dialog to show the game's leaderboard
  def getLeaderboard(): Unit = {
    MainApp.showLeaderboardDialog()
  }
}