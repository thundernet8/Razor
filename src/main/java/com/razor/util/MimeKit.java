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

import com.razor.mvc.http.EContentType;

import java.io.File;
import java.io.InputStream;

/**
 * Mime Types helper
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class MimeKit {

    /**
     * Search mime type through file name
     *
     * @param name file name
     * @return mime type
     */
    public static String of(String name) {
        name = name.toLowerCase();

        // whole name searching
        EContentType type = EContentType.fromFileExtension(name);
        if (type != EContentType.EMPTY) {
            return type.getMimeType();
        }

        // .ext searching
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return null;
        }
        return ofExt(name.substring(index + 1));
    }

    /**
     * Search mime info through file name
     *
     * @param name file name
     * @return object including mime detail info
     */
    public static EContentType detailOf(String name) {
        name = name.toLowerCase();

        // whole name searching
        EContentType type = EContentType.fromFileExtension(name);
        if (type != EContentType.EMPTY) {
            return type;
        }

        // .ext searching
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return null;
        }
        return detailOfExt(name.substring(index + 1));
    }

    /**
     * Search mime type through extension
     *
     * @param ext file extension, e.g `.jpg` or `jpg`
     * @return mime type
     */
    public static String ofExt(String ext) {

        EContentType type = EContentType.fromFileExtension(ext);
        if (type == EContentType.EMPTY) {
            return null;
        } else {
            return type.getMimeType();
        }
    }

    /**
     * Search mime info through extension
     *
     * @param ext file extension, e.g `.jpg` or `jpg`
     * @return mime type
     */
    public static EContentType detailOfExt(String ext) {

        EContentType type = EContentType.fromFileExtension(ext);
        if (type == EContentType.EMPTY) {
            return null;
        } else {
            return type;
        }
    }

    public static String ofStream(InputStream stream) {

        // TODO
        return null;
    }

    public static String ofFile(File file) {

        // TODO
        return null;
    }
}
