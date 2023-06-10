package graphql.mappings

import cats.effect.Sync
import doobie.{Meta, Transactor}
import doobie.util.transactor
import edu.gemini.grackle.Predicate.{Const, Eql}
import edu.gemini.grackle.Query.{Binding, Filter, Select, Unique}
import edu.gemini.grackle.QueryCompiler.SelectElaborator
import edu.gemini.grackle.Value.StringValue
import edu.gemini.grackle.{Mapping, QueryCompiler, Schema, TypeRef}
import edu.gemini.grackle.doobie.postgres.{
  DoobieMapping,
  DoobieMonitor,
  LoggedDoobieMappingCompanion
}
import edu.gemini.grackle.syntax.*
import org.typelevel.log4cats.{Logger, LoggerFactory}

case class Country(
    id: Long,
    name: String,
    continent: String,
    bestFood: Option[String],
    hasEiffelTower: Boolean
)

trait CountryMapping[F[_]] extends DoobieMapping[F] {
  object country extends TableDef("countries") {
    val id: ColumnRef = col("id", Meta[Long])
    val name: ColumnRef = col("name", Meta[String])
    val continent: ColumnRef = col("continent", Meta[String])
    val bestFood: ColumnRef = col("best_food", Meta[String], nullable = true)
    val hasEiffelTower: ColumnRef = col("has_eiffel_tower", Meta[Boolean])
  }

  val schema =
    schema"""
      type Query {
        country(id: Long!): Country
        countries(continent: String): [Country]
      }

      type Country {
        id: Long!
        name: String!
        continent: String!
        bestFood: String
        hasEiffelTower: Boolean!
      }
    """

  val QueryType: TypeRef = schema.ref("Query")
  val CountryType: TypeRef = schema.ref("Country")

  val typeMappings: List[ObjectMapping] =
    List(
      ObjectMapping(
        tpe = QueryType,
        fieldMappings = List(
          SqlObject("countries")
        )
      ),
      ObjectMapping(
        tpe = CountryType,
        fieldMappings = List(
          SqlField("id", country.id, key = true),
          SqlField("name", country.name),
          SqlField("continent", country.continent),
          SqlField("bestFood", country.bestFood),
          SqlField("hasEiffelTower", country.hasEiffelTower)
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
      }
    )
  )
}

object CountryMapping extends LoggedDoobieMappingCompanion {
  override def mkMapping[F[_]: Sync](
      xa: Transactor[F],
      monitor: DoobieMonitor[F]
  ): Mapping[F] = new DoobieMapping[F](xa, monitor) with CountryMapping[F]

  def mkMappingFromXa[F[_]: Sync: LoggerFactory](xa: Transactor[F]): Mapping[F] = {
    given Logger[F] = LoggerFactory[F].getLogger
    mkMapping(xa)
  }
}
