package play

sealed trait Account

object Initial extends Account
final case class Ongoing(balance: Int) extends Account
final case class Terminal(balance: Int) extends Account

sealed trait Transaction
object Begin extends Transaction
final case class Deposit(value: Int) extends Transaction
final case class Withdraw(value: Int) extends Transaction
object End extends Transaction

object Account {
  def init: Account = Initial

  def combine(account: Account, transaction: Transaction): Either[String, Account] = account match {
    case Initial =>
      transaction match {
        case Begin => Right(Ongoing(0))
        case _ => Left("Account must be opened first")
      }
    case Ongoing(balance) =>
      transaction match {
        case Deposit(value) => Right(Ongoing(balance + value))
        case Withdraw(value) if value <= balance => Right(Ongoing(balance - value))
        case End => Right(Terminal(balance))
        case _ =>
          println(s"$balance - $transaction")
          Left("Overdrafted")
      }
    case Terminal(_) => Left("Account is terminal")
  }
}

object Transaction {
  def apply(line: String): Transaction =
    if (line.startsWith("#")) Deposit(0)
    else if (line == "BEGIN") Begin
    else if (line == "END") End
    else {
      val tokens = line.split(" ").toList
      val value = tokens(1).toInt
      if (line.startsWith("D"))
        Deposit(value)
      else
        Withdraw(value)
    }
}
