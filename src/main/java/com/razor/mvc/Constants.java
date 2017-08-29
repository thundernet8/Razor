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


package com.razor.mvc;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Razor global constants
 *
 * @author Touchumind
 * @since 0.0.1
 */
public interface Constants {

    // Package
    String PACKAGE_NAME = "Razor";

    // Version
    String VERSION = "0.0.1-SNAPSHOT";

    // System
    String CLASS_PATH = new File(Constants.class.getResource("/").getPath()).getAbsolutePath();

    // Server default constants
    String DEFAULT_SERVER_HOST = "0.0.0.0";
    int DEFAULT_SERVER_PORT = 8088;
    String DEFAULT_WEB_ROOT_DIR = "web";
    List<String> DEFAULT_STATICS = Arrays.asList("/favicon.ico", "/robots.txt", "/sitemap.xml");
    List<String> DEFAULT_INDEX_FILES = Arrays.asList("index.html", "index.htm");
    boolean DEFAULT_SSL_ENABLE = false;

    // Http constants
    String DEFAULT_CHARSET = "UTF-8";
    Integer DEFAULT_HTTP_CACHE_SECONDS = 3600;
    String CONTENT_TYPE_HTML = "text/html; charset=" + DEFAULT_CHARSET;
    String CONTENT_TYPE_JSON = "application/json; charset=" + DEFAULT_CHARSET;
    String CONTENT_TYPE_TEXT = "text/plain; charset=" + DEFAULT_CHARSET;

    // Env keys
    String ENV_KEY_SERVER_HOST = "razor.server.host";
    String ENV_KEY_SERVER_PORT = "razor.server.port";
    String ENV_KEY_SSL = "razor.server.ssl";
    String ENV_KEY_WEB_ROOT_DIR = "razor.web.root";
    String ENV_KEY_INDEX_FILES = "razor.web.indexs";
    String ENV_KEY_HTTP_CACHE_SECONDS = "razor.web.http.cache.seconds";
}
