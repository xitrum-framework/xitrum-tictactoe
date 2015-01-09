package tictactoe

import akka.actor.{Actor, ActorRef, Props, Terminated}
import xitrum.Config.actorSystem

object Lobby {
  case object Join
  case class Started(o: Boolean, opponent: ActorRef)

  val ref = actorSystem.actorOf(Props[Lobby])
}

class Lobby extends Actor {
  import Lobby._

  private var waitingPlayer: Option[ActorRef] = None

  def receive = {
    case Join =>
      waitingPlayer match {
        case Some(player) =>
          player ! Started(true, sender)
          sender ! Started(false, player)
          waitingPlayer = None

        case None =>
          waitingPlayer = Some(sender)
          context.watch(sender)
      }

    case Terminated(player) =>
      if (waitingPlayer.contains(player)) waitingPlayer = None
  }
}
