package queries

import doobie.{ConnectionIO, Fragment}
import doobie.implicits.toSqlInterpolator

object CityQueries {
  private val insertFr: Fragment = fr"INSERT INTO cities (name, country_id, is_capital)"

  def createCity(name: String, countryId: Int, isCapital: Boolean): ConnectionIO[Int] =
    (insertFr ++ sql"VALUES ($name, $countryId, $isCapital)").update
      .withUniqueGeneratedKeys[Int]("id")
}
