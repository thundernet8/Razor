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


package com.fedepot.server;

import com.fedepot.Razor;
import com.fedepot.exception.RazorException;
import com.fedepot.mvc.Constants;
import com.fedepot.mvc.http.ContentType;
import com.fedepot.mvc.http.HttpMethod;
import com.fedepot.mvc.http.Request;
import com.fedepot.mvc.http.Response;
import com.fedepot.util.DateKit;
import com.fedepot.util.MimeKit;
import com.fedepot.env.Env;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.fedepot.mvc.Constants.*;
import static com.fedepot.mvc.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Static files handler
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class StaticFileHandler implements IRequestHandler<Boolean> {

    private Razor razor;

    StaticFileHandler(Razor razor) {

        this.razor = razor;
    }

    @Override
    public Boolean handle(ChannelHandlerContext ctx, Request request, Response response) throws RazorException {

        if (!request.method().equals(HttpMethod.GET)) {

            response.sendError(METHOD_NOT_ALLOWED);
            return false;
        }

        String path = request.path();

        // resolve statics map
        Map<String, String> staticsMap = razor.getStaticsMap();
        for (String routePrefix : staticsMap.keySet()) {

            if (path.startsWith(routePrefix)) {

                path = staticsMap.get(routePrefix).concat(path.substring(routePrefix.length()));
                break;
            }
        }


        if (path.toUpperCase().startsWith("/WEB-INF")) {

            response.sendError(FORBIDDEN);
            return false;
        }

        // security check, more TODO
        if (path.contains("..") || path.contains(".".concat(File.separator)) || path.contains(File.separator.concat(".")) || path.charAt(0) == '.' || path.charAt(path.length() - 1) == '.') {

            response.sendError(NOT_FOUND);

            return false;
        }

        Env env = razor.getEnv();
        String webRoot = env.get(ENV_RT_KEY_WEB_ROOT_ABS_PATH, "");
        String absPath = webRoot.concat(path);

        boolean useOuterWebRootDir = env.getBool(ENV_KEY_USE_OUTER_WEB_ROOT, false) && env.get(ENV_KEY_WEB_ROOT_FOLDER).isPresent();

        if (!useOuterWebRootDir) {

            URL url = getClass().getResource(File.separator.concat(env.get(ENV_KEY_WEB_ROOT_FOLDER, DEFAULT_WEB_ROOT_FOLDER)).concat(path));

            if (url == null) {

                response.sendError(NOT_FOUND);

                return false;
            }

            if (url.toString().startsWith("file:/") || url.toString().startsWith("jar:file:/")) {

                File tmpFile = new File(Constants.ROOT_DIR.concat(File.separator).concat("cache").concat(path));

                if (!tmpFile.exists()) {

                    // for resources packaged in one jar file, only direct files will be handled, sub folder index files will not be checked
                    try {

                        InputStream openStream = url.openStream();
                        int contentLength = openStream.available();
                        byte[] binaryData = new byte[contentLength];
                        openStream.read(binaryData);

                        FileUtils.copyURLToFile(url, tmpFile);

                        setHeaders(tmpFile, request, response);
                        response.end(binaryData);

                        openStream.close();

                        return true;
                    } catch (Exception e) {

                        response.interanlError();
                        return false;
                    }
                } else {

                    absPath = tmpFile.getAbsolutePath();
                }
            }
        }


        File file = new File(absPath);

        if (!file.exists() || file.isHidden()) {

            response.sendError(NOT_FOUND);

            return false;
        }

        if (!file.isFile() && !file.isDirectory()) {

            response.sendError(FORBIDDEN);

            return false;
        }

        if (file.isDirectory()) {

            // find directory index file
            Set<String> indexs = new HashSet<>((List<String>)env.getObject(ENV_KEY_INDEX_FILES).orElse(DEFAULT_INDEX_FILES));

            File indexFile = null;

            for (String index : indexs) {

                String filePath = absPath.endsWith(File.separator) ? absPath.concat(index) : absPath.concat(File.separator).concat(index);
                File tmpFile = new File(filePath);

                if (tmpFile.exists() && tmpFile.isFile() && !tmpFile.isHidden()) {

                    indexFile = tmpFile;
                    break;
                }
            }

            if (indexFile == null) {

                response.sendError(NOT_FOUND);
                return false;
            }

            file = indexFile;
        }


        // check if cache and 304
        if (checkCache(file, request, response)) {

            return false;
        }

        RandomAccessFile raf;
        try {

            raf = new RandomAccessFile(file, "r");
            long length = raf.length();

            setHeaders(file, request, response);

            response.sendFile(raf, length);

            return true;
        } catch (FileNotFoundException e) {

            log.error("Static file not found: {}", file.getPath());

            response.sendError(NOT_FOUND);
            throw new RazorException(e);
        } catch (IOException e) {
            log.error("Static file IO exception: {}", file.getPath());

            response.sendError(INTERNAL_SERVER_ERROR);
            throw new RazorException(e);
        }

    }

    /**
     * Check 304 status
     *
     * @param file request file
     * @param request request object
     * @param response response object
     * @return true for not modified
     */
    private boolean checkCache(File file, Request request, Response response) {

        String ifMdf = request.get(IF_MODIFIED_SINCE);
        String cacheControl = request.get(CACHE_CONTROL);

        if (ifMdf == null || ifMdf.isEmpty() || Arrays.asList("max-age=0", "no-cache", "no-store").contains(cacheControl.toLowerCase())) {

            return false;
        }

        Date ifMdfSinceDate = DateKit.dateFromGmt(ifMdf);
        long ifMdfSinceSecs = ifMdfSinceDate.getTime() / 1000;
        long fileLastMdfSecs = file.lastModified() / 1000;
        if ((fileLastMdfSecs < 0 && ifMdfSinceSecs <= Instant.now().getEpochSecond()) || fileLastMdfSecs == ifMdfSinceSecs) {

            response.status(304).end();
            return true;
        }

        return false;
    }

    private void setHeaders(File file, Request request, Response response) {

        String filename = file.getName();
        response.setDate();

        ContentType contentType = MimeKit.detailOf(filename);

        // content-type based on file mime
        response.header(CONTENT_TYPE, contentType.getMimeType());
        // content-disposition
        String disposition = contentType.isInline() ? "inline" : "attachment;filename=".concat(filename);
        response.header(CONTENT_DISPOSITION, disposition);

        // cache-control
        int cacheSeconds = razor.getEnv().getInt(ENV_KEY_HTTP_CACHE_SECONDS, DEFAULT_HTTP_CACHE_SECONDS);
        LocalDateTime expireDateTime = LocalDateTime.now().plusSeconds(cacheSeconds);
        response.header(EXPIRES, DateKit.getGmtDateString(expireDateTime));
        response.header(CACHE_CONTROL, "private, max-age=" + cacheSeconds);

        String lastMdf;
        if (file != null) {

            lastMdf = DateKit.getGmtDateString(new Date(file.lastModified()));
        } else {

            lastMdf = DateKit.getGmtDateString();
        }
        response.header(LAST_MODIFIED, lastMdf);

        // keep-alive
        if (request.keepAlive()) {

            response.header(CONNECTION, "keep-alive");
        }
    }
}
