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


package com.razor.event;

import com.razor.Razor;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Global events register and trigger
 *
 * @author Touchumind
 * @since 0.0.1
 */
public final class EventEmitter {

    private static EventEmitter instance;

    private Map<EventType, List<EventListener>> listeners;

    private EventEmitter() {

        listeners = Stream.of(EventType.values()).collect(Collectors.toMap(key -> key, value -> new LinkedList<>()));
    }

    public static EventEmitter newInstance() {

        if(instance == null) {

            synchronized (EventEmitter.class) {

                if (instance == null) {

                    instance = new EventEmitter();
                }
            }
        }

        return instance;
    }

    public void on(EventType eventType, EventListener listener) {

        System.out.println("add event listener: " + eventType);

        listeners.get(eventType).add(listener);
    }

    public void emit(EventType eventType) {


        emit(eventType, null);
    }

    public void emit(EventType eventType, Razor razor) {

        listeners.get(eventType).forEach(listener -> listener.call(new Event(eventType, razor)));
    }
}
