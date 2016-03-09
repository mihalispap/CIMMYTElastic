/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Attribute
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.Namespace
 *  org.jdom.input.SAXBuilder
 *  org.jdom.xpath.XPath
 */
package uiuc.oai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;
import sun.misc.BASE64Encoder;
import uiuc.oai.OAIError;
import uiuc.oai.OAIException;
import uiuc.oai.OAIMetadataFormatList;
import uiuc.oai.OAIRecord;
import uiuc.oai.OAIRecordList;
import uiuc.oai.OAIResumptionStream;
import uiuc.oai.OAISetList;
import uiuc.oai.OaiUtil;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class OAIRepository {
    private String strRepositoryId;
    private String strRepositoryName;
    private String strBaseURL = "";
    private String[] strAdminEmail;
    private String[] strCompression;
    private String strEarliestDatestamp;
    private String strDeletedRecord;
    private String strGranularity;
    private String strProtocolVersion;
    private String strUserAgent = "OAIHarvester University of Illinois Library";
    private String strFrom = "someone@somewhere.edu";
    private String strUser = "";
    private String strPassword = "";
    private String strResponseDate;
    private String sRawResponse;
    private List ixmlDescriptions;
    private List ixmlErrors;
    private Element ixmlRequest;
    private Vector<Namespace> descrNamespaceNodes;
    private Vector<Namespace> namespaces;
    private int validation = 2;
    private int state = 0;
    private int iRetryLimit = 5;
    private int iMaxRetryMinutes = 60;
    private boolean fixXmlEncoding = true;
    public final int STATE_UNIDENTIFIED = 0;
    public final int STATE_IDENTIFIED = 1;
    public static final int VALIDATION_STRICT = 0;
    public static final int VALIDATION_VERY_STRICT = 1;
    public static final int VALIDATION_LOOSE = 2;
    public static String XMLNS_DC = "http://purl.org/dc/elements/1.1/";
    public static String XMLNS_RFC1807 = "http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt";
    public static String XMLNS_OAI_MARC = "http://www.openarchives.org/OAI/1.1/oai_marc";
    public static String XMLNS_OAI = "http://www.openarchives.org/OAI/1.1/OAI_";
    public static String XMLNS_ID = "http://www.openarchives.org/OAI/1.1/oai-identifier";
    public static String XMLNS_EPR = "http://www.openarchives.org/OAI/1.1/eprints";
    public static String XMLNS_OAI_2_0 = "http://www.openarchives.org/OAI/2.0/";
    public static String XMLNS_OAI_DC_2_0 = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    public static String XMLNS_ID_2_0 = "http://www.openarchives.org/OAI/2.0/oai-identifier";
    public static String XMLNS_OAI_1_0 = "http://www.openarchives.org/OAI/1.0/OAI_";
    public static String XMLNS_ID_1_0 = "http://www.openarchives.org/OAI/oai-identifier";
    public static String XMLNS_EPR_1_0 = "http://www.openarchives.org/OAI/eprints";
    public static String XMLNS_OAI_MARC_1_0 = "http://www.openarchives.org/OAI/oai_marc";
    public static String XMLNS_ID_1_0_aps = "http://www.openarchives.org/OAI/oai-identifier.xsd";

    public void setBaseURL(String url) throws OAIException {
        this.strBaseURL = url;
        this.identify();
    }

    public String getBaseURL() throws OAIException {
        this.priCheckBaseURL();
        return this.strBaseURL;
    }

    private InputSource priCreateDummyGetRecord(String id, InputStream xml) throws OAIException {
        String rec;
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
        if (this.getProtocolMajorVersion() < 2) {
            rec = "<GetRecord \n";
            rec = rec + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            rec = rec + "xmlns='http://www.openarchives.org/OAI/1.1/OAI_GetRecord' \n";
            rec = rec + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_GetRecord ";
            rec = rec + "http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd'>\n";
            rec = rec + "<responseDate>" + formatter.format(new Date()) + "</responseDate>\n";
            rec = rec + "<requestURL>junk:GetRecord</requestURL>\n";
            rec = rec + "<record>\n";
            rec = rec + "<header>\n";
            rec = rec + "<identifier>" + id + "</identifier>\n";
            rec = rec + "<datestamp>" + formatter.format(new Date()) + "</datestamp>\n";
            rec = rec + "</header>\n";
            rec = rec + "<about>\n";
            rec = rec + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.frndMyEncode(xml) + "]]></junk:junk>\n";
            rec = rec + "</about>\n";
            rec = rec + "</record>\n";
            rec = rec + "</GetRecord>";
        } else {
            rec = "<OAI-PMH \n";
            rec = rec + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            rec = rec + "xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            rec = rec + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            rec = rec + "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd'>\n";
            rec = rec + "<responseDate>" + formatter.format(new Date()) + "</responseDate>\n";
            rec = rec + "<request>junk:GetRecord</request>\n";
            rec = rec + "<GetRecord>\n";
            rec = rec + "<record>\n";
            rec = rec + "<header>\n";
            rec = rec + "<identifier>" + id + "</identifier>\n";
            rec = rec + "<datestamp>" + formatter.format(new Date()) + "</datestamp>\n";
            rec = rec + "</header>\n";
            rec = rec + "<about>\n";
            rec = rec + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.frndMyEncode(xml) + "]]></junk:junk>\n";
            rec = rec + "</about>\n";
            rec = rec + "</record>\n";
            rec = rec + "</GetRecord>\n";
            rec = rec + "</OAI-PMH>";
        }
        StringReader sr = new StringReader(rec);
        return new InputSource(sr);
    }

    private InputSource priCreateDummyIdentify(InputStream xml) throws OAIException {
        String ret;
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
        if (this.getProtocolMajorVersion() < 2) {
            ret = "<Identify \n";
            ret = ret + "xmlns='http://www.openarchives.org/OAI/1.1/OAI_Identify' \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_Identify ";
            ret = ret + "http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd'> \n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
            ret = ret + "<requestURL>junk:Identify</requestURL> \n";
            ret = ret + "<repositoryName>UNKNOWN</repositoryName> \n";
            ret = ret + "<baseURL>" + this.strBaseURL + "</baseURL> \n";
            ret = ret + "<protocolVersion>UNKNOWN</protocolVersion> \n";
            ret = ret + "<adminEmail>mailto:UNKNOWN</adminEmail> \n";
            ret = ret + "<description>\n";
            ret = ret + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.frndMyEncode(xml) + "]]></junk:junk>\n";
            ret = ret + "</description>\n";
            ret = ret + "</Identify>";
        } else {
            ret = "<OAI-PMH \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            ret = ret + "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd'>\n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate>\n";
            ret = ret + "<request>junk:Identify</request>\n";
            ret = ret + "<Identify>\n";
            ret = ret + "<repositoryName>UNKNOWN</repositoryName> \n";
            ret = ret + "<baseURL>" + this.strBaseURL + "</baseURL> \n";
            ret = ret + "<protocolVersion>UNKNOWN</protocolVersion> \n";
            ret = ret + "<adminEmail>mailto:UNKNOWN</adminEmail> \n";
            ret = ret + "<description>\n";
            ret = ret + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.frndMyEncode(xml) + "]]></junk:junk>\n";
            ret = ret + "</description>\n";
            ret = ret + "</Identify>\n";
            ret = ret + "</OAI-PMH>";
        }
        StringReader sr = new StringReader(ret);
        return new InputSource(sr);
    }

    protected void frndSetErrors(List e) {
        this.ixmlErrors = e;
    }

    protected void frndSetRepositoryID(String id) {
        this.strRepositoryId = id;
    }

    protected void frndSetRequest(Element u) {
        this.ixmlRequest = u;
    }

    protected void frndSetResponseDate(String d) {
        this.strResponseDate = d;
    }

    protected String frndGetRawResponse() {
        return this.sRawResponse;
    }

    protected String frndGetUser() {
        return this.strUser;
    }

    protected String frndGetPassword() {
        return this.strPassword;
    }

    public void setBasicAuthorization(String usr, String pwd) {
        this.strUser = usr;
        this.strPassword = pwd;
    }

    public OAIError getLastOAIError() throws OAIException {
        return this.getLastOAIError(0);
    }

    public OAIError getLastOAIError(int i) throws OAIException {
        OAIError err = null;
        if (this.getLastOAIErrorCount() > 0 && i < this.getLastOAIErrorCount()) {
            err = new OAIError();
            Element n = (Element)this.ixmlErrors.get(i);
            err.frndSetCode(n.getAttribute("code").getValue());
            err.frndSetReason(n.getText());
        }
        return err;
    }

    public int getLastOAIErrorCount() {
        if (this.ixmlErrors != null) {
            return this.ixmlErrors.size();
        }
        return 0;
    }

    public String getRequestVerb() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("verb=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 5, idx2);
        }
        return ret;
    }

    public String getRequestIdentifier() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("identifier=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 11, idx2);
        }
        return ret;
    }

    public String getRequestMetadataPrefix() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("metadataPrefix=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 15, idx2);
        }
        return ret;
    }

    public String getRequestFrom() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("from=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 5, idx2);
        }
        return ret;
    }

    public String getRequestUntil() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("until=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 6, idx2);
        }
        return ret;
    }

    public String getRequestSet() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("set=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 4, idx2);
        }
        return ret;
    }

    public String getRequestResumptionToken() {
        String ret = "";
        int idx1 = this.getRequestURL().indexOf("resumptionToken=");
        if (idx1 >= 0) {
            int idx2 = this.getRequestURL().indexOf("&", idx1);
            if (idx2 <= 0) {
                idx2 = this.getRequestURL().length();
            }
            ret = this.getRequestURL().substring(idx1 + 16, idx2);
        }
        return ret;
    }

    public int getAdminEmailCount() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.strAdminEmail == null) {
            return 0;
        }
        return this.strAdminEmail.length;
    }

    public int getCompressionCount() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.strCompression == null) {
            return 0;
        }
        return this.strCompression.length;
    }

    public String getEarliestDatestamp() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.getProtocolMajorVersion() < 2) {
            throw new OAIException(15, "'EarliestDateStamp' is not supporeted.");
        }
        return this.strEarliestDatestamp;
    }

    public String getDeletedRecord() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.getProtocolMajorVersion() < 2) {
            throw new OAIException(15, "'DeletedRecord' is not supported.");
        }
        return this.strDeletedRecord;
    }

    public String getGranularity() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        String ret = this.getProtocolMajorVersion() < 2 ? "YYYY-MM-DD" : (this.strGranularity != null ? this.strGranularity : "YYYY-MM-DD");
        return ret;
    }

    public String getResponseDate() {
        return this.strResponseDate;
    }

    public String getRequestBaseURL() {
        String ret = this.ixmlRequest.getText();
        if (ret.endsWith("?")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    public int getMaxRetryMinutes() {
        return this.iMaxRetryMinutes;
    }

    public void setMaxRetryMinutes(int m) {
        this.iMaxRetryMinutes = m;
    }

    protected String frndMyEncode(InputStream s) throws OAIException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(s, "UTF-8"));
            String tmp = "";
            String ret = "";
            int idx = 0;
            while ((tmp = br.readLine()) != null) {
                while ((idx = tmp.indexOf("]]>", idx)) >= 0) {
                    tmp = tmp.substring(0, idx + 2) + "&gt;" + tmp.substring(idx + 2);
                }
                ret = ret + tmp + "\n";
            }
            return ret;
        }
        catch (IOException ie) {
            throw new OAIException(14, ie.getMessage());
        }
    }

    protected void frndSetNamespaceNode(Vector<Namespace> ns) {
        this.namespaces = ns;
    }

    protected Vector<Namespace> getNamespaceNode() {
        return this.namespaces;
    }

    private String priBuildParamString(String u, String f, String s, String i, String m) {
        String param = "";
        if (u != null && u.length() > 0) {
            param = param + "&until=" + u;
        }
        if (f != null && f.length() > 0) {
            param = param + "&from=" + f;
        }
        if (s != null && s.length() > 0) {
            param = param + "&set=" + s;
        }
        if (i != null && i.length() > 0) {
            param = param + "&identifier=" + i;
        }
        if (m != null && m.length() > 0) {
            param = param + "&metadataPrefix=" + m;
        }
        return param;
    }

    private void priCheckBaseURL() throws OAIException {
        if (this.strBaseURL == null || this.strBaseURL.length() == 0) {
            throw new OAIException(6, "No BaseURL");
        }
    }

    public Element getDescription() throws OAIException {
        return this.getDescription(0);
    }

    public Element getDescription(int i) throws OAIException {
        Element ret = null;
        if (this.state == 0) {
            this.identify();
        }
        if (this.ixmlDescriptions.size() > 0 && i < this.ixmlDescriptions.size()) {
            ret = (Element)this.ixmlDescriptions.get(i);
        }
        return ret;
    }

    public int getDescriptionCount() {
        return this.ixmlDescriptions.size();
    }

    public OAIRecord getRecord(String identifier) throws OAIException {
        return this.getRecord(identifier, "oai_dc");
    }

    private Element setNameSpace(Document xml) throws JDOMException, OAIException {
        this.namespaces = new Vector();
        this.namespaces.add(Namespace.getNamespace((String)"oai", (String)(XMLNS_OAI + "GetRecord")));
        this.namespaces.add(Namespace.getNamespace((String)"dc", (String)XMLNS_DC));
        XPath xpath = XPath.newInstance((String)"/oai:GetRecord/oai:record");
        for (Namespace ns : this.namespaces) {
            xpath.addNamespace(ns);
        }
        List list = xpath.selectNodes((Object)xml);
        Element node = (Element)list.get(0);
        if (node == null) {
            this.namespaces = new Vector();
            this.namespaces.add(Namespace.getNamespace((String)"oai", (String)XMLNS_OAI_2_0));
            xpath = XPath.newInstance((String)"/oai:OAI-PMH/oai:GetRecord/oai:record");
            for (Namespace ns22 : this.namespaces) {
                xpath.addNamespace(ns22);
            }
            list = xpath.selectNodes((Object)xml);
            node = (Element)list.get(0);
            if (node == null) {
                this.namespaces = new Vector();
                this.namespaces.add(Namespace.getNamespace((String)"oai", (String)(XMLNS_OAI_1_0 + "GetRecord")));
                xpath = XPath.newInstance((String)"/oai:GetRecord/oai:record");
                for (Namespace ns22 : this.namespaces) {
                    xpath.addNamespace(ns22);
                }
                node = (Element)xpath.selectSingleNode((Object)xml);
                node = (Element)list.get(0);
            } else {
                xpath = XPath.newInstance((String)"oai:OAI-PMH/oai:error");
                for (Namespace ns22 : this.namespaces) {
                    xpath.addNamespace(ns22);
                }
                this.ixmlErrors = xpath.selectNodes((Object)xml);
                if (this.ixmlErrors.size() > 0) {
                    this.strProtocolVersion = "2";
                    throw new OAIException(15, this.getLastOAIError().getCode() + ": " + this.getLastOAIError().getReason());
                }
            }
        }
        return node;
    }

    public OAIRecord getRecord(String identifier, String metadataPrefix) throws OAIException {
        OAIRecord rec = new OAIRecord();
        this.priCheckBaseURL();
        String params = this.priBuildParamString("", "", "", identifier, metadataPrefix);
        try {
            SAXBuilder docBuilder;
            DocumentBuilderFactory docFactory;
            URL url;
            block28 : {
                url = new URL(this.strBaseURL + "?verb=GetRecord" + params);
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http = this.frndTrySend(http);
                docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(true);
                if (this.validation == 1) {
                    docFactory.setValidating(true);
                } else {
                    docFactory.setValidating(false);
                }
                docBuilder = new SAXBuilder();
                Document xml = null;
                try {
                    xml = docBuilder.build(http.getInputStream());
                    rec.frndSetValid(true);
                }
                catch (IllegalArgumentException iae) {
                    throw new OAIException(14, iae.getMessage());
                }
                catch (JDOMException se) {
                    if (this.validation != 2) {
                        throw new OAIException(13, se.getMessage());
                    }
                    try {
                        url = new URL(this.strBaseURL + "?verb=GetRecord" + params);
                        http.disconnect();
                        http = (HttpURLConnection)url.openConnection();
                        http = this.frndTrySend(http);
                        xml = docBuilder.build(this.priCreateDummyGetRecord(identifier, http.getInputStream()));
                        rec.frndSetValid(false);
                    }
                    catch (JDOMException se2) {
                        throw new OAIException(13, se2.getMessage());
                    }
                }
                try {
                    this.namespaces = new Vector();
                    this.namespaces.add(Namespace.getNamespace((String)"oai", (String)(XMLNS_OAI + "GetRecord")));
                    this.namespaces.add(Namespace.getNamespace((String)"dc", (String)XMLNS_DC));
                    Element node = OaiUtil.getXpathNode("/oai:GetRecord/oai:record", this.namespaces, xml.getRootElement());
                    if (node == null) {
                        this.namespaces = new Vector();
                        this.namespaces.add(Namespace.getNamespace((String)"oai", (String)XMLNS_OAI_2_0));
                        XPath xpath = XPath.newInstance((String)"/oai:OAI-PMH/oai:GetRecord/oai:record");
                        for (Namespace ns : this.namespaces) {
                            xpath.addNamespace(ns);
                        }
                        node = OaiUtil.getXpathNode("/oai:OAI-PMH/oai:GetRecord/oai:record", this.namespaces, xml.getRootElement());
                        if (node == null) {
                            this.namespaces = new Vector();
                            this.namespaces.add(Namespace.getNamespace((String)"oai", (String)(XMLNS_OAI_1_0 + "GetRecord")));
                            node = OaiUtil.getXpathNode("/oai:GetRecord/oai:record", this.namespaces, xml.getRootElement());
                        } else {
                            this.ixmlErrors = OaiUtil.getXpathList("oai:OAI-PMH/oai:error", this.namespaces, xml.getRootElement());
                            if (this.ixmlErrors.size() > 0) {
                                this.strProtocolVersion = "2";
                                throw new OAIException(15, this.getLastOAIError().getCode() + ": " + this.getLastOAIError().getReason());
                            }
                        }
                    }
                    if (node != null) {
                        String attributeValue;
                        rec.frndSetRepository(this);
                        rec.frndSetMetadataPrefix(metadataPrefix);
                        rec.frndSetIdOnly(false);
                        rec.frndSetIdentifier(OaiUtil.getXpathNode("//oai:header/oai:identifier", this.namespaces, xml.getRootElement()).getText());
                        rec.frndSetDatestamp(OaiUtil.getXpathNode("//oai:header/oai:datestamp", this.namespaces, xml.getRootElement()).getTextTrim());
                        rec.frndSetRecord(node);
                        List nmap = node.getAttributes();
                        if (nmap != null && (attributeValue = node.getAttributeValue("status")) != null && !attributeValue.equals("")) {
                            rec.frndSetStatus(attributeValue);
                        }
                    } else {
                        rec = null;
                    }
                    node = OaiUtil.getXpathNode("//oai:responseDate", this.namespaces, xml.getRootElement());
                    if (node != null) {
                        this.strResponseDate = node.getText();
                    } else if (this.validation == 2) {
                        this.strResponseDate = "";
                    } else {
                        throw new OAIException(4, "GetRecord missing responseDate");
                    }
                    node = OaiUtil.getXpathNode("//oai:requestURL | //oai:request", this.namespaces, xml.getRootElement());
                    if (node != null) {
                        this.ixmlRequest = node;
                        break block28;
                    }
                    if (this.validation == 2) {
                        this.ixmlRequest = null;
                        break block28;
                    }
                    throw new OAIException(4, "GetRecord missing requestURL");
                }
                catch (JDOMException te) {
                    throw new OAIException(14, te.getMessage());
                }
            }
            url = null;
            docFactory = null;
            docBuilder = null;
        }
        catch (MalformedURLException mue) {
            throw new OAIException(14, mue.getMessage());
        }
        catch (FactoryConfigurationError fce) {
            throw new OAIException(14, fce.getMessage());
        }
        catch (IOException ie) {
            throw new OAIException(14, ie.getMessage());
        }
        return rec;
    }

    public String identify() throws OAIException {
        return this.identify(this.strBaseURL);
    }

    public String identify(String baseURL) throws OAIException {
        boolean v2 = false;
        this.priCheckBaseURL();
        try {
            URL url = new URL(baseURL + "?verb=Identify");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http = this.frndTrySend(http);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            if (this.validation == 1) {
                docFactory.setValidating(true);
            } else {
                docFactory.setValidating(false);
            }
            SAXBuilder docBuilder = new SAXBuilder();
            Document xml = null;
            try {
                xml = docBuilder.build(http.getInputStream());
            }
            catch (IllegalArgumentException iae) {
                throw new OAIException(14, iae.getMessage());
            }
            catch (JDOMException se) {
                if (this.validation != 2) {
                    throw new OAIException(13, se.getMessage());
                }
                try {
                    url = new URL(baseURL + "?verb=Identify");
                    http.disconnect();
                    http = (HttpURLConnection)url.openConnection();
                    http = this.frndTrySend(http);
                    xml = docBuilder.build(this.priCreateDummyIdentify(http.getInputStream()));
                }
                catch (JDOMException se2) {
                    throw new OAIException(13, se2.getMessage());
                }
            }
            try {
                XPath xpath;
                this.descrNamespaceNodes = new Vector();
                this.descrNamespaceNodes.add(Namespace.getNamespace((String)"oai_id", (String)(XMLNS_OAI + "Identify")));
                this.descrNamespaceNodes.add(Namespace.getNamespace((String)"id", (String)XMLNS_ID));
                this.descrNamespaceNodes.add(Namespace.getNamespace((String)"epr", (String)XMLNS_EPR));
                Element node = OaiUtil.getXpathNode("/oai_id:Identify", this.descrNamespaceNodes, xml.getRootElement());
                if (node == null) {
                    this.descrNamespaceNodes = new Vector();
                    this.descrNamespaceNodes.add(Namespace.getNamespace((String)"oai_id", (String)XMLNS_OAI_2_0));
                    this.descrNamespaceNodes.add(Namespace.getNamespace((String)"id", (String)XMLNS_ID_2_0));
                    this.descrNamespaceNodes.add(Namespace.getNamespace((String)"epr", (String)XMLNS_EPR));
                    node = OaiUtil.getXpathNode("/oai_id:OAI-PMH", this.descrNamespaceNodes, xml.getRootElement());
                    if (node != null) {
                        xpath = XPath.newInstance((String)"oai_id:OAI-PMH/oai_id:error");
                        for (Namespace ns22 : this.descrNamespaceNodes) {
                            xpath.addNamespace(ns22);
                        }
                        this.ixmlErrors = xpath.selectNodes((Object)xml);
                        if (this.getLastOAIErrorCount() > 0) {
                            this.strProtocolVersion = "2";
                            throw new OAIException(15, this.getLastOAIError().getCode() + ": " + this.getLastOAIError().getReason());
                        }
                        v2 = true;
                    } else {
                        this.descrNamespaceNodes = new Vector();
                        this.descrNamespaceNodes.add(Namespace.getNamespace((String)"oai_id", (String)(XMLNS_OAI_1_0 + "Identify")));
                        this.descrNamespaceNodes.add(Namespace.getNamespace((String)"id", (String)XMLNS_ID_1_0));
                        this.descrNamespaceNodes.add(Namespace.getNamespace((String)"epr", (String)XMLNS_EPR_1_0));
                    }
                }
                if ((node = OaiUtil.getXpathNode("//oai_id:repositoryName", this.descrNamespaceNodes, xml.getRootElement())) != null) {
                    this.strRepositoryName = node.getText();
                } else if (this.validation == 2) {
                    this.strRepositoryName = "UNKNOWN";
                } else {
                    throw new OAIException(4, "Identify missing repositoryName");
                }
                xpath = XPath.newInstance((String)"//oai_id:baseURL");
                for (Namespace ns22 : this.descrNamespaceNodes) {
                    xpath.addNamespace(ns22);
                }
                node = OaiUtil.getXpathNode("//oai_id:baseURL", this.descrNamespaceNodes, xml.getRootElement());
                if (node != null) {
                    if (node.getTextTrim().equals("")) {
                        throw new OAIException(4, "could not find baseURL in Identify (mabe the Identify return is wrong...)");
                    }
                    if (this.strBaseURL == null || this.strBaseURL.equals("")) {
                        this.strBaseURL = node.getText();
                    } else if (!this.strBaseURL.equals(node.getText())) {
                        // empty if block
                    }
                } else if (this.validation != 2) {
                    throw new OAIException(4, "Identify missing baseURL");
                }
                xpath = XPath.newInstance((String)"//oai_id:protocolVersion");
                for (Namespace ns22 : this.descrNamespaceNodes) {
                    xpath.addNamespace(ns22);
                }
                node = OaiUtil.getXpathNode("//oai_id:protocolVersion", this.descrNamespaceNodes, xml.getRootElement());
                if (node != null) {
                    if (node.getTextTrim().equals("")) {
                        throw new OAIException(4, "could not find protocolVersion in Identify (mabe the Identify return is wrong...)");
                    }
                    this.strProtocolVersion = node.getText();
                } else if (this.validation == 2) {
                    this.strProtocolVersion = "UNKNOWN";
                } else {
                    throw new OAIException(4, "Identify missing protocolVersion");
                }
                xpath = XPath.newInstance((String)"//oai_id:adminEmail");
                for (Namespace ns22 : this.descrNamespaceNodes) {
                    xpath.addNamespace(ns22);
                }
                List nl = xpath.selectNodes((Object)xml);
                if (nl.size() > 0) {
                    this.strAdminEmail = new String[nl.size()];
                    for (int i = 0; i < nl.size(); ++i) {
                        this.strAdminEmail[i] = ((Element)nl.get(i)).getText();
                    }
                } else if (this.validation == 2) {
                    this.strAdminEmail = new String[1];
                    this.strAdminEmail[0] = "mailto:UNKNOWN";
                } else {
                    throw new OAIException(4, "Identify missing adminEmail");
                }
                if (v2) {
                    xpath = XPath.newInstance((String)"//oai_id:earliestDatestamp");
                    for (Namespace ns3 : this.descrNamespaceNodes) {
                        xpath.addNamespace(ns3);
                    }
                    node = OaiUtil.getXpathNode("//oai_id:earliestDatestamp", this.descrNamespaceNodes, xml.getRootElement());
                    if (node != null) {
                        if (node.getTextTrim().equals("")) {
                            throw new OAIException(4, "could not find earliestDatestamp in Identify (mabe the Identify return is wrong...)");
                        }
                        this.strEarliestDatestamp = node.getText();
                    } else if (this.validation == 2) {
                        this.strEarliestDatestamp = "UNKNOWN";
                    } else {
                        throw new OAIException(4, "Identify missing earliestDatestamp");
                    }
                    node = OaiUtil.getXpathNode("//oai_id:deletedRecord", this.descrNamespaceNodes, xml.getRootElement());
                    if (node != null) {
                        if (node.getTextTrim().equals("")) {
                            throw new OAIException(4, "could not find deletedRecord in Identify (mabe the Identify return is wrong...)");
                        }
                        this.strDeletedRecord = node.getText();
                    } else if (this.validation == 2) {
                        this.strDeletedRecord = "UNKNOWN";
                    } else {
                        throw new OAIException(4, "Identify missing deletedRecord");
                    }
                    node = OaiUtil.getXpathNode("//oai_id:granularity", this.descrNamespaceNodes, xml.getRootElement());
                    if (node != null) {
                        if (node.getTextTrim().equals("")) {
                            throw new OAIException(4, "could not find granularity in Identify (mabe the Identify return is wrong...)");
                        }
                        this.strGranularity = node.getText();
                    } else if (this.validation == 2) {
                        this.strGranularity = "UNKNOWN";
                    } else {
                        throw new OAIException(4, "Identify missing granularity");
                    }
                    nl = OaiUtil.getXpathList("//oai_id:compression", this.descrNamespaceNodes, xml.getRootElement());
                    if (nl.size() > 0) {
                        this.strCompression = new String[nl.size()];
                        for (int i = 0; i < nl.size(); ++i) {
                            this.strCompression[i] = ((Element)nl.get(i)).getValue();
                        }
                    }
                }
                this.ixmlDescriptions = OaiUtil.getXpathList("//oai_id:description", this.descrNamespaceNodes, xml.getRootElement());
                node = OaiUtil.getXpathNode("//oai_id:responseDate", this.descrNamespaceNodes, xml.getRootElement());
                if (node != null) {
                    if (node.getTextTrim().equals("")) {
                        throw new OAIException(4, "could not find responseDate in Identify (maybe the Identify return is wrong...)");
                    }
                    this.strResponseDate = node.getText();
                } else if (this.validation == 2) {
                    this.strResponseDate = "";
                } else {
                    throw new OAIException(4, "Identify missing responseDate");
                }
                node = OaiUtil.getXpathNode("//oai_id:requestURL | //oai_id:request", this.descrNamespaceNodes, xml.getRootElement());
                if (node != null) {
                    this.ixmlRequest = node;
                } else if (this.validation == 2) {
                    this.ixmlRequest = null;
                } else {
                    throw new OAIException(4, "Identify missing requestURL");
                }
                this.state = 1;
            }
            catch (JDOMException te) {
                throw new OAIException(14, te.getMessage());
            }
            url = null;
            docFactory = null;
            docBuilder = null;
        }
        catch (IOException ie) {
            throw new OAIException(14, ie.getMessage());
        }
        catch (FactoryConfigurationError fce) {
            throw new OAIException(14, fce.getMessage());
        }
        return this.strRepositoryName;
    }

    public OAIRecordList listIdentifiers() throws OAIException {
        return this.listIdentifiers("", "", "", "oai_dc");
    }

    public OAIRecordList listIdentifiers(String untild) throws OAIException {
        return this.listIdentifiers(untild, "", "", "oai_dc");
    }

    public OAIRecordList listIdentifiers(String untild, String fromd) throws OAIException {
        return this.listIdentifiers(untild, fromd, "", "oai_dc");
    }

    public OAIRecordList listIdentifiers(String untild, String fromd, String setSpec) throws OAIException {
        return this.listIdentifiers(untild, fromd, setSpec, "oai_dc");
    }

    public OAIRecordList listIdentifiers(String untild, String fromd, String setSpec, String metadataPrefix) throws OAIException {
        this.priCheckBaseURL();
        String prefix = metadataPrefix;
        if (this.getProtocolMajorVersion() > 1 && metadataPrefix.length() == 0) {
            prefix = "oai_dc";
        }
        String params = this.priBuildParamString(untild, fromd, setSpec, "", prefix);
        OAIResumptionStream rs = new OAIResumptionStream(this, this.strBaseURL, "ListIdentifiers", params);
        OAIRecordList sets = new OAIRecordList();
        sets.frndSetMetadataPrefix(metadataPrefix);
        sets.frndSetOAIResumptionStream(rs);
        return sets;
    }

    public OAIRecordList listRecords() throws OAIException {
        return this.listRecords("oai_dc", "", "", "");
    }

    public OAIRecordList listRecords(String metadataPrefix) throws OAIException {
        return this.listRecords(metadataPrefix, "", "", "");
    }

    public OAIRecordList listRecords(String metadataPrefix, String untild) throws OAIException {
        return this.listRecords(metadataPrefix, untild, "", "");
    }

    public OAIRecordList listRecords(String metadataPrefix, String untild, String fromd) throws OAIException {
        return this.listRecords(metadataPrefix, untild, fromd, "");
    }

    public OAIRecordList listRecords(String metadataPrefix, String untild, String fromd, String setSpec) throws OAIException {
        this.priCheckBaseURL();
        String prefix = metadataPrefix;
        prefix = metadataPrefix.length() == 0 ? "oai_dc" : metadataPrefix;
        String params = this.priBuildParamString(untild, fromd, setSpec, "", prefix);
        OAIResumptionStream rs = new OAIResumptionStream(this, this.strBaseURL, "ListRecords", params);
        OAIRecordList sets = new OAIRecordList();
        sets.frndSetMetadataPrefix(metadataPrefix);
        sets.frndSetOAIResumptionStream(rs);
        return sets;
    }

    public OAIRecordList listAllRecords(String metadataPrefix, String setSpec) throws OAIException {
        this.priCheckBaseURL();
        String prefix = metadataPrefix;
        prefix = metadataPrefix.length() == 0 ? "oai_dc" : metadataPrefix;
        String params = this.priBuildParamString(null, null, setSpec, "", prefix);
        OAIResumptionStream rs = new OAIResumptionStream(this, this.strBaseURL, "ListRecords", params);
        OAIRecordList sets = new OAIRecordList();
        sets.frndSetMetadataPrefix(metadataPrefix);
        sets.frndSetOAIResumptionStream(rs);
        return sets;
    }

    public String getRepositoryIdentifier() throws OAIException {
        String ret = "";
        if (this.state == 0) {
            this.identify();
        }
        if (!(this.usesOAIIdentifier() || this.strRepositoryId != null && this.strRepositoryId.length() != 0 || this.validation != 1)) {
            throw new OAIException(8, "The RepositoryIdentifier is unknown");
        }
        if (!(this.usesOAIIdentifier() || this.strRepositoryId != null && this.strRepositoryId.length() != 0)) {
            return ret;
        }
        try {
            for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
                Element node = OaiUtil.getXpathNode("//oai_id:description/id:oai-identifier/id:repositoryIdentifier", this.descrNamespaceNodes, (Element)this.ixmlDescriptions.get(i));
                if (node == null) continue;
                ret = node.getText();
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public Element getOAIIdentifierDescription() throws OAIException {
        Element node = null;
        Element ret = null;
        if (this.state == 0) {
            this.identify();
        }
        if (!this.usesOAIIdentifier()) {
            throw new OAIException(8, "The RepositoryIdentifier is unknown");
        }
        for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
            node = (Element)this.ixmlDescriptions.get(i);
            if (!node.getNamespaceURI().equals(XMLNS_ID) && !node.getNamespaceURI().equals(XMLNS_ID_1_0) && !node.getNamespaceURI().equals("XMLNS_ID_1_0_aps")) continue;
            ret = node;
            break;
        }
        return ret;
    }

    public Element getEPrintsDescription() throws OAIException {
        Element ret = null;
        if (this.state == 0) {
            this.identify();
        }
        if (!this.usesOAIIdentifier()) {
            throw new OAIException(8, "The RepositoryIdentifier is unknown");
        }
        for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
            Element node = (Element)this.ixmlDescriptions.get(i);
            if (!node.getNamespaceURI().equals(XMLNS_EPR) && !node.getNamespaceURI().equals(XMLNS_EPR_1_0)) continue;
            ret = node;
            break;
        }
        return ret;
    }

    public void setRetryLimit(int rl) {
        this.iRetryLimit = rl;
    }

    public int getRetryLimit() {
        return this.iRetryLimit;
    }

    public String getSampleIdentifier() throws OAIException {
        String ret = "";
        if (this.state == 0) {
            this.identify();
        }
        if (!this.usesOAIIdentifier() && this.validation == 1) {
            throw new OAIException(8, "The RepositoryIdentifier is unknown");
        }
        if (!this.usesOAIIdentifier()) {
            return ret;
        }
        try {
            for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
                Element node = OaiUtil.getXpathNode("//oai_id:description/id:oai-identifier/id:sampleIdentifier", this.descrNamespaceNodes, (Element)this.ixmlDescriptions.get(i));
                if (ret == null) continue;
                ret = node.getText();
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public String getRepositoryName() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        return this.strRepositoryName;
    }

    public String getAdminEmail() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        return this.getAdminEmail(0);
    }

    public String getAdminEmail(int i) throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.getAdminEmailCount() > 0 && i < this.getAdminEmailCount()) {
            return this.strAdminEmail[i];
        }
        return "";
    }

    public String getCompression() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        return this.getCompression(0);
    }

    public String getCompression(int i) throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        if (this.getCompressionCount() > 0 && i < this.getCompressionCount()) {
            return this.strCompression[i];
        }
        return "";
    }

    public int getProtocolMajorVersion() throws OAIException {
        int ver = 0;
        if (this.state == 0) {
            this.identify();
        }
        try {
            ver = Integer.parseInt(this.strProtocolVersion.trim().substring(0, 1));
        }
        catch (NumberFormatException ne) {
            ver = 0;
        }
        return ver;
    }

    public String getProtocolVersion() throws OAIException {
        if (this.state == 0) {
            this.identify();
        }
        return this.strProtocolVersion;
    }

    public String getRequestURL() {
        String ret = this.ixmlRequest.getText();
        Attribute n = this.ixmlRequest.getAttribute("verb");
        if (n != null) {
            ret = ret + "?verb=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("identifier")) != null) {
            ret = ret + "&identifier=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("metadataPrefix")) != null) {
            ret = ret + "&metadataPrefix=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("from")) != null) {
            ret = ret + "&from=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("until")) != null) {
            ret = ret + "&until=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("set")) != null) {
            ret = ret + "&set=" + n.getValue();
        }
        if ((n = this.ixmlRequest.getAttribute("resumptionToken")) != null) {
            try {
                ret = ret + "&resumptionToken=" + URLEncoder.encode(n.getValue(), "UTF-8");
            }
            catch (UnsupportedEncodingException ex) {
                ret = ret + "&resumptionToken=" + n.getValue();
            }
        }
        return ret;
    }

    public String getUserAgent() {
        return this.strUserAgent;
    }

    public void setUserAgent(String ua) {
        this.strUserAgent = ua;
    }

    public String getFrom() {
        return this.strFrom;
    }

    public void setFrom(String f) {
        this.strFrom = f;
    }

    protected HttpURLConnection frndTrySend(HttpURLConnection h) throws OAIException {
        HttpURLConnection http = h;
        boolean done = false;
        GregorianCalendar sendTime = new GregorianCalendar();
        GregorianCalendar testTime = new GregorianCalendar();
        GregorianCalendar retryTime = null;
        int retryCount = 0;
        do {
            try {
                http.setRequestProperty("User-Agent", this.strUserAgent);
                http.setRequestProperty("From", this.strFrom);
                if (this.strUser != null && this.strUser.length() > 0) {
                    byte[] encodedPassword = (this.strUser + ":" + this.strPassword).getBytes();
                    BASE64Encoder encoder = new BASE64Encoder();
                    http.setRequestProperty("Authorization", "Basic " + encoder.encode(encodedPassword));
                }
                sendTime.setTime(new Date());
                http.connect();
                if (http.getResponseCode() == 200) {
                    done = true;
                    continue;
                }
                if (http.getResponseCode() == 503) {
                    if (++retryCount > this.iRetryLimit) {
                        throw new OAIException(11, "The RetryLimit " + this.iRetryLimit + " has been exceeded");
                    }
                    String retryAfter = http.getHeaderField("Retry-After");
                    if (retryAfter == null) {
                        throw new OAIException(10, "No Retry-After header");
                    }
                    try {
                        int sec = Integer.parseInt(retryAfter);
                        sendTime.add(13, sec);
                        retryTime = sendTime;
                    }
                    catch (NumberFormatException ne) {
                        try {
                            Date retryDate = DateFormat.getDateInstance().parse(retryAfter);
                            retryTime = new GregorianCalendar();
                            retryTime.setTime(retryDate);
                        }
                        catch (ParseException pe) {
                            throw new OAIException(14, pe.getMessage());
                        }
                    }
                    if (retryTime != null) {
                        testTime.setTime(new Date());
                        testTime.add(12, this.iMaxRetryMinutes);
                        if (retryTime.getTime().before(testTime.getTime())) {
                            try {
                                while (retryTime.getTime().after(new Date())) {
                                    Thread.sleep(10000);
                                }
                                URL url = new URL(http.getURL().toString());
                                http = (HttpURLConnection)url.openConnection();
                                continue;
                            }
                            catch (InterruptedException ie) {
                                throw new OAIException(14, ie.getMessage());
                            }
                        }
                        throw new OAIException(10, "Retry time(" + retryAfter + " sec) is too long");
                    }
                    throw new OAIException(10, retryAfter + " is not a valid Retry-After header");
                }
                if (http.getResponseCode() == 403) {
                    throw new OAIException(14, http.getResponseMessage());
                }
                if (++retryCount > this.iRetryLimit) {
                    throw new OAIException(11, "The RetryLimit " + this.iRetryLimit + " has been exceeded");
                }
                int sec = 10 * (int)Math.exp(retryCount);
                sendTime.add(13, sec);
                retryTime = sendTime;
                try {
                    while (retryTime.getTime().after(new Date())) {
                        Thread.sleep(sec * 1000);
                    }
                    URL url = new URL(http.getURL().toString());
                    http = (HttpURLConnection)url.openConnection();
                    continue;
                }
                catch (InterruptedException ie) {
                    throw new OAIException(14, ie.getMessage());
                }
            }
            catch (IOException ie) {
                throw new OAIException(14, ie.getMessage());
            }
        } while (!done);
        return http;
    }

    public boolean usesOAIIdentifier() throws OAIException {
        boolean ret;
        ret = false;
        if (this.state == 0) {
            this.identify();
        }
        try {
            for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
                Element node = OaiUtil.getXpathNode("//oai_id:description/*", this.descrNamespaceNodes, (Element)this.ixmlDescriptions.get(i));
                if (node == null || !node.getNamespaceURI().equals(XMLNS_ID) && !node.getNamespaceURI().equals(XMLNS_ID_1_0) && !node.getNamespaceURI().equals(XMLNS_ID_2_0) && !node.getNamespaceURI().equals("XMLNS_ID_1_0_aps")) continue;
                ret = true;
                break;
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public boolean usesEPrints() throws OAIException {
        boolean ret;
        ret = false;
        if (this.state == 0) {
            this.identify();
        }
        try {
            for (int i = 0; i < this.ixmlDescriptions.size(); ++i) {
                Element node = OaiUtil.getXpathNode("//oai_id:description/*", this.descrNamespaceNodes, (Element)this.ixmlDescriptions.get(i));
                if (node == null || !node.getNamespaceURI().equals(XMLNS_EPR) && !node.getNamespaceURI().equals(XMLNS_EPR_1_0)) continue;
                ret = true;
                break;
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public int getValidation() {
        return this.validation;
    }

    public void setValidation(int v) {
        this.validation = v;
    }

    public OAIMetadataFormatList listMetadataFormats() throws OAIException {
        return this.listMetadataFormats("");
    }

    public OAIMetadataFormatList listMetadataFormats(String identifier) throws OAIException {
        OAIMetadataFormatList sets = new OAIMetadataFormatList();
        this.priCheckBaseURL();
        String params = this.priBuildParamString("", "", "", identifier, "");
        OAIResumptionStream rs = new OAIResumptionStream(this, this.strBaseURL, "ListMetadataFormats", params);
        sets.frndSetOAIResumptionStream(rs);
        return sets;
    }

    public OAISetList listSets() throws OAIException {
        OAISetList sets = new OAISetList();
        this.priCheckBaseURL();
        OAIResumptionStream rs = new OAIResumptionStream(this, this.strBaseURL, "ListSets");
        sets.frndSetOAIResumptionStream(rs);
        return sets;
    }

    public boolean isFixXmlEncoding() {
        return this.fixXmlEncoding;
    }

    public void setFixXmlEncoding(boolean fixXmlEncoding) {
        this.fixXmlEncoding = fixXmlEncoding;
    }
}

