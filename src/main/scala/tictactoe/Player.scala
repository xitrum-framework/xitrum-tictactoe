package tictactoe

import akka.actor.{Actor, ActorRef, Terminated}
import xitrum.SockJsAction
import xitrum.annotation.SOCKJS
import xitrum.SockJsText

@SOCKJS("player")
class Player extends SockJsAction {
  def execute() {
    // Single node
    joinLobby(Lobby.ref)

    // Clustering
//    Lobby.registry ! glokka.Registry.Lookup("lobby")
//    context.become(waitingLobbyLookup)
  }

  // Clustering
  private def waitingLobbyLookup: Actor.Receive = {
    case glokka.Registry.Found("lobby", lobby) =>
      joinLobby(lobby)

    case glokka.Registry.NotFound("lobby") =>
      log.warn("Lobby not ready")
      context.stop(self)
  }

  private def joinLobby(lobby: ActorRef) {
    lobby ! Lobby.Join
    context.become(waitingOpponentJoin)
  }

  private def waitingOpponentJoin: Actor.Receive = {
    case Lobby.Started(iAmO, opponent) =>
      respondSockJsJson(Map("type" -> "started", "iAmO" -> iAmO))

      val board = new Board

      if (iAmO)
        context.become(waitMyMove(iAmO, opponent, board) orElse opponentQuit(opponent))
      else
        context.become(waitOpponentMove(iAmO, opponent, board) orElse opponentQuit(opponent))

      context.watch(opponent)
  }

  private def waitMyMove(iAmO: Boolean, opponent: ActorRef, board: Board): Actor.Receive = {
    case SockJsText(idxText) =>
      val idx = idxText.toInt
      board.move(iAmO, idx) match {
        case Board.MoveInvalid =>

        case Board.MoveValid =>
          respondSockJsJson(Map("type" -> "move", "o" -> iAmO, "idx" -> idx))
          opponent ! idx
          context.become(waitOpponentMove(iAmO, opponent, board) orElse opponentQuit(opponent))

        case Board.MoveWon =>
          respondSockJsJson(Map("type" -> "move", "o" -> iAmO, "idx" -> idx))
          respondSockJsJson(Map("type" -> "won"))
          opponent ! idx
          context.stop(self)

        case Board.MoveDraw =>
          respondSockJsJson(Map("type" -> "move", "o" -> iAmO, "idx" -> idx))
          respondSockJsJson(Map("type" -> "draw"))
          opponent ! idx
          context.stop(self)
      }
  }

  private def waitOpponentMove(iAmO: Boolean, opponent: ActorRef, board: Board): Actor.Receive = {
    case idx: Int =>
      board.move(!iAmO, idx) match {
        case Board.MoveInvalid =>

        case Board.MoveValid =>
          respondSockJsJson(Map("type" -> "move", "o" -> !iAmO, "idx" -> idx))
          context.become(waitMyMove(iAmO, opponent, board) orElse opponentQuit(opponent))

        case Board.MoveWon =>
          respondSockJsJson(Map("type" -> "move", "o" -> !iAmO, "idx" -> idx))
          respondSockJsJson(Map("type" -> "lose"))
          context.stop(self)

        case Board.MoveDraw =>
          respondSockJsJson(Map("type" -> "move", "o" -> !iAmO, "idx" -> idx))
          respondSockJsJson(Map("type" -> "draw"))
          context.stop(self)
      }
  }

  private def opponentQuit(opponent: ActorRef): Actor.Receive = {
    case Terminated(`opponent`) =>
      respondSockJsJson(Map("type" -> "opponentQuit"))
      context.stop(self)
  }
}
