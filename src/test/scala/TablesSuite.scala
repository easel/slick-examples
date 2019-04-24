import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import slick.basic.DatabaseConfig
import slick.jdbc.meta._

import com.theseventhsense.utils.slick.postgres.{APIWithEffectRouting, CustomPostgresDriver, Databases}

class TablesSuite extends WordSpec with BeforeAndAfterAll with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  var dbs: Databases[CustomPostgresDriver] = new Databases(
    default = DatabaseConfig.forConfig("default"),
    readOnly = DatabaseConfig.forConfig("read-only")
  )
  val api = new APIWithEffectRouting(dbs)

  import api._

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  override def beforeAll: Unit =
    dbs.run((coffees.schema ++ suppliers.schema).dropIfExists).futureValue

  def createSchema() =
    dbs.run((coffees.schema ++ suppliers.schema).createIfNotExists).futureValue


  def insertSupplier(): Int =
    dbs.run(suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199")).futureValue

  "the slick databases" should {
    "Creating a schema works" in {
      createSchema()

      val tables = dbs.run(MTable.getTables).futureValue

      assert(tables.size == 324)
      assert(tables.count(_.name.name.equalsIgnoreCase("suppliers")) == 1)
      assert(tables.count(_.name.name.equalsIgnoreCase("coffees")) == 1)
    }

    "Inserting a Supplier works" in {
      val insertCount = insertSupplier()
      assert(insertCount == 1)
    }

    "Query Suppliers works" in {
      val results = dbs.run(suppliers.result).futureValue
      assert(results.size == 1)
      assert(results.head._1 == 101)
    }
  }

  override def afterAll {
    dbs.close()
  }
}
