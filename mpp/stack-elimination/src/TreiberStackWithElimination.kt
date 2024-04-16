package mpp.stackWithElimination

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import kotlin.random.Random

class TreiberStackWithElimination<E> {
    private val top = atomic<Node<E>?>(null)
    private val eliminationArray = atomicArrayOfNulls<State<E>>(ELIMINATION_ARRAY_SIZE)

    sealed class State<out E> {
        data class Waiting<E>(val v: E) : State<E>()
        object Busy : State<Nothing>()
        object Empty : State<Nothing>()
    }

    /**
     * Adds the specified element [x] to the stack.
     */
    fun push(x: E) {
        val tmp = State.Waiting(x)
        val index = Random.nextInt(0, ELIMINATION_ARRAY_SIZE)

        for (offset in 0..ATTEMPTS_COUNT) {
            val nextIndex = shiftWithinArray(index, offset)

            val curState = eliminationArray[nextIndex].value
            if (curState is State.Empty && eliminationArray[nextIndex].compareAndSet(curState, tmp)) {
                repeat(SPIN_COUNT) {
                    Random.nextInt()
                }

                val changed = eliminationArray[nextIndex].value
                if (changed is State.Waiting<*> && eliminationArray[nextIndex].compareAndSet(changed, State.Empty)) {
                    break
                } else {
                    return
                }
            }
        }

        while (true) {
            val curTop = top.value
            val newTop = Node(x, curTop)
            if (top.compareAndSet(curTop, newTop)) return
        }
    }

    /**
     * Retrieves the first element from the stack
     * and returns it; returns `null` if the stack
     * is empty.
     */
    fun pop(): E? {
        val index = Random.nextInt(0, ELIMINATION_ARRAY_SIZE)

        for (offset in 0..ATTEMPTS_COUNT) {
            val nextIndex = shiftWithinArray(index, offset)

            val curValue = eliminationArray[nextIndex].value
            if (curValue is State.Waiting<E> && eliminationArray[nextIndex].compareAndSet(curValue, State.Busy)) {
                return curValue.v
            }
        }

        while (true) {
            val curTop = top.value ?: return null
            val newTop = curTop.next
            if (top.compareAndSet(curTop, newTop)) return curTop.x
        }
    }

    private fun shiftWithinArray(index: Int, offset: Int) = (index + offset) % ELIMINATION_ARRAY_SIZE

    init {
        for (i in 0 until ELIMINATION_ARRAY_SIZE) {
            eliminationArray[i].value = State.Empty
        }
    }
}

private class Node<E>(val x: E, val next: Node<E>?)

private const val ELIMINATION_ARRAY_SIZE = 2 ata  DO NOT CHANGE IT

private const val SPIN_COUNT = 32

private const val ATTEMPTS_COUNT = 4