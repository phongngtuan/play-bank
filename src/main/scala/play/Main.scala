package play

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import fs2._

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  private val blockingExecutionContext =
    Resource.make(IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))))(ec => IO(ec.shutdown()))

  val stream: Stream[IO, Unit] = Stream.resource(blockingExecutionContext).flatMap { blockingEC =>
    io.file.readAll[IO](Paths.get(getClass.getResource("/transaction.dat").toURI), Blocker.liftExecutionContext(blockingEC), 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.trim.nonEmpty)
      .zipWithIndex
      .scan(Either.right[String, Account](Account.init))((acc, line) => acc.flatMap(a => Account.combine(a, Transaction(line._1)).leftMap(line._2 + " " + _)))
      .takeWhile(_.isRight, true)
      .last
      .evalMap(e => IO(println(e)))
  }
  override def run(args: List[String]): IO[ExitCode] =
    stream.compile.drain.as(ExitCode.Success)
}
