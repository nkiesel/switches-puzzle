import Position.DOWN
import Position.UP
import kotlin.random.Random

enum class Position { UP, DOWN }
class Switch {
    var position = if (Random.nextBoolean()) UP else DOWN
}

class Node<T>(val data: T) {
    var left = this
    var right = this

    fun addRight(other: Node<T>) {
        other.right = right

        other.left = this
        other.right.left = other
        right = other
    }
}

fun main(args: Array<String>) {
    val count = args.firstOrNull()?.toIntOrNull() ?: 100

    for (countSwitches in listOf(::basic, ::enhanced, ::bidirectional)) {
        val start = setup(count)
        println(countSwitches(start))
    }
}

fun setup(count: Int): Node<Switch> {
    val first = Node(Switch())
    repeat(count - 1) { first.addRight(Node(Switch())) }
    var start = first
    repeat(Random.nextInt(count)) { start = start.right }
    return start
}

fun basic(start: Node<Switch>): Pair<Int, Int> {
    start.data.position = UP
    var node = start
    var totalSteps = 0
    while (true) {
        var steps = 0

        do {
            node = node.right
            steps++
        } while (node.data.position == DOWN)
        node.data.position = DOWN

        repeat(steps) { node = node.left }
        totalSteps += steps * 2
        if (node.data.position == DOWN) return Pair(steps, totalSteps)
    }
}

fun enhanced(start: Node<Switch>): Pair<Int, Int> {
    start.data.position = UP
    var node = start
    var totalSteps = 0
    while (true) {
        var steps = 0
        var toggle = false
        while (true) {
            node = node.right
            steps++
            if (node.data.position == DOWN) {
                if (toggle) break
            } else {
                node.data.position = DOWN
                toggle = true
            }
        }

        repeat(steps) { node = node.left }
        totalSteps += steps * 2
        if (node.data.position == DOWN) return Pair(steps - 1, totalSteps)
    }
}

fun bidirectional(start: Node<Switch>): Pair<Int, Int> {
    start.data.position = UP
    var node = start
    var totalSteps = 0
    var leftDowns = 0
    var rightDowns = 0
    while (true) {
        var steps = 0
        var toggle = false
        val walkRight = rightDowns <= leftDowns
        while (true) {
            node = if (walkRight) node.right else node.left
            steps++
            if (node.data.position == DOWN) {
                if (toggle) break
            } else {
                node.data.position = DOWN
                toggle = true
            }
        }

        repeat(steps) { node = if (walkRight) node.left else node.right }
        if (walkRight) rightDowns = steps else leftDowns = steps
        totalSteps += steps * 2
        if (node.data.position == DOWN) return Pair(steps - 1, totalSteps)
    }
}
