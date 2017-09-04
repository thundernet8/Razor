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

import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;

/**
 * Implement of {@link ChannelProgressiveFutureListener} for file progressive future listener
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class ProgressiveFutureListener implements ChannelProgressiveFutureListener {

    private RandomAccessFile raf;

    public ProgressiveFutureListener(RandomAccessFile raf) {

        this.raf = raf;
    }

    @Override
    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {

        if (total < 0) {

            log.debug("Channel {} transfer progress: {}", future.channel(), progress);
        } else {

            log.debug("Channel {} transfer progress: {}, total {}", future.channel(), progress, total);
        }
    }

    @Override
    public void operationComplete(ChannelProgressiveFuture future) throws Exception {

        try {

            raf.close();
            log.debug("Channel {} transfer complete", future.channel());
        } catch (Exception e) {

            log.error("Close randomAccessFile with error", e);
        }
    }
}
