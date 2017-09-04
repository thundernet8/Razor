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


package com.fedepot.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * File handle utils
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class FileKit {

    /**
     * Read specified file content(Encoding UTF-8)
     *
     * @param absPath file path
     * @return content string
     */
    public static String read(String absPath) {

        File file = new File(absPath);
        if (file.exists() && file.isFile() && !file.isHidden()) {
            RandomAccessFile raf;
            try {

                raf = new RandomAccessFile(file, "r");
                byte[] data = new byte[(int)raf.length()];
                raf.readFully(data);

                return new String(data, StandardCharsets.UTF_8);

            } catch (Exception e) {

                log.error(e.toString());
            }
        }

        return null;
    }
}
