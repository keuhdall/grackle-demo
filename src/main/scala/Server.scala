import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Server extends IOApp {
  def app[F[_]: Async]: Resource[F, Unit] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](32)
    xa <- HikariTransactor.newHikariTransactor[F](
      driverClassName = "org.postgresql.Driver",
      url = "jdbc:postgresql://postgres/demo",
      user = "root",
      pass = "password",
      connectEC = ec
    )
  } yield ()

  override def run(args: List[String]): IO[ExitCode] = app[IO].useForever

}
