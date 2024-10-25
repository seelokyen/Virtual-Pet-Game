package sly.app.virtualpetgame.util

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import sly.app.virtualpetgame.model.Dog

object PetAttributeUtil {

  // Scheduler to run the task every 1 minute
  private var scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

  def startDecrementingAttributes(dog: Dog, onAttributesDecreased: () => Unit): Unit = {
    try {
      // Schedule the task to run every 1 minute (60,000 milliseconds)
      scheduler = Executors.newScheduledThreadPool(1)
      val task = new Runnable {
        def run(): Unit = {
          try {
            dog.decreaseAttributes()
            dog.checkPetState()
            // onAttributesDecreased is a callback function that will be executed
            // after the attributes are decreased. This allows the caller to perform
            // any necessary updates (e.g., refreshing the UI) when attributes change.
            onAttributesDecreased()
          } catch {
            case e: Exception =>
              println(s"Error in attribute decrement task: ${e.getMessage}")
            // Consider logging the error or notifying the user
          }
        }
      }
      // Schedule the task to run every 1 minute (60,000 milliseconds)
      scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES)
    } catch {
      case e: Exception =>
        println(s"Failed to start the attribute decrement scheduler: ${e.getMessage}")
      // Consider logging the error or notifying the user
    }
  }
  def stopDecrementingAttributes(): Unit = {
    if (scheduler != null) {
      try {
        scheduler.shutdown()
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow()
        }
      } catch {
      case e: InterruptedException =>
        scheduler.shutdownNow()
        Thread.currentThread().interrupt()
        println("Interrupted while stopping the attribute decrement scheduler")
      case e: Exception =>
        println(s"Error while stopping the attribute decrement scheduler: ${e.getMessage}")
        // Consider logging the error or notifying the user
      } finally {
        scheduler = null
      }
    }
  }
}
