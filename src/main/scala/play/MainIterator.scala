package play

import java.io.File

import scala.annotation.tailrec
import scala.io.Source

object MainIterator extends App {
  val it = Source.fromInputStream(getClass.getResourceAsStream("/transaction.dat")).getLines()

  val init: Either[String, Account] = Right(Account.init)

  @tailrec
  def loop(account: Either[String, Account]): Either[String, Account] = {
    account match {
      case Left(_) => account
      case Right(acc) =>
        if (it.hasNext) {
          val line = it.next().trim
          if (line.nonEmpty)
            loop(Account.combine(acc, Transaction(line)))
          else
            Right(acc)
        } else Right(acc)
    }
  }

  println(loop(init))

}
