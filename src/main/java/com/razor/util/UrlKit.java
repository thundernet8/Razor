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

import com.razor.mvc.http.ContentType;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Url related util
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class UrlKit {

    /**
     * Remove queries part of a url
     *
     * @param url url
     * @return url without queries
     */
    public static String purgeUrlQueries(String url) {
        // TODO
        return url;
    }

    /**
     * Parse url queries
     *
     * @param url url
     * @return url queries
     */
    public static Map<String, List<String>> parseQueries(String url) {

        return new QueryStringDecoder(url).parameters();
    }

    public static boolean isStaticFile(Set<String> statics, String url) {

        if (url.endsWith("/") && !url.equals("/")) {
            // treat as directory

            return true;
        }

        Optional<String> result = statics.stream().filter(s -> s.equals(url) || url.startsWith(s)).findFirst();

        if (result.isPresent()) {

            return true;
        }

        // extension match
        int index = url.lastIndexOf('.');
        if (index > 0 && index != url.length() - 1) {

            String ext = url.substring(index + 1);

            if (ContentType.fromFileExtension(ext) != ContentType.EMPTY) {

                return true;
            }
        }

        return false;
    }
}
