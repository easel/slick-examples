package com.theseventhsense.utils.slick.postgres

import scala.concurrent.Future

import com.github.tminglei.slickpg._
import slick.basic.{Capability, DatabasePublisher}
import slick.dbio
import slick.jdbc.{
  JdbcCapabilities,
  JdbcProfile,
  ResultSetConcurrency,
  ResultSetType
}

import com.theseventhsense.utils.slick.postgres.AccessRole.Has

trait LowPriorityImplicits[P <: JdbcProfile] {
  val dbs: Databases[P]

  implicit val readWriteDb: AccessRole.DB[P, AccessRole.ReadWrite] =
    dbs.ReadWrite.db
}

trait Implicits[P <: JdbcProfile] {
  val dbs: Databases[P]

  implicit val readOnlyDb: AccessRole.DB[P, AccessRole.ReadOnly] =
    dbs.ReadOnly.db
}

class APIWithEffectRouting(val dbs: Databases[CustomPostgresDriver])
    extends CustomPostgresDriver.CustomPostgresAPI
    with Implicits[CustomPostgresDriver]
    with LowPriorityImplicits[CustomPostgresDriver] {

  implicit class RichDatabases(dbs: Databases[CustomPostgresDriver]) {
    def run[A, E <: Effect, R <: AccessRole](
        a: dbio.DBIOAction[A, NoStream, E]
    )(implicit p: R Has E,
      dbWithAccess: AccessRole.DB[CustomPostgresDriver, R]): Future[A] =
      dbWithAccess.run(a)

    def stream[A, E <: Effect, R <: AccessRole](
        a: dbio.DBIOAction[_, dbio.Streaming[A], E],
        fetchSize: Int = 1000
    )(implicit p: R Has E, dbWithAccess: AccessRole.DB[CustomPostgresDriver, R])
      : DatabasePublisher[A] =
      dbWithAccess.stream(
        a.transactionally
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = fetchSize
          )
          .asInstanceOf[dbio.DBIOAction[_, dbio.Streaming[A], E]] // drop the added transactional effect
      )
  }

}

trait CustomPostgresDriver
    extends ExPostgresProfile
    with PgArraySupport
    with PgCirceJsonSupport
    with PgSearchSupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgNetSupport
    with PgLTreeSupport {
  def pgjson =
    "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  class CustomPostgresAPI
      extends API
      with DateTimeImplicits
      with CirceImplicits
      with CirceJsonPlainImplicits
      with SimpleArrayImplicits
      with SearchImplicits
      with SearchAssistants

  override val api = new CustomPostgresAPI()

  def apiWithEffectRouting(implicit dbs: Databases[CustomPostgresDriver]) =
    new APIWithEffectRouting(dbs)

}

object CustomPostgresDriver extends CustomPostgresDriver
