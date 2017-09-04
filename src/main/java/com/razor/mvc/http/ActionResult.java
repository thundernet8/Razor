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

import com.google.gson.Gson;
import com.razor.mvc.json.GsonFactory;

/**
 * Action execute result wrapper
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class ActionResult {

    private Object origin;

    private Class<?> originType;

    private String text;

    private ActionResult() { }

    public static ActionResult build(Object result, Class<?> type) {

        ActionResult actionResult = new ActionResult();
        actionResult.origin = result;
        actionResult.originType = type;

        actionResult.cast();

        return actionResult;
    }

    /**
     * Core cast method
     */
    private void cast() {

        // void
        if (originType == Void.TYPE) {

            text = "";
            return;
        }

        // TODO complicated customized object serialization
        Gson gson = GsonFactory.getGson();

        text = gson.toJson(origin);
    }


    public byte[] getBytes() {

        return text.getBytes();
    }
}
