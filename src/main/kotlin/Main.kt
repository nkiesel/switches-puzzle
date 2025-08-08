import Position.DOWN
import Position.UP
import kotlin.random.Random

/**
 * Enum for the position of a switch.
 */
enum class Position(private val c: String) {
    UP("↑"), DOWN("↓");

    override fun toString() = c
}

fun green(text: String) = "\u001B[32m$text\u001B[0m"
fun red(text: String) = "\u001B[31m$text\u001B[0m"

class Hallway(private val count: Int) {
    private val initial = List(count) { if (Random.nextBoolean()) UP else DOWN }
    private var pos: Int = 0
    private var steps: Int = 0
    private val switches = initial.toMutableList()

    fun restore() {
        pos = 0
        steps = 0
        switches.clear()
        switches.addAll(initial)
    }

    override fun toString() = "$count switches: ${switches.joinToString("")}"

    fun isUp() = switches[pos] == UP

    fun isDown() = switches[pos] == DOWN

    fun turnUp() {
        switches[pos] = UP
    }

    fun turnDown() {
        switches[pos] = DOWN
    }

    fun goRight(steps: Int = 1) {
        pos = (pos + steps) % count
        this.steps += steps
    }

    fun goLeft(steps: Int = 1) {
        pos = (pos + count - steps % count) % count
        this.steps += steps
    }

    fun steps() = steps
}

/**
 * Solutions for the switches puzzle.  Parameter for this is the number of switches in the circle.
 */
fun main(args: Array<String>) {
    val count = args.firstOrNull()?.toIntOrNull() ?: 100

    // Create a circle of switches with random positions
    val hallway = Hallway(count)
    println(hallway)

    // Run solutions. Every solution should return the correct number of switches
    for (countSwitches in listOf(::primitive, ::basic, ::enhanced, ::bidirectional)) {
        val answer = countSwitches(hallway)
        val status = if (answer == count) green("correct") else red("incorrect")
        println("${countSwitches.name} is $status and took ${hallway.steps()} steps")
        // We reset the switch circle after every solution because the solutions are allowed to toggle the switches,
        // but we want all solutions to start with the same position for every switch.
        hallway.restore()
    }
}


/**
 * This turns the start switch up, and then goes to the right n steps and turns the switch down at that position.
 * Then it walks back the same number of steps so that it arrives again at the start switch. If that is now turned
 * down, then the last toggle toggled the start switch, and thus the number of switches is identical to the number of
 * steps of the last "go right and then go back".  If not turned down, then repeat but now walk one step further
 * to the right.
 */
private fun primitive(hallway: Hallway): Int {
    with(hallway) {
        turnUp()
        var switches = 0
        while (true) {
            switches++
            var steps = 0

            do {
                goRight()
                steps++
            } while (steps < switches)
            turnDown()

            goLeft(steps)
            if (isDown()) return switches
        }
    }
}

/**
 * Better than primitive: we only turn back once we reach a switch in the up position. This avoids walking back and
 * forward again when we didi not toggle a switch and thus have not completed the circle.
 */
private fun basic(hallway: Hallway): Int {
    with(hallway) {
        turnUp()
        while (true) {
            var steps = 0

            do {
                goRight()
                steps++
            } while (isDown())
            turnDown()

            goLeft(steps)
            if (isDown()) return steps
        }
    }
}

/**
 * Even better than basic: We know that the switch to the right of our start switch is turned down. Thus, when we
 * arrive at a switch in UP position, we turn it down but nevertheless walk further to the right and turn all switches
 * down as long as they are in the UP position.  That again reduces the "walk right and walk all the way back" iterations
 * and thus reduces the total number of steps required before we find the correct answer.
 */
private fun enhanced(hallway: Hallway): Int {
    with(hallway) {
        turnUp()
        while (true) {
            var steps = 0
            var sawUp = false
            while (true) {
                goRight()
                steps++
                if (isUp()) turnDown().also { sawUp = true } else if (sawUp) break
            }

            goLeft(steps)
            if (isDown()) return steps - 1
        }
    }
}

/**
 * This does the same as enhanced, but instead of always walking right, we walk either to the right or to the left based
 * on which direction has likely fewer steps before finding the next switch in UP position.  This again reduces the total
 * number of steps required to find the correct answer.
 */
private fun bidirectional(hallway: Hallway): Int {
    with(hallway) {
        turnUp()
        var leftDowns = 0
        var rightDowns = 0
        while (true) {
            var steps = 0
            var toggle = false
            val walkRight = rightDowns <= leftDowns
            while (true) {
                if (walkRight) goRight() else goLeft()
                steps++
                if (isDown()) {
                    if (toggle) break
                } else {
                    turnDown()
                    toggle = true
                }
            }

            if (walkRight) {
                goLeft(steps)
                rightDowns = steps
            } else {
                goRight(steps)
                leftDowns = steps
            }
            if (isDown()) return steps - 1
        }
    }
}
