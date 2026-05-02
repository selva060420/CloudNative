package com.interview.java8plus;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Demonstrates: java.time API — LocalDate, Instant, Duration, ZonedDateTime, formatting.
 */
public class DateTimeDemo {

    public static void main(String[] args) {
        // LocalDate — date without time/timezone
        LocalDate today = LocalDate.now();
        LocalDate release = LocalDate.of(2024, 3, 19); // Java 22 release
        System.out.println("Today: " + today);
        System.out.println("Days since Java 22: " + ChronoUnit.DAYS.between(release, today));

        // LocalDateTime — date + time, no timezone
        LocalDateTime meeting = LocalDateTime.of(2024, 6, 15, 14, 30);
        System.out.println("Meeting: " + meeting);

        // Instant — machine timestamp (UTC epoch)
        Instant now = Instant.now();
        Instant later = now.plus(Duration.ofHours(2));
        System.out.println("Now (UTC): " + now);
        System.out.println("2h later: " + later);

        // ZonedDateTime — full date-time with timezone
        ZonedDateTime stockholm = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));
        ZonedDateTime newYork = stockholm.withZoneSameInstant(ZoneId.of("America/New_York"));
        System.out.println("Stockholm: " + stockholm.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")));
        System.out.println("New York:  " + newYork.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z")));

        // Duration & Period
        Duration duration = Duration.between(now, later);
        System.out.println("Duration: " + duration.toMinutes() + " minutes");

        Period period = Period.between(release, today);
        System.out.println("Period since Java 22: " + period.getYears() + "y " + period.getMonths() + "m " + period.getDays() + "d");

        // Parsing
        LocalDate parsed = LocalDate.parse("2024-01-15", DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println("Parsed: " + parsed);

        // Custom formatting
        String formatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        System.out.println("Custom format: " + formatted);
    }
}
