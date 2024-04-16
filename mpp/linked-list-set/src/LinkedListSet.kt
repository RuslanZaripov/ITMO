package mpp.linkedlistset

import kotlinx.atomicfu.*

class LinkedListSet<E : Comparable<E>> {
    private val first = Node<E>(state = State.Begin, next = null)
    private val last = Node<E>(state = State.End, next = null)

    init {
        first.setNext(last)
    }

    private val head = atomic(first)

    class Pair<E : Comparable<E>>(var left: Node<E>, var right: Node<E>)

    /**
     * Adds the specified element to this set
     * if it is not already present.
     *
     * Returns `true` if this set did not
     * already contain the specified element.
     */
    fun add(element: E): Boolean {
        while (true) {
            val pair = find(element)

            val lastPair = pair.right
            if (lastPair.state is State.Active<E> && lastPair.state.element == element) {
                return false
            }

            val newNode = Node(State.Active(element), pair.right)
            if (pair.left.casNext(pair.right, newNode)) {
                return true
            }
        }
    }

    /**
     * Removes the specified element from this set
     * if it is present.
     *
     * Returns `true` if this set contained
     * the specified element.
     */
    fun remove(element: E): Boolean {
        while (true) {
            val pair = find(element)

            val lastWindow = pair.right
            if (lastWindow.state is State.Active<E> && lastWindow.state.element != element) {
                return false
            }

            val next = pair.right.next ?: return false
            if (next.state is State.Removed) {
                return false
            }

            if (pair.right.casNext(next, next.copy(element = State.Removed))) {
                pair.left.casNext(pair.right, next)
                return true
            }
        }
    }

    /**
     * Returns `true` if this set contains
     * the specified element.
     */
    fun contains(element: E): Boolean {
        val pair = find(element)

        val lastPair = pair.right
        return lastPair.state is State.Active<E> && lastPair.state.element == element
    }

    private fun find(element: E): Pair<E> {
        while (true) {
            val first = head.value
            val pair = Pair(first, first.next!!)

            while (true) {
                val lastPair = pair.right
                if (lastPair.state is State.End
                    || (lastPair.state is State.Active<E>
                            && lastPair.state.element >= element)
                ) {
                    break
                }
                pair.left = pair.right
                pair.right = pair.right.next!!
            }

            return pair
        }
    }
}

sealed class State<out E> {
    data class Active<E>(val element: E) : State<E>()
    object Removed : State<Nothing>()
    object Begin : State<Nothing>()
    object End : State<Nothing>()
}

class Node<E : Comparable<E>>(val state: State<E>, next: Node<E>?) {
    private val _next = atomic(next)
    val next get() = _next.value
    fun setNext(value: Node<E>?) {
        _next.value = value
    }

    fun casNext(expected: Node<E>?, update: Node<E>?) =
        _next.compareAndSet(expected, update)

    fun copy(element: State.Removed): Node<E> {
        return Node(element, next)
    }
}