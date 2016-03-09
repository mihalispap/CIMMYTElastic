/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.Namespace
 *  org.jdom.xpath.XPath
 */
package uiuc.oai;

import java.util.List;
import java.util.Vector;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OaiUtil {
    public static Element getXpathNode(String xpathString, Vector<Namespace> nsVector, Element xml) throws JDOMException {
        XPath xpath = XPath.newInstance((String)xpathString);
        for (Namespace ns : nsVector) {
            xpath.addNamespace(ns);
        }
        Element selectSingleNode = (Element)xpath.selectSingleNode((Object)xml);
        return selectSingleNode;
    }

    public static Element getXpathNode(String xpathString, Vector<Namespace> nsVector, Document xml) throws JDOMException {
        return OaiUtil.getXpathNode(xpathString, nsVector, xml.getRootElement());
    }

    public static List getXpathList(String xpathString, Vector<Namespace> nsVector, Element xml) throws JDOMException {
        XPath xpath = XPath.newInstance((String)xpathString);
        for (Namespace ns : nsVector) {
            xpath.addNamespace(ns);
        }
        return xpath.selectNodes((Object)xml);
    }
}

