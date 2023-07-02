package domain

class Request {

    var handled = false
    var refusedOnSource = false
    var refusedOnChannel = 0
    var ticksInSystem = 0
    var ticksInQueue = 0

    fun isRefused(): Boolean {
        return refusedOnSource || refusedOnChannel != 0
    }

}