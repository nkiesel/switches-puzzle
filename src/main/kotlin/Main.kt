import kotlin.random.Random

enum class Position { UP, DOWN }

class Switch(var position: Position = if (Random.nextBoolean()) UP else DOWN) {
    var left = this
    var right = this

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

fun main(args: Array<String>) {
    val count = args.firstOrNull()?.toIntOrNull() ?: 100

    val start = Switch()
    repeat(count - 1) { start.addRight(Switch()) }
    show(start, count)

    for (countSwitches in listOf(::basic, ::enhanced, ::bidirectional)) {
        // We clone the switch circle for every solution because the solutions are allowed to toggle the switches,
        // but we want all solutions to start with the same position for every switch.
        println("${countSwitches.name}: ${countSwitches(clone(start, count))}")
    }
}

/**
 * This clones a switch circle
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
    return first
}

fun show(start: Switch, count: Int) {
    var switch = start
    repeat(count) {
        print(switch)
        switch = switch.right
    }
    println()
}

data class Answer(val switches: Int, val steps: Int) {
    override fun toString() = "$switches switches, took $steps steps"
}

fun basic(start: Switch): Answer {
    start.position = UP
    var switch = start
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

fun enhanced(start: Switch): Answer {
    start.position = UP
    var switch = start
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

fun bidirectional(start: Switch): Answer {
    start.position = UP
    var switch = start
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
