package graphql

import cats.effect.Sync
import doobie.{Meta, Transactor}
import edu.gemini.grackle.Predicate.*
import edu.gemini.grackle.Query.*
import edu.gemini.grackle.QueryCompiler.*
import edu.gemini.grackle.Value.*
import edu.gemini.grackle.doobie.postgres.{
  DoobieMapping,
  DoobieMonitor,
  LoggedDoobieMappingCompanion
}
import edu.gemini.grackle.syntax.*
import edu.gemini.grackle.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

trait Mappings[F[_]] extends DoobieMapping[F] {
  object country extends TableDef("countries") {
    val id: ColumnRef = col("id", Meta[Long])
    val name: ColumnRef = col("name", Meta[String])
    val continent: ColumnRef = col("continent", Meta[String])
    val bestFood: ColumnRef = col("best_food", Meta[String], nullable = true)
    val hasEiffelTower: ColumnRef = col("has_eiffel_tower", Meta[Boolean])
  }

  object city extends TableDef("cities") {
    val id: ColumnRef = col("id", Meta[Long])
    val name: ColumnRef = col("name", Meta[String])
    val countryId: ColumnRef = col("country_id", Meta[Long])
    val isCapital: ColumnRef = col("is_capital", Meta[Boolean])
  }

  override val schema: Schema =
    schema"""
      type Query {
        country(id: Int!): Country
        countries(continent: String): [Country]
        cities(countryId: Int): [City!]
        city(cityId: Int): City
      }

      type Country {
        id: Int!
        name: String!
        continent: String!
        bestFood: String
        hasEiffelTower: Boolean!
        cities: [City!]!
      }

      type City {
        id: ID!
        name: String!
        country: Country!
        isCapital: Boolean!
      }
    """

  val QueryType: TypeRef = schema.ref("Query")
  val CountryType: TypeRef = schema.ref("Country")
  val CityType: TypeRef = schema.ref("City")

  val typeMappings: List[ObjectMapping] =
    List(
      ObjectMapping(
        tpe = QueryType,
        fieldMappings = List(
          SqlObject("cities"),
          SqlObject("countries")
        )
      ),
      ObjectMapping(
        tpe = CityType,
        fieldMappings = List(
          SqlField("id", city.id, key = true),
          SqlField("name", city.name),
          SqlField("countryId", city.countryId, hidden = true),
          SqlField("isCapital", city.isCapital),
          SqlObject("country", Join(city.countryId, country.id))
        )
      ),
      ObjectMapping(
        tpe = CountryType,
        fieldMappings = List(
          SqlField("id", country.id, key = true),
          SqlField("name", country.name),
          SqlField("continent", country.continent),
          SqlField("bestFood", country.bestFood),
          SqlField("hasEiffelTower", country.hasEiffelTower),
          SqlObject("cities", Join(country.id, city.countryId))
        )
      )
    )

  override val selectElaborator: QueryCompiler.SelectElaborator = new SelectElaborator(
    Map(
      QueryType -> {
        case Select("country", List(Binding("id", StringValue(id))), child) =>
          Select(
            "country",
            Nil,
            Unique(Filter(Eql(CountryType / "id", Const(id)), child))
          ).success
        case Select(
              "countries",
              List(Binding("continent", StringValue(continent))),
              child
            ) =>
          Select(
            "countries",
            Nil,
            Filter(Eql(CountryType / "continent", Const(continent)), child)
          ).success
        case Select("city", List(Binding("id", StringValue(id))), child) =>
          Select(
            "city",
            Nil,
            Unique(Filter(Eql(CityType / id, Const(id)), child))
          ).success
        case Select(
              "cities",
              List(
                Binding("countryId", StringValue(countryId)),
                Binding("isCapital", BooleanValue(isCapital))
              ),
              child
            ) =>
          Select(
            "cities",
            Nil,
            Filter(
              And(
                Eql(CityType / "countryId", Const(countryId)),
                Eql(CityType / "isCapital", Const(isCapital))
              ),
              child
            )
          ).success
      }
    )
  )
}

object Mappings extends LoggedDoobieMappingCompanion {
  override def mkMapping[F[_]: Sync](
      xa: Transactor[F],
      monitor: DoobieMonitor[F]
  ): Mapping[F] = new DoobieMapping[F](xa, monitor) with Mappings[F]

  def mkMappingFromXa[F[_]: Sync: LoggerFactory](xa: Transactor[F]): Mapping[F] = {
    given Logger[F] = LoggerFactory[F].getLogger
    mkMapping(xa)
  }
}
