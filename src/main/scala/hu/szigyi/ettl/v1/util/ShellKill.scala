package hu.szigyi.ettl.v1.util

import com.typesafe.scalalogging.StrictLogging

import sys.process._
import scala.language.postfixOps

object ShellKill extends StrictLogging {
  def killGPhoto2Processes(): Unit = {
    logger.warn("killing gphoto process...")
    "pkill -9 -f gphoto" !
  }
}
