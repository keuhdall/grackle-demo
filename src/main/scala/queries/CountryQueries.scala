package queries

import doobie.ConnectionIO
import doobie.implicits.*
import doobie.util.fragment.Fragment
import types.Country

object CountryQueries {
  private val insertFr: Fragment =
    fr"INSERT INTO countries (name, continent, best_food, has_eiffel_tower)"

  private val updateFr: Country => Fragment = ctry => fr"""
         UPDATE countries SET (
            name = ${ctry.name},
            continent = ${ctry.continent},
            best_food = ${ctry.bestFood},
            has_eiffel_tower = ${ctry.hasEiffelTower})
        """
  private val deleteFr: Fragment = fr"DELETE FROM countries"

  def createCountry(
      name: String,
      continent: String,
      bestFood: Option[String],
      hasEiffelTower: Boolean
  ): ConnectionIO[Long] =
    (insertFr ++ sql"VALUES ($name, $continent, $bestFood, $hasEiffelTower)").update
      .withUniqueGeneratedKeys[Long]("id")

  def updateCountry(country: Country): ConnectionIO[Int] =
    (updateFr(country) ++ sql"WHERE id = ${country.id}").update.run

  def deleteCountry(countryId: Long): ConnectionIO[Int] =
    (deleteFr ++ sql"WHERE id = $countryId").update.run
}
