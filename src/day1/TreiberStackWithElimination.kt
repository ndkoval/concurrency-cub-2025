package day1

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReferenceArray

open class TreiberStackWithElimination<E> : Stack<E> {
    private val stack = TreiberStack<E>()

    private val eliminationArray = AtomicReferenceArray<Any?>(ELIMINATION_ARRAY_SIZE)

    override fun push(element: E) {
        if (tryPushElimination(element)) return
        stack.push(element)
    }

    protected open fun tryPushElimination(element: E): Boolean {


        val randomCellIdx = randomCellIndex()
        if (!eliminationArray.compareAndSet(randomCellIdx, CELL_STATE_EMPTY, element)) return false
        repeat(ELIMINATION_WAIT_CYCLES) {
            if (eliminationArray.compareAndSet(randomCellIdx, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) return true
        }
        if (eliminationArray.compareAndSet(randomCellIdx, element, CELL_STATE_EMPTY)) return false
        if (eliminationArray.compareAndSet(randomCellIdx, CELL_STATE_RETRIEVED, CELL_STATE_EMPTY)) return true

        throw IllegalStateException("The cell state is in an unexpected state")

    }

    override fun pop(): E? = tryPopElimination() ?: stack.pop()

    private fun tryPopElimination(): E? {


        val randomCellIdx = randomCellIndex()
        val element = eliminationArray.get(randomCellIdx)
        if (element == CELL_STATE_EMPTY || element == CELL_STATE_RETRIEVED) return null
        if (eliminationArray.compareAndSet(randomCellIdx, element, CELL_STATE_RETRIEVED)) {
            return element as E?
        }
        return null

    }

    private fun randomCellIndex(): Int =
        ThreadLocalRandom.current().nextInt(eliminationArray.length())

    companion object {
        private const val ELIMINATION_ARRAY_SIZE = 2 // Do not change!
        private const val ELIMINATION_WAIT_CYCLES = 1 // Do not change!

        // Initially, all cells are in EMPTY state.
        private val CELL_STATE_EMPTY = null

        // `tryPopElimination()` moves the cell state
        // to `RETRIEVED` if the cell contains element.
        private val CELL_STATE_RETRIEVED = Any()
    }
}