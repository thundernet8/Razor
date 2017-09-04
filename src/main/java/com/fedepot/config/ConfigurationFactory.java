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


package com.fedepot.config;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.fedepot.mvc.Constants.*;

/**
 * Parse app configuration xml to properties
 *
 * @author Touchumind
 * @since 0.0.1
 */
public class ConfigurationFactory {

    public static Properties parseAppXml(File file) throws Exception {

        FileInputStream in = new FileInputStream(file);

        return parseAppXml(in);
    }

    public static Properties parseAppXml(FileInputStream in) throws Exception {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuiler = dbFactory.newDocumentBuilder();
        Document doc = dBuiler.parse(in);
        doc.getDocumentElement().normalize();

        Properties properties = new Properties();

        // server
        NodeList serverNodes = doc.getElementsByTagName("server");
        if (serverNodes.getLength() > 0) {

            Node serverNode = serverNodes.item(0);

            if (serverNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)serverNode;
                properties.put(ENV_KEY_SERVER_HOST, element.getElementsByTagName("host").item(0).getTextContent());
                properties.put(ENV_KEY_SERVER_PORT, Integer.parseInt(element.getElementsByTagName("port").item(0).getTextContent()));
                properties.put(ENV_KEY_CHARSET, element.getElementsByTagName("charset").item(0).getTextContent());
                properties.put(ENV_KEY_HTTP_CACHE_SECONDS, Integer.parseInt(element.getElementsByTagName("cache").item(0).getTextContent()));
                properties.put(ENV_KEY_SSL, element.getElementsByTagName("ssl").item(0).getTextContent().equals("true"));
            }
        }

        // root
        NodeList rootNodes = doc.getElementsByTagName("root");
        if (rootNodes.getLength() > 0) {

            Node rootNode = rootNodes.item(0);

            if (rootNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)rootNode;
                properties.put(ENV_KEY_WEB_ROOT_DIR, element.getElementsByTagName("web").item(0).getTextContent());
                properties.put(ENV_KEY_TEMPLATE_ROOT_DIR, element.getElementsByTagName("template").item(0).getTextContent());
            }
        }

        // static rules
        NodeList staticRuleNodes = doc.getElementsByTagName("statics");
        if (staticRuleNodes.getLength() > 0) {

            Node staticRuleNode = staticRuleNodes.item(0);

            if (staticRuleNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)staticRuleNode;
                NodeList indexs = element.getElementsByTagName("static");
                List<String> list = new ArrayList<>();
                for (int i=0; i<indexs.getLength(); i++) {

                    list.add(indexs.item(i).getTextContent());
                }
                properties.put(ENV_KEY_STATIC_RULES, list);
            }
        }

        // index files
        NodeList indexFileNodes = doc.getElementsByTagName("indexFiles");
        if (indexFileNodes.getLength() > 0) {

            Node indexFileNode = indexFileNodes.item(0);

            if (indexFileNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)indexFileNode;
                NodeList indexs = element.getElementsByTagName("index");
                List<String> list = new ArrayList<>();
                for (int i=0; i<indexs.getLength(); i++) {

                    list.add(indexs.item(i).getTextContent());
                }
                properties.put(ENV_KEY_INDEX_FILES, list);
            }
        }


        // session
        NodeList sessionNodes = doc.getElementsByTagName("session");
        if (sessionNodes.getLength() > 0) {

            Node sessionNode = sessionNodes.item(0);

            if (sessionNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)sessionNode;
                properties.put(ENV_KEY_SESSION_KEY, element.getElementsByTagName("key").item(0).getTextContent());
                properties.put(ENV_KEY_SESSION_TIMEOUT, Integer.parseInt(element.getElementsByTagName("timeout").item(0).getTextContent()));
            }
        }


        // error pages
        NodeList errPageNodes = doc.getElementsByTagName("errorPage");
        if (errPageNodes.getLength() > 0) {

            Node errPageNode = errPageNodes.item(0);

            if (errPageNode.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element)errPageNode;
                properties.put(ENV_KEY_403_PAGE_TEMPLATE, element.getElementsByTagName("_403").item(0).getTextContent());
                properties.put(ENV_KEY_404_PAGE_TEMPLATE, element.getElementsByTagName("_404").item(0).getTextContent());
                properties.put(ENV_KEY_500_PAGE_TEMPLATE, element.getElementsByTagName("_500").item(0).getTextContent());
                properties.put(ENV_KEY_502_PAGE_TEMPLATE, element.getElementsByTagName("_502").item(0).getTextContent());
            }
        }


        return properties;
    }
}
