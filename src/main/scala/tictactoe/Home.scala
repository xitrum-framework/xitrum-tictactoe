package tictactoe

import xitrum.annotation.GET

@GET("")
class Home extends Layout {
  def execute(): Unit = {
    respondView()
  }
}
