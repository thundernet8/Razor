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


package com.fedepot.cache;

import com.fedepot.event.EventEmitter;
import com.fedepot.event.EventType;
import com.fedepot.mvc.Constants;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import java.io.File;
import java.util.Optional;

/**
 * Ehcache factory
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
public class Ehcache implements Cache {

    private static Ehcache instance;

    private static String DEFAULT_GROUP = "_default_";

    private CacheManager cacheManager;


    private Ehcache(String group) {

        String configXmlPath = Constants.CLASS_PATH.concat("/WEB-INF/ehcache.xml");
        File file = new File(configXmlPath);

        if (file.exists()) {

            log.info("Ehcache use configuration from file {}", configXmlPath);

        } else {

            configXmlPath = Constants.RAZOR_CLASS_PATH.concat("/ehcache_default.xml");

            log.info("Ehcache default configuration file is not exist, use {} instead.", configXmlPath);

            file = new File(configXmlPath);
        }

        Configuration configuration = ConfigurationFactory.parseConfiguration(file);

        this.cacheManager = CacheManager.create(configuration);

        if (!cacheManager.cacheExists(DEFAULT_GROUP)) {

            this.cacheManager.addCache(DEFAULT_GROUP);
        }

        if (!cacheManager.cacheExists(group)) {

            this.cacheManager.addCache(group);
        }

        // persist when app stop
        EventEmitter.newInstance().on(EventType.APP_STOP, e -> {

            log.info("Persist as app shutting down");
            this.shutdown();
        });
    }

    public synchronized static Ehcache newInstance(String group) {

        if (instance == null) {

            instance = new Ehcache(group);
        }

        if (!instance.cacheManager.cacheExists(group)) {

            instance.cacheManager.addCache(group);
        }

        return instance;
    }

    private net.sf.ehcache.Cache cacher(String group) {

        net.sf.ehcache.Cache cacher = this.cacheManager.getCache(group);

        if (cacher == null) {

            log.error("Cache group {} is not exist", group);
        }

        return cacher;
    }


    public void add(String key, Object value, int expires) {

        add(key, value, expires, DEFAULT_GROUP);
    }

    @Override
    public void add(String key, Object value, int expires, String group) {

        net.sf.ehcache.Cache cacher = cacher(group);

        if (cacher != null) {

            Element element = new Element(key, value);
            element.setTimeToLive(expires);
            cacher.put(element);
        }
    }

    @Override
    public boolean safeAdd(String key, Object value, int expires) {

        return safeAdd(key, value, expires, DEFAULT_GROUP);
    }

    @Override
    public boolean safeAdd(String key, Object value, int expires, String group) {

        try {

            add(key, value, expires);

            return true;
        } catch (Exception e) {

            log.error(e.toString());

            return false;
        }
    }

    @Override
    public void delete(String key) {

        delete(key, DEFAULT_GROUP);
    }

    @Override
    public void delete(String key, String group) {

        net.sf.ehcache.Cache cacher = cacher(group);

        if (cacher != null) {

            cacher.remove(key);
        }
    }

    @Override
    public Object get(String key, Object defaultValue) {

        return get(key, DEFAULT_GROUP, defaultValue);
    }

    @Override
    public Object get(String key, String group, Object defaultValue) {

        return get(key, group).orElse(defaultValue);
    }

    @Override
    public Optional<Object> get(String key) {

        return get(key, DEFAULT_GROUP);
    }

    @Override
    public Optional<Object> get(String key, String group) {

        net.sf.ehcache.Cache cacher = cacher(group);

        if (cacher != null) {

            Element element = cacher.get(key);

            if (element != null) {

                return Optional.ofNullable(element.getObjectValue());
            }
        }

        return Optional.empty();
    }

    @Override
    public long incr(String key, int by) {

        return incr(key, DEFAULT_GROUP, by);
    }

    @Override
    public synchronized long incr(String key, String group, int by) {

        net.sf.ehcache.Cache cacher = cacher(group);

        if (cacher != null) {

            Element element = cacher.get(key);

            if (element == null) {

                return -1;
            }

            long newValue = ((Number)element.getObjectValue()).longValue() + by;
            Element newEle = new Element(key, newValue);
            newEle.setTimeToLive(element.getTimeToLive());

            cacher.put(newEle);

            return newValue;
        }

        return -1;
    }

    @Override
    public long decr(String key, int by) {

        return decr(key, DEFAULT_GROUP, by);
    }

    @Override
    public synchronized long decr(String key, String group, int by) {

        net.sf.ehcache.Cache cacher = cacher(group);

        if (cacher != null) {

            Element element = cacher.get(key);

            if (element == null) {

                return -1;
            }

            long newValue = ((Number)element.getObjectValue()).longValue() - by;
            Element newEle = new Element(key, newValue);
            newEle.setTimeToLive(element.getTimeToLive());

            cacher.put(newEle);

            return newValue;
        }

        return -1;
    }

    @Override
    public void clear() {

        String[] groups = cacheManager.getCacheNames();

        for(String group : groups) {

            if (cacheManager.cacheExists(group)) {

                cacheManager.getCache(group).flush();
            }
        }
    }

    @Override
    public void clear(String group) {

        cacher(group).flush();
    }

    @Override
    public void shutdown() {

        this.cacheManager.shutdown();
    }

    // TODO check value serializable
}
