package stockrabbit.calculations.kafka.logic

import java.time.Instant
import java.time.Duration
import org.apache.kafka.streams.scala.kstream.KStream
import stockrabbit.calculations.kafka.Transform
import io.circe._
import io.circe.generic.auto._
import stockrabbit.common.model.variable._
import stockrabbit.common.model.variable.name._

class Recent[
  S: Encoder: Decoder, 
  K >: Null: Encoder: Decoder, 
  I >: Null: Encoder: Decoder, 
  O: Encoder: Decoder
](
  init: => S, 
  add: (S, I) => (S, O), 
  remove: (S, I) => S,
  time: I => Instant,
  period: Duration
){
  private def initialize: (S, Seq[I]) = {
    (init, Seq())
  }

  private def process(s: (S, Seq[I]), i: I): ((S, Seq[I]), O) = {
    val state = s._1
    val seq = s._2
    val now = time(i)
    
    def isOld(v: I): Boolean = 
      Duration.between(time(v), now).minus(period).isPositive()

    val toRemove = seq.takeWhile(isOld _)
    val toKeep = seq.dropWhile(isOld _)

    val stateRemove = toRemove.foldLeft(state)(remove)
    val (stateAdded, out) = add(stateRemove, i)
    val seqAdded = toKeep ++ Seq(i)
    ((stateAdded, seqAdded), out)
  }

  def get(stream: KStream[K, I]): KStream[K, O] = {
    Transform.mono[(S, Seq[I]), K, I, O](initialize, process _)(stream)
  }
}

object Recent {
  def recentVariable[S: Encoder: Decoder](
    stat: Statistic,
    period: Duration,
    init: => S, 
    add: (S, Double) => S,
    remove: (S, Double) => S,
    get: S => Double
  )(stream: KStream[Name, Variable]): KStream[Name, Variable] = {
    new Recent[S, Name, Variable, Variable](
      init,
      (s, v) => {
        val newS = add(s, v.value.value)
        val newV = get(newS)
        (newS, Variable(Name(stat, v.name.strategy, v.name.subject), Value(newV), v.time))
      },
      (s, v) => remove(s, v.value.value),
      _.time.time,
      period
    )
      .get(stream)
      .selectKey((_, v) => v.name)
  }

  def sum(stat: Statistic, period: Duration)(stream: KStream[Name, Variable]): KStream[Name, Variable] = {
    recentVariable[Double](
      stat,
      period,
      0,
      (s, v) => s + v,
      (s, v) => s - v,
      s => s
    )(stream)
  }

  def avg(stat: Statistic, period: Duration)(stream: KStream[Name, Variable]): KStream[Name, Variable] = {
    recentVariable[(Double, Int)](
      stat,
      period,
      (0, 0),
      (s, v) => (s._1 + v, s._2 + 1),
      (s, v) => (s._1 - v, s._2 - 1),
      { case (s, n) => s / n }
    )(stream)
  }
}
