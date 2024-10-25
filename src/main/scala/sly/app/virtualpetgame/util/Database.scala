package sly.app.virtualpetgame.util

import scalafx.beans.property.{IntegerProperty, StringProperty}
import scalikejdbc._

import scala.util.{Failure, Success, Try}

// Trait to set up the database connection and session management
trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:petDB;create=true;"

  // Initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "user", "password")

  // Ad-hoc session provider on the REPL
  implicit val session = AutoSession
}

// Companion object to handle database initialization
object Database extends Database {
  def setupDB(): Unit = {
    if (!hasDBInitialize) {
      Pet.initializeTable()
    }
  }

  def hasDBInitialize: Boolean = {
    DB getTable "Pet" match {
      case Some(_) => true
      case None => false
    }
  }
}

// Pet model to handle database operations
class Pet(val usernameS: String, val petBreedS: String, val petNameS: String, val levelS: Int) extends Database {

  var username = StringProperty(usernameS)
  var petBreed = StringProperty(petBreedS)
  var petName = StringProperty(petNameS)
  var level = IntegerProperty(levelS)

  def save(): Try[Int] = {
    // The isExist check is necessary to determine whether to perform an INSERT or UPDATE operation
    // This prevents duplicate entries and ensures proper database management
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
          INSERT INTO Pet (username, petBreed, petName, level) VALUES
          (${username.value}, ${petBreed.value}, ${petName.value}, ${level.value})
        """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
          UPDATE Pet SET
          petBreed = ${petBreed.value},
          petName = ${petName.value},
          level = ${level.value}
          WHERE username = ${username.value}
        """.update.apply()
      })
    }
  }
  // The isExist method checks if a pet with the given username and petName already exists in the database
  def isExist: Boolean = {
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM Pet WHERE
        username = ${username.value} AND petName = ${petName.value}
      """.map(rs => rs.string("username")).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }
  }
}

// Companion object to handle Pet operations
object Pet extends Database {
  def initializeTable(): Unit = {
    println("Initializing Pet Table...")
    try {
      DB autoCommit { implicit session =>
        sql"""
          CREATE TABLE Pet (
            id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            username VARCHAR(64),
            petBreed VARCHAR(64),
            petName VARCHAR(64),
            level INT
          )
        """.execute.apply()
        println("Pet table created successfully.")
      }
    } catch {
    case e: Exception =>
      println(s"Error initializing table: ${e.getMessage}")
      e.printStackTrace()
  }
}

  def getTop5Players(): List[Pet] = {
    DB readOnly { implicit session =>
      sql"""
      SELECT * FROM Pet ORDER BY level DESC FETCH FIRST 5 ROWS ONLY
    """.map(rs => new Pet(
        rs.string("username"),
        rs.string("petBreed"),
        rs.string("petName"),
        rs.int("level")
      )).list.apply()
    }
  }
}