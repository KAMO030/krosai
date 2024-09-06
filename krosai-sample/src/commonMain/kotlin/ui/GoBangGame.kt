package org.krosai.sample.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.krosai.core.chat.client.ChatClient
import org.krosai.core.chat.function.buildFunctionCall
import org.krosai.core.factory.ModelFactory
import org.krosai.core.util.SerialDescription
import org.krosai.sample.di.AiModule
import kotlin.math.min

@Composable
fun GoBangGame() {
    KoinApplication(
        application = {
            modules(AiModule)
        }
    ) {
        GoBangGameSetup()
    }

}

@Composable
private fun GoBangGameSetup(
    chatClient: ChatClient = koinInject<ModelFactory>().createChatClient {
        systemText {
            """
             你是一个五子棋游戏的高手。
             棋盘上的数字代表了棋子的状态，0表示空白位置，1表示玩家的棋子，2表示AI(你)的棋子。
             当前棋盘状态如下：
             ${Json.encodeToString(get("board"))}
             不用回答问题,请你作为对手,
             根据给出的棋盘状态,作为Ai下一步最佳的落棋点坐标的x和y然后直接调用工具方法落棋子。
             请不用回答问题,直接调用工具!
        """.trimIndent()
        }
        userText { "现在轮到你下棋了,请落棋子" }
    },
) {
    var board: MutableList<MutableList<Int>> by remember { mutableStateOf(createBoard()) }
    var currentPlayer by remember { mutableStateOf(1) }
    var gameEnded by remember { mutableStateOf(false) }

    LaunchedEffect(currentPlayer) {
        if (gameEnded.not() && currentPlayer == 2) {
            println("AI call")
            chatClient.call {
                system {
                    "board" to board
                }
                functions {
                    +buildFunctionCall(
                        name = "ChessPiece",
                        description = "输入两个int作为坐标,落棋子到指定的坐标"
                    ) {
                        withCall<CoordinatesRequest> {
                            val move = it.y.toInt() to it.x.toInt()

                            board[move.first][move.second] = currentPlayer
                            if (checkForWin(board, move, currentPlayer)) {
                                gameEnded = true
                            } else {
                                currentPlayer = if (currentPlayer == 1) 2 else 1
                            }
                            return@withCall "ok"
                        }
                    }
                }
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
            if (gameEnded) {
                GameOver(currentPlayer) {
                    board = createBoard()
                    currentPlayer = 1
                    gameEnded = false
                }
            } else {
                Board(board, onCellClick = { row, col ->
                    if (gameEnded.not() && board[row][col] == 0) {
                        board[row][col] = currentPlayer
                        if (checkForWin(board, row to col, currentPlayer)) {
                            gameEnded = true
                        } else {
                            currentPlayer = if (currentPlayer == 1) 2 else 1

                        }
                    }
                })
            }

        }
    }
}


@Composable
fun GameOver(
    winner: Int,
    resetGame: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Over!",
            style = MaterialTheme.typography.h4,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Winner: ${if (winner == 1) "Player 1" else "AI"}",
            style = MaterialTheme.typography.body1,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // 重新开始游戏
                resetGame()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Restart")
        }
    }
}


private fun createBoard(): MutableList<MutableList<Int>> {
    return MutableList(15) {
        SnapshotStateList<Int>().also { list ->
            repeat(15) {
                list.add(0)
            }
        }
    }
}


private fun updateBoard(
    board: MutableList<MutableList<Int>>,
    move: Pair<Int, Int>,
    player: Int,
): MutableList<MutableList<Int>> {
    val newBoard = board.map { it.toMutableList() }.toMutableList()
    newBoard[move.first][move.second] = player
    return newBoard
}

private fun aiMakeMove(board: MutableList<MutableList<Int>>): Pair<Int, Int>? {
    val emptyCells = mutableListOf<Pair<Int, Int>>()

    for (row in board.indices) {
        for (col in board[row].indices) {
            if (board[row][col] == 0) {
                emptyCells.add(row to col)
            }
        }
    }

    return if (emptyCells.isEmpty()) null else emptyCells.random()
}

@Composable
private fun Board(board: MutableList<MutableList<Int>>, onCellClick: (Int, Int) -> Unit) {
    val cellSize = 40.dp
    val padding = 10.dp
    val boardSize = 15 * cellSize
    Canvas(modifier = Modifier.size(boardSize)) {
        // Draw lines
        for (i in 0 until 15) {
            drawLine(
                start = Offset(padding.toPx(), (padding + i * cellSize).toPx()),
                end = Offset((padding + 14 * cellSize).toPx(), (padding + i * cellSize).toPx()),
                color = Color.Black,
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                start = Offset((padding + i * cellSize).toPx(), padding.toPx()),
                end = Offset((padding + i * cellSize).toPx(), (padding + 14 * cellSize).toPx()),
                color = Color.Black,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw stones
        board.forEachIndexed { row, cells ->
            cells.forEachIndexed { col, player ->
                if (player != 0) {
                    drawCircle(
                        color = if (player == 1) Color.Black else Color.White,
                        radius = min(cellSize.toPx() / 2, cellSize.toPx() / 2) - 5,
                        center = Offset((padding + col * cellSize).toPx(), (padding + row * cellSize).toPx())
                    )
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(boardSize)
    ) {
        repeat(15) { rowIndex ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(15) { colIndex ->
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clickable { onCellClick(rowIndex, colIndex) },
                    )
                }
            }
        }
    }
}

fun checkForWin(board: List<List<Int>>, lastMove: Pair<Int, Int>, player: Int): Boolean {
    val directions = listOf(
        Pair(1, 0), // 水平
        Pair(0, 1), // 垂直
        Pair(1, 1), // 斜线（从左上到右下）
        Pair(1, -1) // 斜线（从右上到左下）
    )

    for (direction in directions) {
        var count = 1

        // 检查一个方向
        count += countInDirection(board, lastMove, player, direction.first, direction.second)
        // 检查反方向
        count += countInDirection(board, lastMove, player, -direction.first, -direction.second)

        if (count >= 5) {
            return true
        }
    }

    return false
}

fun countInDirection(board: List<List<Int>>, lastMove: Pair<Int, Int>, player: Int, dx: Int, dy: Int): Int {
    var count = 0
    var x = lastMove.first + dx
    var y = lastMove.second + dy

    while (x in board.indices && y in board[0].indices && board[x][y] == player) {
        count++
        x += dx
        y += dy
    }
    return count
}

@SerialDescription("Coordinates Request")
@Serializable
data class CoordinatesRequest(
    @SerialName("x")
    @SerialDescription("x coordinate")
    val x: String,
    @SerialName("y")
    @SerialDescription("y coordinate")
    val y: String,
)


