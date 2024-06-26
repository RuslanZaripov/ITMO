package dijkstra

import kotlinx.atomicfu.atomic
import java.util.concurrent.Phaser
import kotlin.Comparator
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }

private val activeNodes = atomic(0)

// Returns `Integer.MAX_VALUE` if a path has not been found.
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = MultiQueue(workers, NODE_DISTANCE_COMPARATOR) // TODO replace me with a multi-queue based PQ!
    q.add(start)
    activeNodes.getAndIncrement()
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    repeat(workers) {
        thread {
            while (true) {
                // TODO Write the required algorithm here,
                // TODO break from this loop when there is no more node to process.
                // TODO Be careful, "empty queue" != "all nodes are processed".
                val cur: Node = synchronized(q) { q.poll() } ?: if (isWorkDone()) break else continue
                for (e in cur.outgoingEdges) {
                    while (true) {
                        val oldDist = e.to.distance
                        val newDist = cur.distance + e.weight
                        if (oldDist <= newDist) break
                        else {
                            if (e.to.casDistance(oldDist, newDist)) {
                                q.add(e.to)
                                activeNodes.getAndIncrement()
                                break
                            }
                        }
                    }
                }
                activeNodes.getAndDecrement()
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}

private fun isWorkDone() = activeNodes.value == 0