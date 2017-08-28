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


package com.razor.mvc.http;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * File content type enums
 *
 * @author Touchumind
 * @since 0.0.1
 */
public enum EContentType {

    TEXT("text/plain", "text", true, "txt", "text"),

    AUDIO_MPEG("audio/mpeg", "mpeg", false, "mpga", "mp2", "mp2a", "mp3", "m2a", "m3a"),

    AVI("video/x-msvideo", "avi", false, "avi"),

    MP4A("video/mp4", "mp4a", false, "mp4", "mp4a", "mpg4"),

    MP4V("video/mp4v-es", "mp4v", false, "mp4v"),

    WAV("audio/x-wav", "wav", false, "wav"),

    BITTORRENT("application/x-bittorrent", "bittorrent", false, "torrent"),

    JPEG("image/jpeg", "jpeg", true, "jpeg", "jpg", "jpe"),

    BMP("image/x-ms-bmp", "bmp", true, "bmp"),

    PNG("image/png", "png", true, "png"),

    GIF("image/gif", "gif", true, "gif"),

    ICO("image/x-ico", "ico", true, "ico"),

    TIFF("image/tiff", "tiff", true, "tiff", "tif"),

    WEBP("image/webp", "webp", true, "webp"),

    FLASH("application/x-shockwave-flash", "flash", false, "swf"),

    FLASH_VIDEO("video/x-flv", "flash-video", false, "flv"),

    GNU_INFO("text/x-info", "gnu-info", true, "info"),

    GZIP("application/x-gzip", "gzip", false, "gz"),

    HTML("text/html", "html", true, "html", "htm"),

    JAVASCRIPT("application/javascript", "javascript", true, "js"),

    CSS("text/css", "css", true, "css"),

    JSON("application/json", "json", true, "json"),

    JSON_ML("application/jsonml+json", "jsonml", true, "jsonml"),

    RSS("application/rss+xml", "rss", true, "rss"),

    PDF("application/pdf", "pdf", false, "pdf"),

    CSV("text/csv", "csv", false, "csv"),

    EPUB("application/epub+zip", "epub", false, "epub"),

    MICROSOFT_EXCEL("application/vnd.ms-excel", "excel", false, "xls", "xlm", "xla", "xlc", "xlt", "xlw", "xlsx"),

    MICROSOFT_WORD("application/msword", "word", false, "doc", "dot", "docx"),

    MICROSOFT_ACCESS("application/x-msaccess", "access", false, "mdb"),

    EMPTY("application/octet-stream", "other", true),;

    private final static Map<String, EContentType> mimeTypeMap = new HashMap<>();

    private final static Map<String, EContentType> extensionMap = new HashMap<>();

    static {
        for (EContentType type : values()) {
            if (type.mimeType != null) {
                mimeTypeMap.put(type.mimeType.toLowerCase(), type);
            }
            if (type.extensions != null) {
                for (String extension : type.extensions) {
                    extensionMap.put(extension, type);
                }
            }
        }
    }

    @Getter
    private final String mimeType;

    @Getter
    private final String shortName;

    @Getter
    private final boolean inline;

    @Getter
    private final String[] extensions;

    /**
     * Construct new instance
     *
     * @param mimeType mime type, e.g `text/plain`
     * @param shortName mime type short name, e.g `text`
     * @param inline content-disposition property, true for `inline`, others such as `attachment` treat as false
     * @param extensions possible file extensions for this mime type
     */
    private EContentType(String mimeType, String shortName, boolean inline, String... extensions) {

        this.mimeType = mimeType;
        this.shortName = shortName;
        this.inline = inline;
        this.extensions = extensions;
    }

    public static EContentType fromMimeType(String mimeType) {

        if (mimeType != null) {
            mimeType = mimeType.toLowerCase();
        }
        EContentType type = mimeTypeMap.get(mimeType);

        if (type == null) {
            return EMPTY;
        }

        return type;
    }

    public static EContentType fromFileExtension(String extension) {

        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        EContentType type = extensionMap.get(extension.toLowerCase());

        if (type == null) {
            return EMPTY;
        }

        return type;
    }
}
