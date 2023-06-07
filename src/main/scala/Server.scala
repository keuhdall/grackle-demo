import cats.effect.*
import com.comcast.ip4s.{ipv4, port}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import graphql.GraphQLService
import graphql.mappings.CountryMapping
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import routes.GraphQLRoute

object Server extends IOApp {
  private def app[F[_]: Async: LoggerFactory]: Resource[F, Server] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = "org.postgresql.Driver",
        url = "jdbc:postgresql://postgres/default",
        user = "root",
        pass = "admin",
        connectEC = ec
      )
      countryMapping = CountryMapping.mkMappingFromXa(xa)
      gqlService = GraphQLService.fromMapping(countryMapping)
      routes = GraphQLRoute[F](gqlService)
      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(routes.orNotFound)
        .build
    } yield server
  }

  override def run(args: List[String]): IO[ExitCode] = {
    given LoggerFactory[IO] = Slf4jFactory.create[IO]
    app[IO].useForever.as(ExitCode.Success)
  }
}
