package sly.app.virtualpetgame.model

import sly.app.virtualpetgame.util.Database
import sly.app.virtualpetgame.util.Pet

import scala.util.{Failure, Success}

abstract class Dog(val name: String, var hunger: Int, var happiness: Int, var cleanliness: Int) extends Database {

  private var feedCount = 0
  private var showerCount = 0
  private var playCount = 0
  private var level = 1
  var previousLevel: Int = 1

  def breed: String

  // Decrease attributes every time this method is called
  def decreaseAttributes(): Unit = {
    hunger = Math.max(0, (hunger - 10).toInt)
    happiness = Math.max(0, (happiness - 10).toInt)
    cleanliness = Math.max(0, (cleanliness - 10).toInt)

    println(s"$name's current state: Hunger = $hunger, Happiness = $happiness, Cleanliness = $cleanliness")
  }

  // Check pet state and notify if any attribute is low
  def checkPetState(): Unit = {
    if (hunger < 20) {
      println(s"$name is hungry!")
    }

    if (happiness < 20) {
      println(s"$name wants to play!")
    }

    if (cleanliness < 20) {
      println(s"$name needs a shower!")
    }
  }

  // Feed the pet and increase its hunger attribute
  def feed(): Unit = {
    hunger = Math.min(100, hunger + 20)
    println(s"$name is fed. Hunger: $hunger")
    feedCount += 1
    checkLevelUp()
  }

  // Play with the pet and increase its happiness attribute
  def play(): Unit = {
    happiness = Math.min(100, happiness + 20)
    println(s"$name is played with. Happiness: $happiness")
    playCount += 1
    checkLevelUp()
  }

  // Shower the pet and increase its cleanliness attribute
  def shower(): Unit = {
    cleanliness = Math.min(100, cleanliness + 20)
    println(s"$name is showered. Cleanliness: $cleanliness")
    showerCount += 1
    checkLevelUp()
  }

  def isFull: Boolean = hunger >= 100
  def isHappy: Boolean = happiness >= 100
  def isClean: Boolean = cleanliness >= 100

  // Check if the pet can level up
  private def checkLevelUp(): Unit = {
    // Level-up logic:
    // 1. Calculate the current level threshold based on the pet's level
    // 2. The threshold starts at 5 and increases by 2 * level for each level
    // 3. Check if all activity counts (feed, shower, play) meet or exceed the threshold
    // 4. If so, increase the level and update the database
    val minimumThreshold = 5
    val increasePerLevel = 2 * (level)
    val currentLevelThreshold = minimumThreshold + (level - 1) * increasePerLevel
    if (feedCount >= currentLevelThreshold && showerCount >= currentLevelThreshold && playCount >= currentLevelThreshold) {
      level += 1
      updateDatabase()
    }
  }

  // Method to update the pet's data in the database
  private def updateDatabase(): Unit = {
    val pet = new Pet(GameState.username, breed, name, level)
    pet.save() match {
      case Success(_) =>
        println(s"$name's data has been saved to the database.")
      case Failure(e) =>
        println(s"Failure to save $name's data: ${e.getMessage}")
    }
  }

  // Getter for the level attribute
  def getLevel(): Int = level
}
