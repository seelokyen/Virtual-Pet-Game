package sly.app.virtualpetgame.util

import scalafx.scene.media.{Media, MediaPlayer}

object SoundManager {
  private var mediaPlayer: Option[MediaPlayer] = None

  def startMusic(): Unit = {
    val musicFile = getClass.getResource("/sly/app/virtualpetgame/view/audio/bg_music.mp3")
    musicFile match {
      case null => println("Background music file not found.")
      case _ =>
        val media = new Media(musicFile.toString)
        mediaPlayer = Some(new MediaPlayer(media))
        mediaPlayer.foreach { player =>
          player.setCycleCount(MediaPlayer.Indefinite) // Loop indefinitely
          player.play()
        }
    }
  }

  def stopMusic(): Unit = {
    mediaPlayer.foreach { player =>
      player.stop()
    }
  }
}