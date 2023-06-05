package graphql.mappings

import doobie.Meta
import edu.gemini.grackle.QueryCompiler.SelectElaborator
import edu.gemini.grackle.{QueryCompiler, TypeRef}
import edu.gemini.grackle.doobie.postgres.DoobieMapping
import edu.gemini.grackle.syntax.schema

case class Country(
    id: Long,
    name: String,
    continent: String,
    bestFood: Option[String],
    hasEiffelTower: Boolean
)

trait CountryMapping[F[_]] extends DoobieMapping[F] {
  object country extends TableDef("country") {
    val id = col("id", Meta[Long])
    val name = col("name", Meta[String])
    val continent = col("continent", Meta[String])
    val bestFood = col("best_food", Meta[String], nullable = true)
    val hasEiffelTower = col("has_eiffel_tower", Meta[Boolean])
  }

  val schema =
    schema"""
      type Query {
        countries(id: Long): [Country]
      }

      type Country {
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
          SqlField("best_food", country.bestFood),
          SqlField("has_eiffel_tower", country.hasEiffelTower)
        )
      )
    )

  override val selectElaborator: QueryCompiler.SelectElaborator = new SelectElaborator(
    Map()
  )
}