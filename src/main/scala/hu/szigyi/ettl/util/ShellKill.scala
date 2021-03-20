package hu.szigyi.ettl.util

import com.typesafe.scalalogging.StrictLogging

import sys.process._
import scala.language.postfixOps

object ShellKill extends StrictLogging {
  def killGPhoto2Processes(): Unit =
    "pkill -9 -f gphoto" !
}
