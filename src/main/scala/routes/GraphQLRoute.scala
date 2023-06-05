package routes

import cats.implicits.*
import cats.effect.Async
import graphql.GraphQLService
import io.circe.Json
import org.http4s.circe.*
import org.http4s.{HttpRoutes, InvalidMessageBodyFailure}
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.LoggerFactory

object GraphQLRoute {
  def apply[F[_]: Async: LoggerFactory](gqlService: GraphQLService[F]): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl.*
    val logger = LoggerFactory[F].getLogger

    HttpRoutes.of[F] { case req @ POST -> Root / "graphql" =>
      for {
        body <- req.as[Json]
        obj <- body.asObject.liftTo[F](InvalidMessageBodyFailure("Invalid GraphQL query"))
        query <- obj("query")
          .flatMap(_.asString)
          .liftTo[F](InvalidMessageBodyFailure("Missing query field"))
        op = obj("operationName").flatMap(_.asString)
        vars = obj("variables")
        result <- gqlService.runQuery(op, vars, query)
        response <- Ok(result)
      } yield response
    }
  }
}
