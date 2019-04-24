import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import slick.basic.DatabaseConfig
import slick.jdbc.meta._

import com.theseventhsense.utils.slick.postgres.{APIWithEffectRouting, CustomPostgresDriver, Databases}

class TablesSuite extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  var dbs: Databases[CustomPostgresDriver] = new Databases(
    default = DatabaseConfig.forConfig("default"),
    readOnly = DatabaseConfig.forConfig("read-only")
  )
  val api = new APIWithEffectRouting(dbs)

  import api._

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  before {
  }

  def createSchema() =
    dbs.run((suppliers.schema ++ coffees.schema).createIfNotExists).futureValue
    dbs.run(coffees.schema.truncate).futureValue

  def insertSupplier(): Int =
    dbs.run(suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199")).futureValue

  test("Creating the Schema works") {
    createSchema()

    val tables = dbs.run(MTable.getTables).futureValue

    assert(tables.size == 324)
    assert(tables.count(_.name.name.equalsIgnoreCase("suppliers")) == 1)
    assert(tables.count(_.name.name.equalsIgnoreCase("coffees")) == 1)
  }

  test("Inserting a Supplier works") {
    val insertCount = insertSupplier()
    assert(insertCount == 1)
  }

  test("Query Suppliers works") {
    insertSupplier()
    val results = dbs.run(suppliers.result).futureValue
    assert(results.size == 1)
    assert(results.head._1 == 101)
  }

  after {
    dbs.close()
  }
}
