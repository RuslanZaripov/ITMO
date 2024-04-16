import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class FCPriorityQueue<E : Comparable<E>> {
    private val queue = PriorityQueue<E>()
    private val locked = atomic(false)
    private val size = 4 * Runtime.getRuntime().availableProcessors()
    private val operations = atomicArrayOfNulls<State<E>?>(size)

    sealed class State<out E> {
        data class Add<E>(val element: E) : State<E>()
        class Result<E>(val result: E?) : State<E>()
        object Poll : State<Nothing>()
        object Peek : State<Nothing>()
        object Done : State<Nothing>()
    }

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        return apply(State.Poll)
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        return apply(State.Peek)
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        apply(State.Add(element))
    }

    private fun rand(range: Int): Int {
        return ThreadLocalRandom.current().nextInt(range)
    }

    private fun apply(operation: State<E>): E? {
        var curIdx: Int

        while (true) {
            curIdx = rand(size)
            if (operations[curIdx].compareAndSet(null, operation)) {
                break
            }
        }

        while (true) {
            if (tryLock()) {
                for (i in 0 until size) {
                    val op = operations[i].value ?: continue
                    val newState = when (op) {
                        is State.Add<E> -> {
                            queue.add(op.element)
                            State.Done
                        }

                        is State.Poll -> State.Result(queue.poll())
                        is State.Peek -> State.Result(queue.peek())
                        else -> continue
                    }
                    operations[i].value = newState
                }
                unlock()
            }
            return when (val curState = operations[curIdx].value) {
                is State.Result<E> -> {
                    operations[curIdx].value = null
                    curState.result
                }

                is State.Done -> {
                    operations[curIdx].value = null
                    null
                }

                else -> continue
            }
        }
    }

    private fun tryLock(): Boolean = locked.compareAndSet(expect = false, update = true)

    private fun unlock() {
        locked.value = false
    }
}
