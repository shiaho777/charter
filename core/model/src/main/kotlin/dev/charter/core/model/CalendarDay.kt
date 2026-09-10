package dev.charter.core.model

/** One weekday column of the on-air timetable. */
data class CalendarDay(
    /** Bangumi weekday number: 1=Monday … 7=Sunday. */
    val weekday: Int,
    val items: List<Bangumi>,
) {
    val label: String
        get() = WEEKDAY_LABELS.getOrElse(weekday - 1) { "?" }

    companion object {
        private val WEEKDAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

        /** The user's today, mapped onto the Bangumi weekday numbering. */
        fun todayWeekday(): Int =
            java.time.LocalDate
                .now()
                .dayOfWeek.value
    }
}
