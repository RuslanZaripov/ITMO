package mpp.dynamicarray

import kotlinx.atomicfu.*

interface DynamicArray<E> {
    /**
     * Returns the element located in the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the [size] of this array.
     */
    fun get(index: Int): E

    /**
     * Puts the specified [element] into the cell [index],
     * or throws [IllegalArgumentException] if [index]
     * exceeds the [size] of this array.
     */
    fun put(index: Int, element: E)

    /**
     * Adds the specified [element] to this array
     * increasing its [size].
     */
    fun pushBack(element: E)

    /**
     * Returns the current size of this array,
     * it increases with [pushBack] invocations.
     */
    val size: Int
}

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))

    override fun get(index: Int): E {
        while (true) {
            val curCore = core.value
            val curValue = curCore.get(index)
            val curCapacity = curCore.capacity

            when (curValue) {
                is State.Active<E> -> return curValue.value
                is State.Fixed<E> -> return curValue.value
                else -> moveCore(curCore, curCapacity)
            }
        }
    }

    override fun put(index: Int, element: E) {
        while (true) {
            val curCore = core.value
            val curValue = curCore.get(index)
            val curCapacity = curCore.capacity

            if (curValue == null) {
                if (curCore.cas(index, null, State.Active(element))) {
                    return
                }
            } else {
                if (!(curValue is State.Fixed || curValue is State.Moved)) {
                    if (curCore.cas(index, curValue, State.Active(element))) {
                        return
                    }
                } else {
                    moveCore(curCore, curCapacity)
                }
            }
        }
    }

    override fun pushBack(element: E) {
        while (true) {
            val curCore = core.value
            val curSize = curCore.size
            val curCapacity = curCore.capacity
            if (curSize < curCapacity) {
                if (curCore.cas(curSize, null, State.Active(element))) {
                    curCore.casSize(curSize, curSize + 1)
                    return
                }
                curCore.casSize(curSize, curSize + 1)
            } else {
                moveCore(curCore, curCapacity)
            }
        }
    }

    private fun moveCore(curCore: Core<E>, curCapacity: Int) {
        val newCore = Core<E>(curCapacity * 2)
        newCore.casSize(0, curCapacity)
        curCore.next.compareAndSet(null, newCore)

        val nextCore = curCore.next.value!!
        for (i in 0 until curCapacity) {
            while (true) {
                val curValue = curCore.get(i)
                if (curValue is State.Fixed) {
                    nextCore.cas(i, null, State.Active(curValue.value))
                    curCore.cas(i, curValue, State.Moved)
                } else if (curValue is State.Active) {
                    curCore.cas(i, curValue, State.Fixed(curValue.value))
                } else {
                    break
                }
            }
        }
        core.compareAndSet(curCore, curCore.next.value!!)
    }

    override val size: Int get() = core.value.size
}

private sealed class State<out E> {
    class Active<E>(val value: E) : State<E>()
    class Fixed<E>(val value: E) : State<E>()
    object Moved : State<Nothing>()
}

private class Core<E>(
    capacity: Int,
) {
    private val array = atomicArrayOfNulls<State<E>?>(capacity)
    private val _size = atomic(0)

    val next = atomic<Core<E>?>(null)

    val size: Int get() = _size.value
    val capacity: Int get() = array.size

    @Suppress("UNCHECKED_CAST")
    fun get(index: Int): State<E>? {
        require(index in 0 until size) { "Index $index is out of bounds [0, $size)" }
        return array[index].value
    }

    fun cas(index: Int, expected: State<E>?, new: State<E>?): Boolean {
        return array[index].compareAndSet(expected, new)
    }

    fun casSize(expected: Int, new: Int): Boolean {
        return _size.compareAndSet(expected, new)
    }
}

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME