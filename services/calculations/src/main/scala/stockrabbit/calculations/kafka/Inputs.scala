package stockrabbit.calculations.kafka

import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.kstream.Named
import stockrabbit.common.model.variable._
import scala.annotation.unused

trait Inputs {
  def stockValues: KStream[Name, Variable]
  def stockTrades: KStream[Name, Variable]
}

object Inputs {
  private val stockValuesName = "stockValue"
  private def stockValuesPred(name: Name, @unused variable: Variable): Boolean = 
    name.statistic.statistic == stockValuesName

  private val stockTradesName = "stockTrade"
  private def stockTradesPred(name: Name, @unused variable: Variable): Boolean =
    name.statistic.statistic == stockTradesName

  private def branchName(n: String): String = "branch-" + n

  def impl(stream: KStream[Name, Variable]): Inputs = {
    val branches = stream
      .split(Named.as(branchName("")))
      .branch(stockValuesPred(_, _), Branched.as(stockValuesName))
      .branch(stockTradesPred(_, _), Branched.as(stockTradesName))
      .noDefaultBranch()

    println("Branches ==============================================================")
    println(branches)
    val stockValuesBranch = branches(branchName(stockValuesName))
    val stockTradesBranch = branches(branchName(stockTradesName))

    new Inputs {
      def stockValues: KStream[Name,Variable] = stockValuesBranch
      def stockTrades: KStream[Name,Variable] = stockTradesBranch
    }
  }
}