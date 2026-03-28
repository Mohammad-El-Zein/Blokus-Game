package service

import entity.BlokusGame
import service.bot.BotService


/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService{
    /** gameService's link back to rootService */
    val gameService = GameService(this)

    /** playerActionService's link back to rootService */
    val playerActionService = PlayerActionService(this)

    /** networkService's link back to rootService */
    val networkService = NetworkService(this)

    /** botManager's link back to rootService */
    val botService = BotService(this)

    /**
     * The currently active game. Can be `null`, if no game has started yet.
     */
    var currentGame : BlokusGame? = null
}