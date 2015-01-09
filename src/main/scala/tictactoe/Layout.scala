package tictactoe

import xitrum.Action

trait Layout extends Action {
  override def layout = renderViewNoLayout[Layout]()
}
