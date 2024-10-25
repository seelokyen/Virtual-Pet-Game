package sly.app.virtualpetgame.view

import scalafx.beans.value.ObservableValue
import scalafx.scene.control.{TableColumn, TableView}
import sly.app.virtualpetgame.MainApp
import scalafxml.core.macros.sfxml
import sly.app.virtualpetgame.util.Pet

@sfxml
class LeaderboardController(private val leaderboardTable: TableView[Pet],
                            private val usernameCol: TableColumn[Pet, String],
                            private val petBreedCol: TableColumn[Pet, String],
                            private val petNameCol: TableColumn[Pet, String],
                            private val levelCol: TableColumn[Pet, Int]) {

  // Populate the leaderboard table with pet data
  // MainApp.petData is expected to be an ObservableBuffer[Pet] that contains
  // all pets' information, fetched from the Pet database
  leaderboardTable.items = MainApp.petData

  // Set up cell value factories for each column
  usernameCol.cellValueFactory = {_.value.username}
  petBreedCol.cellValueFactory = {_.value.petBreed}
  petNameCol.cellValueFactory = {_.value.petName}
  levelCol.cellValueFactory = { cellData =>
    // Explicitly cast to ObservableValue[Int, Int] for type safety
    cellData.value.level.asInstanceOf[ObservableValue[Int, Int]]
  }
}
