package hu.szigyi.ettl.util

import sys.process._
import scala.language.postfixOps

class ShellKill {
  def killGPhoto2Processes: Unit = {
    "pkill -9 -f gphoto" !
  }
}
