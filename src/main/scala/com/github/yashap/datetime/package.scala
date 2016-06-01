package com.github.yashap

import java.time.{ZoneOffset, LocalDateTime}
import java.time.temporal.ChronoUnit._

package object datetime {
  private val offset = ZoneOffset.of("Z")

  implicit class LocalDateTimeMethods(val d: LocalDateTime) {
    def isBetween(start: LocalDateTime, end: LocalDateTime): Boolean = {
      d.compareTo(start) >= 0 && d.compareTo(end) < 0
    }

    /**
     * Counts the number of weeks between two LocalDateTimes. Note, rounds up
     *
     * @param other the other LocalDateTime
     * @return the number of weeks until the other LocalDateTime
     */
    def weeksUntil(other: LocalDateTime): Long = {
      val thisInstant = d.toInstant(offset)
      val otherInstant = other.toInstant(offset)
      val nanosBetween: Long = NANOS.between(thisInstant, otherInstant)
      val sevenDaysInNanos: Double = (1L * 1000000000L * 60L * 60L * 24L * 7L).toDouble

      if (nanosBetween >= 0)
        (nanosBetween.toDouble / sevenDaysInNanos).ceil.toLong
      else
        (nanosBetween.toDouble / sevenDaysInNanos).floor.toLong
    }
  }

  implicit val localDateTimeOrdering: Ordering[LocalDateTime] =
    Ordering.fromLessThan((earlier, later) => earlier.isBefore(later))
}
