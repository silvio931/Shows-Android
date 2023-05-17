package com.silvio_mihalic.shows.utils

class NetworkChecker {

    companion object {
        private const val INTERNET_CHECK_COMMAND = "ping -c 1 google.com"
    }

    /**
     * Try to ping google, on success return true, else return false
     */
    fun internetIsConnected(): Boolean {
        return try {
            val command = INTERNET_CHECK_COMMAND
            Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
