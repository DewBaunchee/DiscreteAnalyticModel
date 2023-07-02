import domain.System

fun main() {
    val system = System(0.5, 1, 0.0, 0.9)
    val stats = system.simulate(5_000_000)

    println("========== STATISTICS ==========")

    stats.apply {
        println()
        println("---------- STATES --------------")
        println()
        statesProbability.entries.forEach {
            println("   ${it.key} = ${it.value}")
        }

        println()
        println("---------- CALCULATIONS --------")
        println()

        println("   Refuse probability     (Potk) = $refuseProbability")
        println("   Block probability      (Pbl) = $blockProbability")
        println("   Average queue length   (Loch) = $averageQueueLength")
        println("   Average request count  (Lc) = $averageRequestCount")
        println("   Relative throughput    (Q) = $relativeThroughput")
        println("   Absolute throughput    (A) = $absoluteThroughput")
        println("   Average time in queue  (Woch) = $averageTimeInQueue")
        println("   Average time in system (Wc) = $averageTimeInSystem")

        channelCoefficients.forEachIndexed { index, value ->
            println("   Channel ${index + 1} coefficient  (Kkan${index + 1}) = $value")
        }
    }
}