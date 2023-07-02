package domain

import java.util.*
import kotlin.random.Random

fun Boolean.toInt() = if (this) 1 else 0

class System(
    emitProbability: Double,
    queueCapacity: Int,
    channel1Probability: Double,
    channel2Probability: Double
) {

    private val state get() = State(queue.size, listOf(channel1.isBusy(), channel2.isBusy()))

    private val source = Source(emitProbability)
    private var queue = Queue(queueCapacity)
    private var channel1 = Channel(channel1Probability)
    private var channel2 = Channel(channel2Probability)

    private fun tick(): Pair<State, Request?> {
        channel2.tick()?.also {
            it.handled = true
        }
        channel1.tick()?.also {
            if (channel2.isFree()) {
                channel2.handle(it)
                return@also
            }
            it.refusedOnChannel = 1
        }
        queue.tick()
        if (channel1.isFree()) channel1.handle(queue.dequeue())

        val emitted = source.tick()
        if (emitted != null) {
            if (channel1.isFree()) {
                channel1.handle(emitted)
            } else if (!queue.enqueue(emitted)) {
                emitted.refusedOnSource = true
            }
        }

        return state to emitted
    }

    fun simulate(tickCount: Int): Statistics {
        val requests = mutableListOf<Request>()
        val states = mutableMapOf<State, Int>()
        for (i in 0 until tickCount) {
            val tickResult = tick()

            states.compute(tickResult.first) { _, count ->
                if (count == null) return@compute 1
                return@compute count + 1
            }

            if (tickResult.second != null) requests.add(tickResult.second!!)
        }

        return Statistics.from(tickCount, listOf(channel1, channel2), requests.toList(), states.toMap())
    }

    class State(
        val queueSize: Int,
        val channelBusiness: List<Boolean>
    ) {

        val requestCount get() = queueSize + channelBusiness.sumOf { it.toInt() }

        override fun hashCode(): Int {
            return Objects.hash(queueSize, channelBusiness)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as State

            if (queueSize != other.queueSize) return false
            if (channelBusiness != other.channelBusiness) return false

            return true
        }

        private fun name(key: String): String {
            return when (key) {
                "000" -> "1"
                "010" -> "2"
                "001" -> "3"
                "011" -> "4"
                "110" -> "5"
                "111" -> "6"
                else -> "UNKNOWN"
            }
        }

        override fun toString(): String {
            val key = "" + queueSize + channelBusiness.joinToString("") { it.toInt().toString() }
            return "P-$key (${name(key)})"
        }
    }

    class Source(private val probability: Double) {

        fun tick(): Request? {
            return if (Random.nextDouble() > probability) Request() else null
        }
    }

    class Queue(private val maxCapacity: Int) {

        private val list = LinkedList<Request>()

        val size get() = list.size

        fun enqueue(request: Request?): Boolean {
            if (request == null) throw Exception()
            if (isFull()) return false
            list.add(request)
            return true
        }

        fun dequeue(): Request? {
            return list.pollFirst()
        }

        fun tick() {
            list.forEach { it.ticksInQueue++; it.ticksInSystem++ }
        }

        private fun isFull(): Boolean {
            return list.size >= maxCapacity
        }
    }

    class Channel(private val probability: Double) {

        private var request: Request? = null
        var busyTickCount = 0
            private set

        fun tick(): Request? {
            if (request != null) {
                request!!.ticksInSystem++
                busyTickCount++
            }
            return (if (Random.nextDouble() > probability) request else null)?.also { request = null }
        }

        fun isFree(): Boolean {
            return request == null
        }

        fun isBusy(): Boolean {
            return !isFree()
        }

        fun handle(request: Request?) {
            this.request = request
        }
    }
}