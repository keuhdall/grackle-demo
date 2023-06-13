//package graphql.mappings
//
//import cats.effect.Sync
//import doobie.Transactor
//import doobie.util.meta.Meta
//import doobie.util.transactor
//import edu.gemini.grackle.Ast.Value.StringValue
//import edu.gemini.grackle.Predicate.{And, Const, Eql}
//import edu.gemini.grackle.Query.{Binding, Filter, Select, Unique}
//import edu.gemini.grackle.QueryCompiler.SelectElaborator
//import edu.gemini.grackle.Value.BooleanValue
//import edu.gemini.grackle.{Mapping, QueryCompiler, Schema, TypeRef}
//import edu.gemini.grackle.doobie.postgres.{
//  DoobieMapping,
//  DoobieMonitor,
//  LoggedDoobieMappingCompanion
//}
//import edu.gemini.grackle.syntax.*
//import org.typelevel.log4cats.{Logger, LoggerFactory}
//
//trait CityMapping[F[_]] extends DoobieMapping[F] { self: CountryMapping[F] =>
//  object city extends TableDef("cities") {
//    val id: ColumnRef = col("id", Meta[Long])
//    val name: ColumnRef = col("name", Meta[String])
//    val countryId: ColumnRef = col("country_id", Meta[Long])
//    val isCapital: ColumnRef = col("is_capital", Meta[Boolean])
//  }
//
//  override val schema = schema"""
//  type Query {
//    cities(countryId: Int): [City!]
//    city(cityId: Int): City
//  }
//
//  type City {
//    id: ID!
//    name: String!
//    country: Country!
//    isCapital: Boolean!
//  }
//  """
//
//  val QueryType: TypeRef = schema.ref("Query")
//  val CityType: TypeRef = schema.ref("City")
//
//  val typeMappings: List[ObjectMapping] =
//    List(
//      ObjectMapping(
//        tpe = QueryType,
//        fieldMappings = List(
//          SqlObject("cities")
//        )
//      ),
//      ObjectMapping(
//        tpe = CityType,
//        fieldMappings = List(
//          SqlField("id", city.id, key = true),
//          SqlField("name", city.name),
//          SqlField("countryId", city.countryId, hidden = true),
//          SqlField("isCapital", city.isCapital),
//          SqlObject("country", Join(city.countryId, country.id))
//        )
//      )
//    )
//
//  override val selectElaborator: QueryCompiler.SelectElaborator = new SelectElaborator(
//    Map(
//      QueryType -> {
//        case Select("city", List(Binding("id", StringValue(id))), child) =>
//          Select(
//            "city",
//            Nil,
//            Unique(Filter(Eql(CityType / id, Const(id)), child))
//          ).success
//        case Select(
//              "cities",
//              List(
//                Binding("countryId", StringValue(countryId)),
//                Binding("isCapital", BooleanValue(isCapital))
//              ),
//              child
//            ) =>
//          Select(
//            "cities",
//            Nil,
//            Filter(
//              And(
//                Eql(CityType / "countryId", Const(countryId)),
//                Eql(CityType / "isCapital", Const(isCapital))
//              ),
//              child
//            )
//          ).success
//      }
//    )
//  )
//}
//
//object CityMapping extends LoggedDoobieMappingCompanion {
//  override def mkMapping[F[_]: Sync](
//      xa: Transactor[F],
//      monitor: DoobieMonitor[F]
//  ): Mapping[F] = new DoobieMapping[F](xa, monitor) with CityMapping[F]
//
//  def mkMappingFromXa[F[_]: Sync: LoggerFactory](xa: Transactor[F]): Mapping[F] = {
//    given Logger[F] = LoggerFactory[F].getLogger
//    mkMapping[F](xa)
//  }
//}
