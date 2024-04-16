import kotlinx.atomicfu.*

class AtomicArrayNoAba<E>(size: Int, initialValue: E) {
    private val a = atomicArrayOfNulls<Any>(size)

    init {
        for (i in 0 until size) a[i].value = initialValue
    }

    fun get(index: Int): E {
        while (true) {
            when (val value = a[index].value) {
                is Descriptor -> value.complete()
                else -> return value as E
            }
        }
    }

    fun cas(index: Int, expected: E, update: E) =
        a[index].compareAndSet(expected, update)

    interface Descriptor {
        fun complete()
    }

    enum class Outcome { SUCCESS, FAILURE, UNDECIDED }

    private inner class Cas2Descriptor<E>(
        val index1: Int, val expected1: E, val update1: E,
        val index2: Int, val expected2: E, val update2: E
    ) : Descriptor {
        val outcome = atomic(Outcome.UNDECIDED)

        override fun complete() {
            casDescriptor(index2, expected2, this)
            when (a[index2].value) {
                this -> {
                    outcome.compareAndSet(Outcome.UNDECIDED, Outcome.SUCCESS)
                    a[index1].compareAndSet(this, update1)
                    a[index2].compareAndSet(this, update2)
                }

                else -> {
                    outcome.compareAndSet(Outcome.UNDECIDED, Outcome.FAILURE)
                    a[index1].compareAndSet(this, expected1)
                }
            }
        }
    }

    fun cas2(
        index1: Int, expected1: E, update1: E,
        index2: Int, expected2: E, update2: E
    ): Boolean {
        return when {
            index1 == index2 && expected1 is Int -> return cas(index1, expected1, (expected1 + 2) as E)
            index1 < index2 -> helper(index1, expected1, update1, index2, expected2, update2)
            else -> helper(index2, expected2, update2, index1, expected1, update1)
        }
    }

    private fun helper(
        index1: Int, expected1: E, update1: E,
        index2: Int, expected2: E, update2: E
    ): Boolean {
        val descriptor = Cas2Descriptor(index1, expected1, update1, index2, expected2, update2)
        return if (casDescriptor(index1, expected1, descriptor)) {
            descriptor.complete()
            descriptor.outcome.value == Outcome.SUCCESS
        } else {
            false
        }
    }

    private fun <E> casDescriptor(index: Int, expected: E, descriptor: Any): Boolean {
        while (true) {
            val elem = a[index].value
            if (elem != expected) return false
            if (a[index].compareAndSet(expected, descriptor)) return true
        }
    }
}