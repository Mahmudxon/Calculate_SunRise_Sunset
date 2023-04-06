import java.util.*

class SunCalculator {
    private val calendar = Calendar.getInstance()
    private val timeZone: TimeZone
    private val longitude: Double
    private val latitude: Double
    private val zenith = 90.83333

    constructor(year: Int, month: Int, day: Int, longitude: Double, latitude: Double, timeZone: TimeZone) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.timeZone = timeZone
        this.longitude = longitude
        this.latitude = latitude
        this.timeZone = timeZone
    }

    fun getSunrise(): Double {
        val D = getDaysSince2000()
        val L = getGeoLocation()
        val G = getSunriseSunsetTime(D, L, true)
        val t = getTimeUTC(G)
        return t + (timeZone.rawOffset / 3600000.0)
    }

    fun getSunset(): Double {
        val D = getDaysSince2000()
        val L = getGeoLocation()
        val G = getSunriseSunsetTime(D, L, false)
        val t = getTimeUTC(G)
        return t + (timeZone.rawOffset / 3600000.0)
    }

    private fun getDaysSince2000(): Int {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return 367 * year - (7 * (year + (month + 9) / 12)) / 4 + (275 * month) / 9 + day - 730530
    }

    private fun getGeoLocation(): Double {
        return (longitude / 360) + (calendar.get(Calendar.ERA) - 1) * 0.5
    }

    private fun getSunriseSunsetTime(D: Int, L: Double, isSunrise: Boolean): Double {
        val m = getSolarMeanAnomaly(D)
        val quadrant = getQuadrant(L)
        val rA = getRightAscension(m)
        val sinDec = getSinDeclination(m)
        val cosDec = getCosDeclination(sinDec)
        val cosH = getCosHourAngle(zenith, sinDec, latitude, cosDec, isSunrise)

        if (cosH > 1) {
            return Double.NaN
        }

        val H = getHourAngle(cosH, isSunrise)
        val T = getLocalMeanTime(H, rA, L, quadrant)
        return T
    }

    private fun getSolarMeanAnomaly(D: Int): Double {
        return (357.5291 + 0.98560028 * D) % 360
    }

    private fun getQuadrant(L: Double): Double {
        return Math.floor(L / 90) * 90
    }

    private fun getRightAscension(M: Double): Double {
        val RA = (Math.atan(0.91764 * Math.tan(Math.toRadians(M))) / Math.PI * 180)
        return (RA + 360) % 360
    }

    private fun getSinDeclination(M: Double): Double {
        return 0.39779 * Math.sin(Math.toRadians(M))
    }

    private fun getCosDeclination(sinDec: Double): Double {
        return Math.cos(Math.asin(sinDec))
    }

    private fun getCosHourAngle(
        zenith: Double,
        sinDec: Double,
        latitude: Double,
        cosDec: Double,
        isSunrise: Boolean
    ): Double {
        val h = Math.acos(
            (Math.cos(Math.toRadians(zenith)) - sinDec * Math.sin(Math.toRadians(latitude))) / (cosDec * Math.cos(
                Math.toRadians(
                    latitude
                )
            ))
        )
        return if (isSunrise) h else 2 * Math.PI - h
    }

    private fun getHourAngle(cosH: Double, isSunrise: Boolean): Double {
        return if (isSunrise) Math.acos(cosH) else 2 * Math.PI - Math.acos(cosH)
    }

    private fun getLocalMeanTime(H: Double, RA: Double, L: Double, Lquadrant: Double): Double {
        val T = H + RA - (0.06571 * (L - Lquadrant)) - 6.622
        return (T + 24) % 24
    }

    private fun getTimeUTC(T: Double): Double {
        val UT = T - longitude / 15
        return (UT + 24) % 24
    }
}
