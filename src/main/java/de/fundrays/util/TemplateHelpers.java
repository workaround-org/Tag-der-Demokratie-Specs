package de.fundrays.util;

import io.quarkus.qute.TemplateExtension;

import java.time.Instant;
import java.time.ZoneOffset;

public class TemplateHelpers {

    /** {someAmount.euros()} → "12.34 €" */
    @TemplateExtension
    static String euros(Long cents) {
        return String.format("%.2f €", cents / 100.0);
    }

    /** {someAmount.eurosAmount()} → "12.34" (no symbol, for form inputs) */
    @TemplateExtension
    static String eurosAmount(Long cents) {
        return String.format("%.2f", cents / 100.0);
    }

    /** {someInstant.isoDate()} → "2026-01-15" or "" if null */
    @TemplateExtension
    static String isoDate(Instant instant) {
        if (instant == null) return "";
        return instant.atZone(ZoneOffset.UTC).toLocalDate().toString();
    }

    /** {fmt:percent(raised, goal)} → 0-100 int, capped at 100 */
    @TemplateExtension(namespace = "fmt")
    static int percent(long part, long total) {
        if (total == 0) return 0;
        return (int) Math.min(100L, part * 100L / total);
    }
}
