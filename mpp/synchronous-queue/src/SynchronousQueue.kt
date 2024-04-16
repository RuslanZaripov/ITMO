import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * An element is transferred from sender to receiver only when [send] and [receive]
 * invocations meet in time (rendezvous), so [send] suspends until another coroutine
 * invokes [receive] and [receive] suspends until another coroutine invokes [send].
 */
class SynchronousQueue<E> {
    private val head: AtomicRef<Node>
    private val tail: AtomicRef<Node>

    init {
        val dummy = DummyNode()
        head = atomic(dummy)
        tail = atomic(dummy)
    }

    /**
     * Sends the specified [element] to this channel, suspending if there is no waiting
     * [receive] invocation on this channel.
     */
    suspend fun send(element: E) {
        while (true) {
            val curHead = head.value
            val curTail = tail.value
            if (isEmpty() || curTail is SendNode<*>) {
                val nextTail = curTail.next.value
                if (curTail != tail.value) continue
                if (nextTail != null) {
                    tail.compareAndSet(curTail, nextTail)
                } else {
                    val res = suspendCoroutine sc@{ cont ->
                        val newTail = SendNode(element, cont)
                        if (curTail.next.compareAndSet(null, newTail)) {
                            tail.compareAndSet(curTail, newTail)
                            return@sc
                        }
                        cont.resume(RETRY)
                    }
                    if (res === RETRY) continue
                    return
                }
            } else {
                val nextHead = curHead.next.value as? ReceiveNode ?: continue
                if (curTail !== tail.value || curHead !== head.value) continue
                if (head.compareAndSet(curHead, nextHead)) {
                    nextHead.cont.resume(element!!)
                    return
                }
            }
        }
    }

    /**
     * Retrieves and removes an element from this channel if there is a waiting [send] invocation on it,
     * suspends the caller if this channel is empty.
     */
    suspend fun receive(): E {
        while (true) {
            val curHead = head.value
            val curTail = tail.value
            if (isEmpty() || curTail is ReceiveNode) {
                val nextTail = curTail.next.value
                if (curTail != tail.value) continue
                if (nextTail != null) {
                    tail.compareAndSet(curTail, nextTail)
                } else {
                    val res = suspendCoroutine sc@{ cont ->
                        val newTail = ReceiveNode(cont)
                        if (curTail.next.compareAndSet(null, newTail)) {
                            tail.compareAndSet(curTail, newTail)
                            return@sc
                        }
                        cont.resume(RETRY)
                    }
                    if (res === RETRY) continue
                    return res as E
                }
            } else {
                val nextHead = curHead.next.value as? SendNode<E> ?: continue
                if (curTail !== tail.value || curHead !== head.value) continue
                if (head.compareAndSet(curHead, nextHead)) {
                    nextHead.cont.resume(Unit)
                    return nextHead.element
                }
            }
        }
    }

    private fun isEmpty(): Boolean {
        return head.value === tail.value
    }
}

private open class Node {
    val next = atomic<Node?>(null)
}

private class SendNode<E>(val element: E, val cont: Continuation<Any>) : Node()

private class ReceiveNode(val cont: Continuation<Any>) : Node()

private class DummyNode : Node()

private object RETRY