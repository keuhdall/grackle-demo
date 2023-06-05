package graphql

import cats.effect.Concurrent
import edu.gemini.grackle.Mapping
import io.circe.Json

trait GraphQLService[F[_]] {
  def runQuery(op: Option[String], vars: Option[Json], query: String): F[Json]
}

object GraphQLService {
  def fromMapping[F[_]: Concurrent](mapping: Mapping[F]): GraphQLService[F] =
    (op: Option[String], vars: Option[Json], query: String) =>
      mapping.compileAndRun(query, op, vars)
}
