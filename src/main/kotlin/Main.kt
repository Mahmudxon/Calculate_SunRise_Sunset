import java.util.*

fun main(args: Array<String>) {
    val timeZone = TimeZone.getTimeZone(TimeZone.getAvailableIDs().first())
    val sunCalculator = SunCalculator(2023, 4, 6, 42.3601, 71.0589,  timeZone)

    val sunrise = sunCalculator.getSunrise()
    val sunriseString = doubleToTimeString(sunrise)

    val sunset = sunCalculator.getSunset()
    val sunsetString = doubleToTimeString(sunset)

    println("Sunrise: $sunriseString ($sunrise)   && sunset: $sunsetString  ($sunset)")
}

fun doubleToTimeString(time: Double): String {
    val hours = (time % 24).toInt()
    val minutes = ((time - hours) * 60).toInt()
    val seconds = ((time - hours - minutes / 60.0) * 3600).toInt()
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
