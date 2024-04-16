package dijkstra

import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.locks.ReentrantLock

fun getDistinctIndices(n: Int): Pair<Int, Int> {
    val r = ThreadLocalRandom.current()
    var p = r.nextInt(n) to r.nextInt(n)
    while (p.first == p.second) {
        p = r.nextInt(n) to r.nextInt(n)
    }
    return p
}

class MultiQueue<E>(threadAmount: Int, private val comparator: Comparator<E>) {
    private val queueAmount = threadAmount * 4
    private val queues = List(queueAmount) { PriorityQueue(comparator) }
    private val locks = List(queueAmount) { ReentrantLock(true) }

    fun add(x: E) {
        while (true) {
            val index = ThreadLocalRandom.current().nextInt(queueAmount)
            val queue = queues[index]
            if (!locks[index].tryLock()) continue
            queue.add(x)
            locks[index].unlock()
            return
        }
    }

    fun poll(): E? {
        while (true) {
            val (index1, index2) = getDistinctIndices(queueAmount)
            if (!locks[index1].tryLock()) continue
            if (!locks[index2].tryLock()) {
                locks[index1].unlock()
                continue
            }
            try {
                val e1 = queues[index1].peek()
                val e2 = queues[index2].peek()
                if (e1 == null && e2 == null) return null
                val index = when {
                    e1 == null -> index2
                    e2 == null -> index1
                    else -> {
                        when {
                            comparator.compare(e1, e2) < 0 -> index1
                            else -> index2
                        }
                    }
                }
                return queues[index].poll()
            } finally {
                locks[index1].unlock()
                locks[index2].unlock()
            }
        }
    }
}