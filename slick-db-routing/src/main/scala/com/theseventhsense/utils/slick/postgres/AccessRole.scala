package com.theseventhsense.utils.slick.postgres

import scala.annotation.implicitNotFound
import scala.concurrent.Future

import slick.basic.{BasicProfile, DatabaseConfig, DatabasePublisher}
import slick.dbio.Effect._
import slick.dbio.{DBIOAction, Effect, NoStream, Streaming}

trait AccessRole

object AccessRole {

  trait ReadWrite extends AccessRole

  trait ReadOnly extends AccessRole

  @implicitNotFound(
    "'${R}' database is not privileged to to perform effect '${E}'."
  )
  trait Has[R <: AccessRole, E <: Effect]

  type ReadWriteTransaction = Read with Write with Transactional

  implicit val readOnlyCanRead: ReadOnly Has Read = null
  implicit val readOnlyCanReadTransactionally
  : ReadOnly Has Read with Transactional = null

  implicit val readWriteCanSchema: ReadWrite Has Schema = null
  implicit val readWriteCanWrite: ReadWrite Has Write = null
  implicit val readWriteCanReadWrite: ReadWrite Has Write with Read = null
  implicit val readWriteCanReadWriteSchema: ReadWrite Has Write with Read with Schema = null
  implicit val readWriteCanPerformTransactions
  : ReadWrite Has ReadWriteTransaction = null
  implicit val readWriteCanPerformAll: ReadWrite Has All = null

  class DB[P <: BasicProfile, R <: AccessRole](
                                                databaseConfiguration: DatabaseConfig[P]
                                              ) {

    private val underlyingDatabase = databaseConfiguration.db

    def run[A, E <: Effect](
                             a: DBIOAction[A, NoStream, E]
                           )(implicit p: R Has E): Future[A] =
      underlyingDatabase.run(a)

    def stream[A, E <: Effect](
                                a: DBIOAction[_, Streaming[A], E]
                              )(implicit p: R Has E): DatabasePublisher[A] = {
      underlyingDatabase.stream(a)
    }

    def close(): Unit = underlyingDatabase.close()
  }

}
