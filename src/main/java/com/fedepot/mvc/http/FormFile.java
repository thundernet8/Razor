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


package com.fedepot.mvc.http;

import com.google.gson.annotations.Expose;
import lombok.Setter;
import lombok.Getter;

/**
 * File from multipart/form-data request
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class FormFile {

    @Getter
    private String name;

    @Getter
    private String fileName;

    @Getter
    private String contentType;

    @Getter
    private long length;

    @Setter
    @Getter
    @Expose(serialize = false)
    private byte[] data;

    FormFile(String name, String fileName, String contentType, long length) {

        this.name = name;
        this.fileName = fileName;
        this.contentType = contentType;
        this.length = length;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(FormFile.class.getPackage().getName());
        sb.append(".FormFile(");
        sb.append("name='");
        sb.append(name);
        sb.append("', fileName='");
        sb.append(fileName);
        sb.append("', contentType='");
        sb.append(contentType);
        sb.append("', size=");

        if (length < 1024) {

            sb.append(length);
            sb.append("Bytes");
        } else if (length < 1024*1024) {

            sb.append(length / 1024);
            sb.append("Kb");
        } else {

            sb.append(length / (1024*1024));
            sb.append("Mb");
        }

        return sb.toString();
    }
}
