package hu.szigyi.ettl.tools

import hu.szigyi.ettl.service.EvService

object EvDiffCalculator extends App {

  val A = EvService.ev(1d/8000d, 100, 2.8d)
  val B = EvService.ev(1d/6400d, 100, 2.8d)

  println(
    s"""
       |A=${A}
       |B=${B}
       |Diff=${B-A}""".stripMargin)
}
