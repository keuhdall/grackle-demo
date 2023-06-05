package repositories

import cats.effect.kernel.MonadCancelThrow
import cats.implicits.*
import doobie.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import graphql.mappings.Country

trait CountryRepository[F[_]] {
  def createCountry(
      name: String,
      continent: String,
      bestFood: Option[String],
      hasEiffelTower: Boolean
  ): F[Unit]
  def updateCountry(country: Country): F[Unit]
  def deleteCountry(countryId: Long): F[Unit]
}

object CountryRepository {
  def fromTransactor[F[_]: MonadCancelThrow](xa: Transactor[F]): CountryRepository[F] =
    new CountryRepository[F] {
      private val insertFr: Fragment =
        fr"INSERT INTO countries (name, continent, best_food, has_eiffel_tower)"
      private val updateFr: Country => Fragment =
        ctry => fr"""
         UPDATE countries SET (
            name = ${ctry.name},
            continent = ${ctry.continent},
            best_food = ${ctry.bestFood},
            has_eiffel_tower = ${ctry.hasEiffelTower})
        """
      private val deleteFr: Fragment =
        fr"DELETE FROM countries"

      override def createCountry(
          name: String,
          continent: String,
          bestFood: Option[String],
          hasEiffelTower: Boolean
      ): F[Unit] =
        (insertFr ++ sql"VALUES ($name, $continent, $bestFood, $hasEiffelTower)").update
          .withUniqueGeneratedKeys[Long]("id")
          .transact(xa)
          .void

      override def updateCountry(country: Country): F[Unit] =
        (updateFr(country) ++ sql"WHERE id = ${country.id}").update.run
          .transact(xa)
          .void

      override def deleteCountry(countryId: Long): F[Unit] =
        (deleteFr ++ sql"WHERE id = $countryId").update.run
          .transact(xa)
          .void
    }
}
