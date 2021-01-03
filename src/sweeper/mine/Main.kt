/**
 * Almas Baimagambetov's YouTube Mine Sweeper vid ported to Kotlin:
 *
 *    https://www.youtube.com/watch?v=JwcyxuKko_M
 */
package sweeper.mine

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage

fun main(args: Array<String>) = Application.launch(Main::class.java, *args)

const val W = 800
const val H = 600
const val TILE_SIZE = 40
const val TILE_X = W / TILE_SIZE
const val TILE_Y = H / TILE_SIZE

// Relative offsets of a cell's 8 neighbors
class Pt(val x: Int, val y: Int)
val NEIGHBORS = listOf(Pt(0, -1), Pt(1, -1), Pt(1, 0), Pt(1, 1), Pt(0, 1), Pt(-1, 1), Pt(-1, 0), Pt(-1, -1))

// Central object of the game
class Tile(val x: Int, val y: Int, val hasBomb: Boolean) : StackPane() {
	// FX nodes
	val border = Rectangle(TILE_SIZE - 2.0, TILE_SIZE - 2.0)
		.apply {
			stroke = Color.LIGHTGRAY
		}
	val label = Text()
		.apply {
			isVisible = false
			font = Font.font(18.0)
		}

	// Game logic properties
	var isOpen = false         // Set when the tile is left clicked
	var badNeighbors = 0       // The number of neighboring bombs
	val neighbors: List<Tile>  // Constructs a list of valid neighboring tiles
		get() = NEIGHBORS
			.filter { x + it.x in 0 until TILE_X && y + it.y in 0 until TILE_Y } // Discard out of bounds neighbors
			.map { grid[x + it.x][y + it.y] }
	var isFlagged = false      // Toggled when the tile is right clicked
		set(value) {
			if (isOpen || value == isFlagged) return
			field = value

			if (value) {
				label.text = "F"
				label.isVisible = true
				border.fill = null
			} else {
				label.isVisible = false
				border.fill = Color.BLACK
			}
		}

	init {
		if (hasBomb) bombs++
		children.addAll(border, label)
		translateX = (x * TILE_SIZE).toDouble()
		translateY = (y * TILE_SIZE).toDouble()
		setOnMouseClicked {
			if (it.button == MouseButton.PRIMARY) open()
			if (it.button == MouseButton.SECONDARY) isFlagged = !isFlagged
			updateScore()
		}
	}

	private fun open() {
		if (isOpen) return

		if (hasBomb) { // Game over, show all the bombs
			grid.forEach { x ->
				x.forEach { y ->
					if (y.hasBomb) {
						if (!y.isFlagged) { // Undetected bomb
							y.label.fill = Color.RED
							y.label.text = "X"
						}
						y.label.style = "-fx-font-weight: bold;"
						y.label.isVisible = true
						y.border.fill = null
					} else {
						if (y.isFlagged) {   // False flag LoL
							y.label.fill = Color.RED
						} else {             // Show the tile
							y.show()
						}
					}
				}
			}
			gameOver("Boom! You missed ${bombs - score} bombs")
		}

		isOpen = true
		show()
		if (badNeighbors == 0) neighbors
			.filter { !it.hasBomb }
			.forEach { it.open() }  // Recursively call to open all surrounding tiles
	}

	private fun show() {
		label.text = if (badNeighbors > 0) badNeighbors.toString() else ""
		label.isVisible = true
		border.fill = null
	}
}

fun updateScore() {
	score = 0
	var tbd = false
	grid.forEach { x ->
		x.forEach { y ->
			if (y.hasBomb && y.isFlagged) {
				score++
			} else if (!y.isOpen) {
				tbd = true
			}
		}
	}
	if (!tbd && score == bombs) gameOver("You Won!")
}

fun gameOver(message: String) {
	Alert(Alert.AlertType.INFORMATION)
		.apply {
			title = "Game Over"
			headerText = null
			contentText = message
		}
		.showAndWait()
	Platform.exit()
}

var grid = Array(TILE_X) { x -> Array(TILE_Y) { y -> Tile(x, y, Math.random() < 0.07) } }
var score = 0
var bombs = 0

class Main : Application() {

	override fun start(primaryStage: Stage) {
		println("You need to flag $bombs bombs.  Good luck!")
		primaryStage
			.apply {
				title = "kMineSweeper"
				scene = Scene(createContent())
			}
			.show()
	}

	private fun createContent(): Parent {
		val root = Pane().apply {
			prefWidth = 800.0
			prefHeight = 600.0
		}

		for (x in 0 until TILE_X)
			for (y in 0 until TILE_Y)
				root.children.add(grid[x][y]
					.apply {
						badNeighbors = neighbors.fold(0) { sum, n -> sum + if (n.hasBomb) 1 else 0 }
					})

		return root
	}
}