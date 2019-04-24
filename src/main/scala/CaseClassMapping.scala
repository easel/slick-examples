import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import slick.basic.DatabaseConfig

import com.theseventhsense.utils.slick.postgres.{APIWithEffectRouting, CustomPostgresDriver, Databases}

object CaseClassMapping extends App {
  val dbs: Databases[CustomPostgresDriver] = new Databases(
  default = DatabaseConfig.forConfig("default"),
  readOnly = DatabaseConfig.forConfig("read-only")
  )
  val api = new APIWithEffectRouting(dbs)
  import api._

  case class User(name: String, id: Option[Int] = None)

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    // Auto Increment the id primary key column
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    // The name can't be null
    def name = column[String]("NAME")
    // the * projection (e.g. select * ...) auto-transforms the tupled
    // column values to / from a User
    def * = (name, id.?) <> (User.tupled, User.unapply)
  }

  // the base query for the Users table
  val users = TableQuery[Users]

  try {
    Await.result(dbs.run(DBIO.seq(
      // create the schema
      users.schema.create,

      // insert two User instances
      users += User("John Doe"),
      users += User("Fred Smith"),

      // print the users (select * from USERS)
      users.result.map(println)
    )), Duration.Inf)
  } finally dbs.close
}


