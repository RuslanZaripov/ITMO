package mpp.faaqueue

import kotlinx.atomicfu.*

class FAAQueue<E> {
    private val head: AtomicRef<Segment<E>> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment<E>> // Tail pointer, similarly to the Michael-Scott queue

    init {
        val firstNode = Segment<E>()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [element] to the queue.
     */
    fun enqueue(element: E) {
        while (true) {
            val curTail = tail.value
            val curTailNext = curTail.next.value
            if (curTailNext != null) {
                tail.compareAndSet(curTail, curTailNext)
                continue
            }
            val insertIdx = curTail.enqIdx.getAndIncrement()
            if (insertIdx >= SEGMENT_SIZE) {
                curTail.next.compareAndSet(null, Segment())
                continue
            }
            if (curTail.cas(insertIdx, null, State.Full(element))) {
                return
            }
        }
    }

    /**
     * Retrieves the first element from the queue and returns it;
     * returns `null` if the queue is empty.
     */
    fun dequeue(): E? {
        while (true) {
            val curHead = head.value
            val delIdx = curHead.deqIdx.getAndIncrement()
            if (delIdx >= SEGMENT_SIZE) {
                val nextHead = curHead.next.value ?: return null
                head.compareAndSet(curHead, nextHead)
                continue
            }
            if (curHead.cas(delIdx, null, State.Invalid)) {
                continue
            }
            return when (val element = curHead.get(delIdx)) {
                is State.Full -> element.value
                else -> continue
            }
        }
    }

    /**
     * Returns `true` if this queue is empty, or `false` otherwise.
     */
    val isEmpty: Boolean
        get() = head.value === tail.value && head.value.deqIdx.value >= head.value.enqIdx.value
}

private sealed class State<out E> {
    object Invalid : State<Nothing>()
    data class Full<E>(val value: E) : State<E>()
}

private class Segment<E> {
    val next: AtomicRef<Segment<E>?> = atomic(null)
    val elements = atomicArrayOfNulls<State<E>?>(SEGMENT_SIZE)
    val deqIdx = atomic(0)
    val enqIdx = atomic(0)

    fun get(i: Int) = elements[i].value
    fun cas(i: Int, expect: State<E>?, update: State<E>?) = elements[i].compareAndSet(expect, update)
    fun put(i: Int, value: State<E>?) {
        elements[i].value = value
    }
}

const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

