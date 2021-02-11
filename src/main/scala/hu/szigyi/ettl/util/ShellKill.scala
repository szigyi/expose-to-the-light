package hu.szigyi.ettl.util

import sys.process._
import scala.language.postfixOps

object ShellKill {
  def killGPhoto2Processes: Unit = {
    "pkill -9 -f gphoto" !
  }
}
