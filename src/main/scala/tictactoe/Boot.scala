package tictactoe

import xitrum.Server

object Boot {
  def main(args: Array[String]): Unit = {
    Server.start()
    Server.stopAtShutdown()
  }
}
