import kotlin.random.Random

/**
 * Enum for the position of a switch.
 */
enum class Position { UP, DOWN }

/**
 *  When the switch is part of a circle of switches passed to one of the solution functions, then
 *  the switch position can be updated, but the neighbors cannot be changed.
 *
 * Switch has 3 properties:
 *  @property position whether switch is UP or DOWN
 *  @property left the neighbor switch on the left side
 *  @property right the neighbor switch on the right side
 */
class Switch(var position: Position = if (Random.nextBoolean()) UP else DOWN) {
    private var frozenNeighbors = false

    var left = this
        set(value) {
            // prevent solutions to change neighbors of the switch
            check(!frozenNeighbors) { "Not allowed to change neighbors " }
            field = value
        }

    var right = this
        set(value) {
            // prevent solutions to change neighbors of the switch
            check(!frozenNeighbors) { "Not allowed to change neighbors " }
            field = value
        }

    fun freezeNeighbors() {
        frozenNeighbors = true
    }

    /**
     * This inserts a switch into the switch circle to the right of the current switch.
     */
    fun addRight(other: Switch) {
        other.right = right
        other.left = this
        other.right.left = other
        right = other
    }

    override fun toString() = if (position == UP) "↑" else "↓"
}

fun green(text: String) = "\u001B[32m$text\u001B[0m"
fun red(text: String) = "\u001B[31m$text\u001B[0m"

/**
 * Solutions for the switches puzzle.  Parameter for this is the number of switches in the circle.
 */
fun main(args: Array<String>) {
    val count = args.firstOrNull()?.toIntOrNull() ?: 100

    // Create a circle of switches with random positions
    val start = Switch()
    repeat(count - 1) { start.addRight(Switch()) }
    show(start, count)

    // Run solutions. Every solution should return the correct number of switches
    for (countSwitches in listOf(::primitive, ::basic, ::enhanced, ::bidirectional)) {
        // We clone the switch circle for every solution because the solutions are allowed to toggle the switches,
        // but we want all solutions to start with the same position for every switch.
        val answer = countSwitches(clone(start, count))
        val status = if (answer.switches == count) green("correct") else red("incorrect")
        println("${countSwitches.name} is $status and took ${answer.steps} steps")
    }
}

/**
 * This clones a switch circle and returns a frozen circle
 */
fun clone(start: Switch, count: Int): Switch {
    val first = Switch(start.position)
    var orig = start.right
    var next = first
    repeat(count - 1) {
        next.addRight(Switch(orig.position))
        next = next.right
        orig = orig.right
    }
    next = first
    repeat(count) {
        next.freezeNeighbors()
        next = next.right
    }
    return first
}

fun show(start: Switch, count: Int) {
    var switch = start
    print("$count switches: ")
    repeat(count) {
        print(switch)
        switch = switch.right
    }
    println()
}

data class Answer(val switches: Int, val steps: Int)

/**
 * This turns the start switch up, and then goes to the right n steps and turns the switch down at that position.
 * Then it walks back the same number of steps so that it arrives again at the start switch. If that is now turned
 * down, then the last toggle toggled the start switch, and thus the number of switches is identical to the number of
 * steps of the last "go right and then go back".  If not turned down, then repeat but now walk one step further
 * to the right.
 */
fun primitive(start: Switch): Answer {
    var switch = start
    switch.position = UP
    var totalSteps = 0
    var switches = 0
    while (true) {
        switches++
        var steps = 0

        do {
            switch = switch.right
            steps++
        } while (steps < switches)
        switch.position = DOWN

        repeat(steps) { switch = switch.left }
        totalSteps += steps * 2
        if (switch.position == DOWN) return Answer(switches, totalSteps)
    }
}

/**
 * Better than primitive: we only turn back once we reach a switch in the up position. This avoids walking back and
 * forward again when we didi not toggle a switch and thus have not completed the circle.
 */
fun basic(start: Switch): Answer {
    var switch = start
    switch.position = UP
    var totalSteps = 0
    while (true) {
        var steps = 0

        do {
            switch = switch.right
            steps++
        } while (switch.position == DOWN)
        switch.position = DOWN

        repeat(steps) { switch = switch.left }
        totalSteps += steps * 2
        if (switch.position == DOWN) return Answer(steps, totalSteps)
    }
}

/**
 * Even better than basic: We know that the switch to the right of our start switch is turned down. Thus, when we
 * arrive at a switch in UP position, we turn it down but nevertheless walk further to the right and turn all switches
 * down as long as they are in the UP position.  That again reduces the "walk right and walk all the way back" iterations
 * and thus reduces the total number of steps required before we find the correct answer.
 */
fun enhanced(start: Switch): Answer {
    var switch = start
    switch.position = UP
    var totalSteps = 0
    while (true) {
        var steps = 0
        var toggle = false
        while (true) {
            switch = switch.right
            steps++
            if (switch.position == DOWN) {
                if (toggle) break
            } else {
                switch.position = DOWN
                toggle = true
            }
        }

        repeat(steps) { switch = switch.left }
        totalSteps += steps * 2
        if (switch.position == DOWN) return Answer(steps - 1, totalSteps)
    }
}

/**
 * This does the same as enhanced, but instead of always walking right, we walk either to the right or to the left based
 * on which direction has likely fewer steps before finding the next switch in UP position.  This again reduces the total
 * number of steps required to find the correct answer.
 */
fun bidirectional(start: Switch): Answer {
    var switch = start
    switch.position = UP
    var totalSteps = 0
    var leftDowns = 0
    var rightDowns = 0
    while (true) {
        var steps = 0
        var toggle = false
        val walkRight = rightDowns <= leftDowns
        while (true) {
            switch = if (walkRight) switch.right else switch.left
            steps++
            if (switch.position == DOWN) {
                if (toggle) break
            } else {
                switch.position = DOWN
                toggle = true
            }
        }

        repeat(steps) { switch = if (walkRight) switch.left else switch.right }
        if (walkRight) rightDowns = steps else leftDowns = steps
        totalSteps += steps * 2
        if (switch.position == DOWN) return Answer(steps - 1, totalSteps)
    }
}
