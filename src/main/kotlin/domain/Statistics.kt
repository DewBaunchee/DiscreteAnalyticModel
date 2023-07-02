package domain

data class Statistics(
    val statesProbability: Map<System.State, Double>,
    val refuseProbability: Double, // Pотк
    val blockProbability: Double, // Pбл
    val averageQueueLength: Double, // Lоч
    val averageRequestCount: Double, // Lс
    val relativeThroughput: Double, // Q
    val absoluteThroughput: Double, // A
    val averageTimeInQueue: Double, // Wоч
    val averageTimeInSystem: Double, // Wс
    val channelCoefficients: List<Double>, // Ккан
) {

    companion object {

        fun from(
            tickCount: Int,
            channels: List<System.Channel>,
            requests: List<Request>,
            states: Map<System.State, Int>
        ): Statistics {
            val entered = requests.filter { !it.refusedOnSource }
            val enteredRefused = entered.filter { it.isRefused() }
            val refused = requests.filter { it.isRefused() }
            val handled = entered.filter { it.handled }

            val stateCount = states.values.sum()
            val statesProbability = states.mapValues { it.value.toDouble() / stateCount }
            return Statistics(
                statesProbability,
                refused.size / requests.size.toDouble(),
                0.0,
                statesProbability.entries.sumOf { it.key.queueSize * it.value },
                statesProbability.entries.sumOf { it.key.requestCount * it.value },
                1 - refused.size / requests.size.toDouble(),
                (entered.size - enteredRefused.size) / tickCount.toDouble(),
                entered.sumOf { it.ticksInQueue } / entered.size.toDouble(),
                handled.sumOf { it.ticksInSystem } / handled.size.toDouble(),
                channels.map { it.busyTickCount / tickCount.toDouble() }
            )
        }
    }
}