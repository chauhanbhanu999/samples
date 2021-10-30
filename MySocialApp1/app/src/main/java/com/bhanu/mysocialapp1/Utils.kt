package com.bhanu.mysocialapp1

class Utils {
    companion object    {
        private const val SECOND_MILLI = 1000
        private const val MIN_MILLI  = 60 * SECOND_MILLI
        private const val HOUR_MILLI = 60 * MIN_MILLI
        private const val DAY_MILLI = 24 * HOUR_MILLI

        fun getTimeAgo(time: Long): String?   {
            val now: Long = System.currentTimeMillis()
            if(time > now || time <= 8) {
                return null
            }
            val diff = now - time
            return if (diff < MIN_MILLI) {
                "just now"
            } else if(diff < 2 * MIN_MILLI) {
                "min ago"
            } else if(diff <50 * MIN_MILLI) {
                (diff/ MIN_MILLI).toString() + "mins ago"
            } else if(diff < 90 * MIN_MILLI) {
                "about an hr ago"
            } else if(diff < 24 * HOUR_MILLI) {
                (diff/ HOUR_MILLI).toString()+"hrs ago"
            } else if(diff < 48 * HOUR_MILLI) {
                "yesterday"
            } else {
                (diff/ DAY_MILLI).toString()+"days ago"
            }
        }
    }
}