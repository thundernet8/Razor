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


package com.razor.server;

import com.razor.Razor;
import com.razor.exception.RazorException;
import com.razor.mvc.http.IHttpMethod;
import com.razor.mvc.http.Request;
import com.razor.mvc.http.Response;
import io.netty.channel.ChannelHandlerContext;
import com.razor.env.Env;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;

import static com.razor.mvc.Constants.*;

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

        if (!request.method().equals(IHttpMethod.GET)) {
            response.sendError(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return false;
        }

        String path = request.path();
        if (path.toUpperCase().startsWith("/WEB-INF")) {
            response.sendError(HttpResponseStatus.FORBIDDEN);
            return false;
        }

        // security check, more TODO
        if (path.contains("..") || path.contains(".".concat(File.separator)) || path.contains(File.separator.concat(".")) || path.charAt(0) == '.' || path.charAt(path.length() - 1) == '.') {
            response.sendError(HttpResponseStatus.NOT_FOUND);
            return false;
        }

        Env env = razor.getEnv();
        String webRoot = CLASS_PATH.concat(File.separator).concat(env.get(ENV_KEY_RESOURCE_CONTENT_DIR, DEFAULT_RESOURCE_CONTENT_DIR));
        String absPath = webRoot.concat(path);

        File file = new File(absPath);
        if (!simpleFileCheck(file, request, response)) {
            return false;
        }

        if (file.isDirectory()) {
            // find indexs
            List<String> indexs = (ArrayList<String>)env.getObject(ENV_KEY_INDEX_FILES).orElse(DEFAULT_INDEX_FILES);
            for (int i=0; i<indexs.size(); i++) {
                String filePath = absPath.concat(File.separator).concat(indexs.get(i));
                file = new File(filePath);
                if (simpleFileCheck(file, request, response)) {
                    break;
                }
            }

            if (file.isDirectory()) {
                response.sendError(HttpResponseStatus.NOT_FOUND);
                return false;
            }
        }

        // check if cache and 304
        if (checkCache(file, request, response)) {
            return false;
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
            long length = raf.length();

            response.header(HttpHeaderNames.CONTENT_LENGTH, new AsciiString(Long.toString(length)));
            setHeaders(file, request, response);

            response.sendFile(raf, length);
            return true;
        } catch (FileNotFoundException e) {
            log.error("Static file not found: {}", file.getPath());

            response.sendError(HttpResponseStatus.NOT_FOUND);
            throw new RazorException(e);
        } catch (IOException e) {
            log.error("Static file IO exception: {}", file.getPath());

            response.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            throw new RazorException(e);
        }

    }

    private boolean simpleFileCheck(File file, Request request, Response response) {

        if (!file.exists() || file.isHidden()) {
            response.sendError(HttpResponseStatus.NOT_FOUND);
            return false;
        }

        if (!file.isFile()) {
            response.sendError(HttpResponseStatus.FORBIDDEN);
            return false;
        }

        return true;
    }

    private boolean checkCache(File file, Request request, Response response) {
        // TODO
        return false;
    }

    private void setHeaders(File file, Request request, Response response) {
        // TODO

        // content-type based on file mime
        response.header(HttpHeaderNames.CONTENT_TYPE, new AsciiString("image/x-icon"));
        response.header(HttpHeaderNames.CONTENT_DISPOSITION, "inline");

        // cache-control

        // keep-alive
        if (request.keepAlive()) {
            response.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
    }
}
