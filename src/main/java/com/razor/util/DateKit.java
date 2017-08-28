/**
 * Copyright (c) 2017, Touchumind<chinash2010@gmail.com>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.razor.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/**
 * Date utils
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class DateKit {

    /**
     * Support parse date string in http headers
     *
     * @param dateString date string
     * @return parsed local date, null on error
     */
    public static Date dateFromGmt(String dateString) {

        return dateFromString(dateString, "EEE, dd MMM yyyy HH:mm:ss zzz");
    }

    /**
     * Parse date from string with specified format
     *
     * @param dateString date string
     * @param pattern date parse pattern
     * @return parse local date, null on error
     */
    public static Date dateFromString(String dateString, String pattern) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.US);
        LocalDateTime formatted = LocalDateTime.parse(dateString, formatter);

        return Date.from(formatted.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert current datetime to gmt datetime string
     *
     * @return gmt datetime string
     */
    public static String getGmtDateString() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

        return formatter.format(LocalDateTime.now().atZone(ZoneId.of("GMT")));
    }

    /**
     * Convert date to http application compatible string
     *
     * @param date date
     * @return gmt date string
     */
    public static String getGmtDateString(Date date) {

        return getZoneDateString(date, "EEE, dd MMM yyyy HH:mm:ss zzz", ZoneId.of("GMT"));
    }

    /**
     * Convert date to http application compatible string
     *
     * @param date local date
     * @return gmt date string
     */
    public static String getGmtDateString(LocalDateTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        return formatter.format(date.atZone(ZoneId.of("GMT")));
    }

    /**
     * Convert date to string with specified format
     * @param date date
     * @param pattern date pattern
     * @return date string
     */
    public static String getDateString(Date date, String pattern) {

        return getZoneDateString(date, pattern, ZoneId.systemDefault());
    }

    /**
     * Convert date to string with specified format and time zone
     *
     * @param date date
     * @param pattern date pattern
     * @return date string
     */
    public static String getZoneDateString(Date date, String pattern, ZoneId zoneId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.US);

        return formatter.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).atZone(zoneId));
    }
}
