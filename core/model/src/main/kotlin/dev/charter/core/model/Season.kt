package dev.charter.core.model

/**
 * A broadcast season (quarter). The Bangumi search API filters by air date
 * range, so a season is exactly "an air_date window" — the popular page and
 * the season picker both speak in these terms.
 */
data class Season(
    val year: Int,
    /** 1=冬 2=春 3=夏 4=秋 */
    val quarter: Int,
) {
    init {
        require(quarter in FIRST_QUARTER..LAST_QUARTER) { "quarter must be $FIRST_QUARTER..$LAST_QUARTER" }
    }

    /** Inclusive ISO start date of the season. */
    val startDate: String
        get() = "%04d-%02d-01".format(year, startMonth)

    /** Exclusive ISO end date (start of the next season). */
    val endDate: String
        get() =
            if (quarter == LAST_QUARTER) {
                "%04d-01-01".format(year + 1)
            } else {
                "%04d-%02d-01".format(year, startMonth + MONTHS_PER_QUARTER)
            }

    val label: String
        get() = "${year}年${QUARTER_NAMES[quarter - FIRST_QUARTER]}季"

    /** The previous season — for "上一季" stepping in the picker. */
    fun previous(): Season = if (quarter == FIRST_QUARTER) Season(year - 1, LAST_QUARTER) else Season(year, quarter - 1)

    private val startMonth: Int
        get() = (quarter - FIRST_QUARTER) * MONTHS_PER_QUARTER + FIRST_QUARTER

    companion object {
        private val QUARTER_NAMES = listOf("冬", "春", "夏", "秋")
        private const val FIRST_QUARTER = 1
        private const val LAST_QUARTER = 4
        private const val MONTHS_PER_QUARTER = 3

        fun current(today: java.time.LocalDate = java.time.LocalDate.now()): Season =
            Season(today.year, (today.monthValue - FIRST_QUARTER) / MONTHS_PER_QUARTER + FIRST_QUARTER)

        /** [count] most recent seasons, newest first — the picker's data. */
        fun recent(
            count: Int,
            today: java.time.LocalDate = java.time.LocalDate.now(),
        ): List<Season> {
            var season = current(today)
            return List(count) {
                season.also { season = season.previous() }
            }
        }
    }
}
