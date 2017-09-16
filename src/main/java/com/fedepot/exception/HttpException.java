/**
 * Elune - Lightweight Forum Powered by Razor.
 * Copyright (C) 2017, Touchumind<chinash2010@gmail.com>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.fedepot.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Exception for http response(used on APIController for rest service)
 */
public class HttpException extends RuntimeException {

    private HttpResponseStatus status;

    public int getCode() {

        return status.code();
    }

    @Override
    public String getMessage() {

        String msg = super.getMessage();
        if (msg == null || msg.isEmpty()) {

            return status.reasonPhrase();
        }

        return msg;
    }

    public HttpException() {

        this.status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public HttpException(String message) {

        this(message, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpException(String message, HttpResponseStatus status) {

        super(message);
        this.status = status;
    }

    public HttpException(String message, int httpCode) {

        this(message, HttpResponseStatus.valueOf(httpCode));
    }
}
