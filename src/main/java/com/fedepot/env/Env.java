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


package com.fedepot.env;

import com.fedepot.config.ConfigurationFactory;
import com.fedepot.mvc.Constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Razor environment handler
 *
 * @author Touchumind
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Env {

    private Properties props = new Properties();

    private Env(Properties props) {

        this.props = props;
    }

    public static Env defaults() {

        return new Env();
    }

    public Env set(@NonNull String key, @NonNull Object value) {

        props.put(key, value);

        return this;
    }

    public Env setMulti(@NonNull Map<String, String> map) {

        map.forEach(props::setProperty);

        return this;
    }

    public Optional<String> get(@NonNull String key) {

        return Optional.ofNullable(props.getProperty(key));
    }

    public String get(@NonNull String key, String defaultValue) {

        return props.getProperty(key, defaultValue);
    }

    public Optional<Object> getObject(@NonNull String key) {

        return Optional.ofNullable(props.get(key));
    }

    public Optional<Integer> getInt(@NonNull String key) {

        Optional<Object> optional = getObject(key);

        return optional.isPresent() ? Optional.of(Integer.parseInt(optional.get().toString())) : Optional.empty();
    }

    public Integer getInt(@NonNull String key, int defaultValue) {

        return getInt(key).orElse(defaultValue);
    }

    public Optional<Long> getLong(@NonNull String key) {

        Optional<Object> optional = getObject(key);

        return optional.isPresent() ? Optional.of(Long.parseLong(optional.get().toString())) : Optional.empty();
    }

    public Long getLong(@NonNull String key, long defaultValue) {

        return getLong(key).orElse(defaultValue);
    }

    public Optional<Double> getDouble(@NonNull String key) {

        Optional<Object> optional = getObject(key);

        return optional.isPresent() ? Optional.of(Double.parseDouble(optional.get().toString())) : Optional.empty();
    }

    public Double getDouble(@NonNull String key, double defaultValue) {

        return getDouble(key).orElse(defaultValue);
    }

    public Optional<Boolean> getBool(@NonNull String key) {

        Optional<Object> optional = getObject(key);

        return optional.isPresent() ? Optional.of(Boolean.parseBoolean(optional.get().toString())) : Optional.empty();
    }

    public Boolean getBool(@NonNull String key, boolean defaultValue) {

        return getBool(key).orElse(defaultValue);
    }

    public Properties getProps() {

        return props;
    }

    /**
     * Initialize env from xml file
     *
     * @return Env instance
     */
    public static Env fromXml() {

        String appXmlPath = Constants.APP_CLASS_PATH.concat("/WEB-INF/app.xml");

        URL appXmlUrl = Env.class.getResource("/WEB-INF/app.xml");

        try {

            Properties properties;

            if (appXmlUrl == null) {

                String defaultAppXmlPath = Constants.RAZOR_CLASS_PATH.concat("app_default.xml");

                log.info("App configuration file {} is not exist, use {} instead.", appXmlPath, defaultAppXmlPath);

                properties = ConfigurationFactory.parseAppXml(Env.class.getResourceAsStream("/app_default.xml"));
            } else {

                log.info("App use configuration from file {}", appXmlPath);

                properties = ConfigurationFactory.parseAppXml(appXmlUrl.openStream());
            }

            return new Env(properties);
        } catch (Exception e) {

            log.error(e.toString());

            return Env.defaults();
        }
    }
}
