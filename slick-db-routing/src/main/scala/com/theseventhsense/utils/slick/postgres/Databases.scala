package com.theseventhsense.utils.slick.postgres

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class Databases[P <: JdbcProfile](
    val default: DatabaseConfig[P],
    val readOnly: DatabaseConfig[P],
) {

  def close(): Unit = {
    default.db.close
    readOnly.db.close()
  }

  sealed trait AccessRoleDatabaseConfig[R <: AccessRole] {
    def name: String

    protected def dbConfig: DatabaseConfig[P]

    def db: AccessRole.DB[P, R] = new AccessRole.DB(dbConfig)
  }

  object ReadWrite extends AccessRoleDatabaseConfig[AccessRole.ReadWrite] {
    override val name: String = "ReadWrite"
    override lazy val dbConfig: DatabaseConfig[P] = default
  }

  object ReadOnly extends AccessRoleDatabaseConfig[AccessRole.ReadOnly] {
    override val name: String = "ReadOnly"
    override lazy val dbConfig: DatabaseConfig[P] = readOnly
  }

}
