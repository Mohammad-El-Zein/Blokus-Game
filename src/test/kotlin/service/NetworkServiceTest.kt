package service

import entity.PlayerType
import edu.udo.cs.sopra.ntf.Rotation as NTFRotation
import entity.Color as G7Color                          // we are Group 7
import entity.GameMode as G7GameMode
import entity.Rotation as G7Rotation
import entity.BlockType as G7BlockType
import kotlin.test.*

/**
 * a class that tests the functionality of the NetworkService
 * it connects to and sends messages through the sopra server
 * this might fail if the server is offline or the secret is outdated
 * */
class NetworkServiceTest {
    /** gets initialized in this class -> late-init */
    private lateinit var rootServiceHost: RootService
    /** gets initialized in this class -> late-init */
    private lateinit var rootServiceGuest: RootService

    /** initializes both connections, does not start a game yet */
    private fun initConnections(sessionID: String) {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame("Host", sessionID, PlayerType.PLAYER)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUESTS)
        val sessionID = rootServiceHost.networkService.client?.sessionID
        assertNotNull(sessionID)

        rootServiceGuest.networkService.joinGame("Guest", sessionID, PlayerType.PLAYER)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)
    }

    /** tests, if a connection can be established to the server via both the hostGame and joinGame function
     * this test has already passed before, if it does not work anymore the issue lies on the server's side */
    @Test
    fun `test hostGame and joinGame`() {
        initConnections("test1")

        assertEquals(ConnectionState.WAITING_FOR_GUESTS, rootServiceHost.networkService.connectionState)
        assertEquals(ConnectionState.WAITING_FOR_INIT, rootServiceGuest.networkService.connectionState)
    }

    /** tests the disconnect function */
    @Test
    fun `test disconnect`() {
        initConnections("test2")

        rootServiceHost.networkService.disconnect()
        rootServiceGuest.networkService.disconnect()

        rootServiceHost.waitForState(ConnectionState.DISCONNECTED)
        rootServiceGuest.waitForState(ConnectionState.DISCONNECTED)

        assertNull(rootServiceHost.networkService.client)
        assertNull(rootServiceGuest.networkService.client)
    }

    /** tests if the actions sendInit, receiveInit, sendAction, receiveAction work as intended for valid inputs */
    @Test
    fun `test ideal game actions`(){
        val players = listOf(
            Triple("Host", G7Color.BLUE, PlayerType.PLAYER), Triple("Guest", G7Color.YELLOW, PlayerType.PLAYER))
        initConnections("test3")

        rootServiceHost.networkService.sendInit(false, players, G7GameMode.TWO_PLAYER_SMALL)
        rootServiceHost.gameService.createGame(false, players, G7GameMode.TWO_PLAYER_SMALL)

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAIT_FOR_MY_TURN)
        // only the guest has a game running
        assertNotNull(rootServiceGuest.currentGame)

        rootServiceHost.networkService.sendAction(false, Pair(0, 0), G7Rotation.RIGHT ,G7BlockType.T4)

        rootServiceHost.waitForState(ConnectionState.WAIT_FOR_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        val currentGuestGame = checkNotNull(rootServiceGuest.currentGame){"something went wrong"}
        val currentState = currentGuestGame.currentStateIndex
        val gameBoard = currentGuestGame.gameStates[currentState].gameBoard

        assertEquals(G7Color.BLUE, gameBoard[1][1])
        assertEquals(G7Color.BLUE, gameBoard[1][2])
        assertEquals(G7Color.BLUE, gameBoard[1][3]) // should be at (1,3), is currently at (3,1)
        assertEquals(G7Color.BLUE, gameBoard[2][2])
    }

    /** tests the rotation conversion functions to bump up line-coverage */
    @Test
    fun `test the rotation conversion functions`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val ntfRotation0 = net.run { G7Rotation.NONE.toNTFRotation() }
        val ntfRotation1 = net.run { G7Rotation.RIGHT.toNTFRotation() }
        val ntfRotation2 = net.run { G7Rotation.BOTTOM.toNTFRotation() }
        val ntfRotation3 = net.run { G7Rotation.LEFT.toNTFRotation() }

        assertEquals(NTFRotation.NONE, ntfRotation0)
        assertEquals(NTFRotation.NINETY, ntfRotation1)
        assertEquals(NTFRotation.ONEHUNDREDANDEIGHTY, ntfRotation2)
        assertEquals(NTFRotation.TWOHUNDREDANDSEVENTY, ntfRotation3)
    }

    /** tests the color conversion functions to bump up line-coverage */
    @Test
    fun `test the color conversion functions`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp0 = net.run { G7Color.BLUE.toNTFColor() }
        val conBlue = net.run { tmp0.toG7Color() }
        val tmp1 = net.run { G7Color.YELLOW.toNTFColor() }
        val conYellow = net.run { tmp1.toG7Color() }
        val tmp2 = net.run { G7Color.RED.toNTFColor() }
        val conRed = net.run { tmp2.toG7Color() }
        val tmp3 = net.run { G7Color.GREEN.toNTFColor() }
        val conGreen = net.run { tmp3.toG7Color() }

        assertEquals(G7Color.BLUE, conBlue)
        assertEquals(G7Color.YELLOW, conYellow)
        assertEquals(G7Color.RED, conRed)
        assertEquals(G7Color.GREEN, conGreen)

        assertFails { net.run { G7Color.BLANK.toNTFColor() } }
    }

    /** tests the gameMode conversion functions to bump up line-coverage */
    @Test
    fun `test the gameMode conversion functions`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp0 = net.run { G7GameMode.FOUR_PLAYER.toNTFGameMode() }
        val con4P = net.run { tmp0.toG7GameMode() }
        val tmp1 = net.run { G7GameMode.THREE_PLAYER.toNTFGameMode() }
        val con3P = net.run { tmp1.toG7GameMode() }
        val tmp2 = net.run { G7GameMode.TWO_PLAYER_FOUR_COLOR.toNTFGameMode() }
        val con2P4C = net.run { tmp2.toG7GameMode() }
        val tmp3 = net.run { G7GameMode.TWO_PLAYER_SMALL.toNTFGameMode() }
        val con2P2C = net.run { tmp3.toG7GameMode() }

        assertEquals(G7GameMode.FOUR_PLAYER, con4P)
        assertEquals(G7GameMode.THREE_PLAYER, con3P)
        assertEquals(G7GameMode.TWO_PLAYER_FOUR_COLOR, con2P4C)
        assertEquals(G7GameMode.TWO_PLAYER_SMALL, con2P2C)
    }

    /** tests the blockType conversion functions to massively improve line-coverage
     * part 1 of multiple parts due to detekt test */
    @Test
    fun `test the blockType conversion functions part 1`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp0 = net.run { G7BlockType.O1.toNTFBlockType() }
        val conO1 = net.run { tmp0.toG7BlockType() }
        assertEquals(G7BlockType.O1, conO1)
        val tmp1 = net.run { G7BlockType.I2.toNTFBlockType() }
        val conI2 = net.run { tmp1.toG7BlockType() }
        assertEquals(G7BlockType.I2, conI2)
        val tmp2 = net.run { G7BlockType.V3.toNTFBlockType() }
        val conV3 = net.run { tmp2.toG7BlockType() }
        assertEquals(G7BlockType.V3, conV3)
    }

    /** part 2 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 2`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp3 = net.run { G7BlockType.I3.toNTFBlockType() }
        val conI3 = net.run { tmp3.toG7BlockType() }
        assertEquals(G7BlockType.I3, conI3)
        val tmp4 = net.run { G7BlockType.T4.toNTFBlockType() }
        val conT4 = net.run { tmp4.toG7BlockType() }
        assertEquals(G7BlockType.T4, conT4)
        val tmp5 = net.run { G7BlockType.O4.toNTFBlockType() }
        val conO4 = net.run { tmp5.toG7BlockType() }
        assertEquals(G7BlockType.O4, conO4)
    }

    /** part 3 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 3`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp6 = net.run { G7BlockType.L4.toNTFBlockType() }
        val conL4 = net.run { tmp6.toG7BlockType() }
        assertEquals(G7BlockType.L4, conL4)
        val tmp7 = net.run { G7BlockType.I4.toNTFBlockType() }
        val conI4 = net.run { tmp7.toG7BlockType() }
        assertEquals(G7BlockType.I4, conI4)
        val tmp8 = net.run { G7BlockType.Z4.toNTFBlockType() }
        val conZ4 = net.run { tmp8.toG7BlockType() }
        assertEquals(G7BlockType.Z4, conZ4)
    }

    /** part 4 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 4`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp9 = net.run { G7BlockType.F5.toNTFBlockType() }
        val conF5 = net.run { tmp9.toG7BlockType() }
        assertEquals(G7BlockType.F5, conF5)
        val tmp10 = net.run { G7BlockType.X5.toNTFBlockType() }
        val conX5 = net.run { tmp10.toG7BlockType() }
        assertEquals(G7BlockType.X5, conX5)
        val tmp11 = net.run { G7BlockType.P5.toNTFBlockType() }
        val conP5 = net.run { tmp11.toG7BlockType() }
        assertEquals(G7BlockType.P5, conP5)
    }

    /** part 5 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 5`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp12 = net.run { G7BlockType.W5.toNTFBlockType() }
        val conW5 = net.run { tmp12.toG7BlockType() }
        assertEquals(G7BlockType.W5, conW5)
        val tmp13 = net.run { G7BlockType.Z5.toNTFBlockType() }
        val conZ5 = net.run { tmp13.toG7BlockType() }
        assertEquals(G7BlockType.Z5, conZ5)
        val tmp14 = net.run { G7BlockType.Y5.toNTFBlockType() }
        val conY5 = net.run { tmp14.toG7BlockType() }
        assertEquals(G7BlockType.Y5, conY5)
    }

    /** part 6 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 6`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp15 = net.run { G7BlockType.L5.toNTFBlockType() }
        val conL5 = net.run { tmp15.toG7BlockType() }
        assertEquals(G7BlockType.L5, conL5)
        val tmp16 = net.run { G7BlockType.U5.toNTFBlockType() }
        val conU5 = net.run { tmp16.toG7BlockType() }
        assertEquals(G7BlockType.U5, conU5)
        val tmp17 = net.run { G7BlockType.T5.toNTFBlockType() }
        val conT5 = net.run { tmp17.toG7BlockType() }
        assertEquals(G7BlockType.T5, conT5)
    }

    /** part 7 of the blockType conversion functions test */
    @Test
    fun `test the blockType conversion functions part 7`(){
        rootServiceHost = RootService()
        val net = rootServiceHost.networkService

        val tmp18 = net.run { G7BlockType.V5.toNTFBlockType() }
        val conV5 = net.run { tmp18.toG7BlockType() }
        assertEquals(G7BlockType.V5, conV5)
        val tmp19 = net.run { G7BlockType.N5.toNTFBlockType() }
        val conN5 = net.run { tmp19.toG7BlockType() }
        assertEquals(G7BlockType.N5, conN5)
        val tmp20 = net.run { G7BlockType.I5.toNTFBlockType() }
        val conI5  = net.run { tmp20.toG7BlockType() }
        assertEquals(G7BlockType.I5, conI5)
    }

    /**
     * busy waiting for the game represented by this [RootService] to reach the desired network [state].
     * Polls the desired state every 100 ms until the [timeout] is reached.
     *
     * This is a simplification hack for testing purposes, so that tests can be linearized on
     * a single thread.
     *
     * @param state the desired network state to reach
     * @param timeout maximum milliseconds to wait (default: 5000)
     *
     * @throws IllegalStateException if desired state is not reached within the [timeout]
     */
    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 5000) {
        var timePassed = 0
        while ( timePassed < timeout) {
            if (networkService.connectionState == state) return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after $timeout ms.")
    }
}