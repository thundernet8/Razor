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

    TEXT("text/plain", "text", "txt", "text"),

    AUDIO_MPEG("audio/mpeg", "mpeg", "mpga", "mp2", "mp2a", "mp3", "m2a", "m3a"),

    AVI("video/x-msvideo", "avi", "avi"),

    MP4A("video/mp4", "mp4a", "mp4", "mp4a", "mpg4"),

    MP4V("video/mp4v-es", "mp4v", "mp4v"),

    WAV("audio/x-wav", "wav", "wav"),

    BITTORRENT("application/x-bittorrent", "bittorrent", "torrent"),

    JPEG("image/jpeg", "jpeg", "jpeg", "jpg", "jpe"),

    BMP("image/x-ms-bmp", "bmp", "bmp"),

    PNG("image/png", "png", "png"),

    GIF("image/gif", "gif", "gif"),

    ICO("image/x-ico", "ico", "ico"),

    TIFF("image/tiff", "tiff", "tiff", "tif"),

    WEBP("image/webp", "webp", "webp"),

    FLASH("application/x-shockwave-flash", "flash", "swf"),

    FLASH_VIDEO("video/x-flv", "flash-video", "flv"),

    GNU_INFO("text/x-info", "gnu-info", "info"),

    GZIP("application/x-gzip", "gzip", "gz"),

    HTML("text/html", "html", "html", "htm"),

    JAVASCRIPT("application/javascript", "javascript", "js"),

    CSS("text/css", "css", "css"),

    JSON("application/json", "json", "json"),

    JSON_ML("application/jsonml+json", "jsonml", "jsonml"),

    RSS("application/rss+xml", "rss", "rss"),

    PDF("application/pdf", "pdf", "pdf"),

    CSV("text/csv", "csv", "csv"),

    EPUB("application/epub+zip", "epub", "epub"),

    MICROSOFT_EXCEL("application/vnd.ms-excel", "excel", "xls", "xlm", "xla", "xlc", "xlt", "xlw", "xlsx"),

    MICROSOFT_WORD("application/msword", "word", "doc", "dot", "docx"),

    MICROSOFT_ACCESS("application/x-msaccess", "access", "mdb"),

    MICROSOFT_OFFICE("application/vnd.openxmlformats-officedocument", "office"),

    EMPTY("application/octet-stream", "other"),;

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
    private final String[] extensions;

    private EContentType(String mimeType, String shortName, String... extensions) {

        this.mimeType = mimeType;
        this.shortName = shortName;
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
