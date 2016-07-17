package tictactoe

object Board {
  sealed trait MoveResult
  object MoveInvalid extends MoveResult
  object MoveValid   extends MoveResult
  object MoveWon     extends MoveResult
  object MoveDraw    extends MoveResult
}

class Board {
  import Board._

  private val pieces = new Array[Byte](9)
  for (i <- 0 until 9) pieces(i) = -1

  def move(o: Boolean, idx: Int): MoveResult = {
    if (idx < 0 || idx > 8) return MoveInvalid

    if (pieces(idx) != -1) return MoveInvalid

    pieces(idx) = if (o) 0 else 1
    state(o)
  }

  private def state(o: Boolean): MoveResult = {
    val v = if (o) 0 else 1

    // Check each row
    for (r <- 0 to 2) {
      if (pieces(r * 3) == v && pieces(r * 3 + 1) == v && pieces(r * 3 + 2) == v)
        return MoveWon
    }

    // Check each column
    for (c <- 0 to 2) {
      if (pieces(0 * 3 + c) == v && pieces(1 * 3 + c) == v && pieces(2 * 3 + c) == v)
        return MoveWon
    }

    // Check cross \
    if (pieces(0 * 3 + 0) == v && pieces(1 * 3 + 1) == v && pieces(2 * 3 + 2) == v)
      return MoveWon

    // Check cross /
    if (pieces(0 * 3 + 2) == v && pieces(1 * 3 + 1) == v && pieces(2 * 3 + 0) == v)
      return MoveWon


    // Check draw (can't make more moves)
    for (idx <- 0 until 9)
      if (pieces(idx) == -1) return MoveValid
    MoveDraw
  }
}
