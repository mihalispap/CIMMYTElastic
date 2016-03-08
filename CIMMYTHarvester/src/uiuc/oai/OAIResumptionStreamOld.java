/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.ariadne.util.IOUtilsv2
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
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import org.ariadne.util.IOUtilsv2;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;
import uiuc.oai.OAIError;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRepository;
import uiuc.oai.OaiUtil;

public class OAIResumptionStreamOld {
    private String strBaseURL;
    private String strVerb;
    private String strParams;
    private String strResumptionToken;
    private String strExpirationDate;
    private Document xml;
    private List nodeList;
    private int iIndex;
    private int iCompleteListSize;
    private int iCursor;
    private int iCount;
    private int iRealCursor;
    private Vector<Namespace> namespaces;
    private boolean boolInitialized;
    private boolean boolValidResponse;
    private int iResumptionCount;
    private OAIRepository oParent;

    public OAIResumptionStreamOld(OAIRepository repo, String u, String v) throws OAIException {
        this.initialize(repo, u, v, "");
    }

    public OAIResumptionStreamOld(OAIRepository repo, String u, String v, String params) throws OAIException {
        this.initialize(repo, u, v, params);
    }

    private void initialize(OAIRepository repo, String u, String v, String params) throws OAIException {
        this.oParent = repo;
        this.strVerb = v;
        this.strBaseURL = u;
        this.strParams = params;
        this.strResumptionToken = "";
        this.iResumptionCount = 0;
        this.boolInitialized = false;
        this.boolValidResponse = false;
        this.iIndex = 1;
        this.iCount = -1;
        this.iCursor = -1;
        this.iRealCursor = -1;
        this.iCompleteListSize = -1;
        if (!(this.strVerb.equals("ListIdentifiers") || this.strVerb.equals("ListMetadataFormats") || this.strVerb.equals("ListRecords") || this.strVerb.equals("ListSets"))) {
            throw new OAIException(5, "Invalid verb");
        }
        if (this.strBaseURL.length() == 0) {
            throw new OAIException(6, "No baseURL");
        }
        if (params.length() > 0 && params.charAt(0) != '&') {
            params = "&" + params;
        }
        try {
            URL url = new URL(this.strBaseURL + "?verb=" + this.strVerb + params);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http = this.oParent.frndTrySend(http);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            if (this.oParent.getValidation() == 1) {
                docFactory.setValidating(true);
            } else {
                docFactory.setValidating(false);
            }
            SAXBuilder docBuilder = new SAXBuilder();
            InputStream inputStream = http.getInputStream();
            try {
                this.xml = docBuilder.build(inputStream);
                this.boolValidResponse = true;
            }
            catch (IllegalArgumentException iae) {
                throw new OAIException(14, iae.getMessage());
            }
            catch (JDOMException se) {
                if (this.oParent.isFixXmlEncoding()) {
                    System.out.println("Invalid characters found !!!! Error : " + se.getMessage());
                    http.disconnect();
                    url = new URL(this.strBaseURL + "?verb=" + this.strVerb + params);
                    http = (HttpURLConnection)url.openConnection();
                    http = this.oParent.frndTrySend(http);
                    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    try {
                        String pattern = "[^\\x20-\\x7e]";
                        IOUtilsv2.writeStringToFileInEncodingUTF8((String)buffer.toString(), (String)("oaireturn-" + GregorianCalendar.getInstance().getTimeInMillis() + "-" + se.getMessage().replaceAll(" ", "_") + ".xml"));
                        this.xml = docBuilder.build((Reader)new StringReader(buffer.toString().replaceAll(pattern, " ")));
                    }
                    catch (JDOMException e) {
                        throw new OAIException(13, e.getMessage());
                    }
                }
                if (this.oParent.getValidation() != 2) {
                    throw new OAIException(13, se.getMessage() + " Try loose validation.");
                }
                try {
                    http.disconnect();
                    url = new URL(this.strBaseURL + "?verb=" + this.strVerb + params);
                    http = (HttpURLConnection)url.openConnection();
                    http = this.oParent.frndTrySend(http);
                    this.xml = docBuilder.build(this.priCreateDummyResponse(inputStream));
                }
                catch (JDOMException se2) {
                    throw new OAIException(13, se2.getMessage());
                }
            }
            this.setNameSpace();
            this.nodeList = OaiUtil.getXpathList("//oai:" + this.strVerb + "/oai:" + this.priGetMainNodeName(), this.namespaces, this.xml.getRootElement());
            this.boolInitialized = true;
            this.oParent.frndSetNamespaceNode(this.namespaces);
            Element node = OaiUtil.getXpathNode("//oai:requestURL | //oai:request", this.namespaces, this.xml.getRootElement());
            if (node != null) {
                this.oParent.frndSetRequest(node);
            }
            this.oParent.frndSetResponseDate(this.getResponseDate());
            docFactory = null;
            docBuilder = null;
            url = null;
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
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
    }

    private void setNameSpace() throws JDOMException, OAIException {
        this.namespaces = new Vector();
        this.namespaces.add(Namespace.getNamespace((String)"oai", (String)(OAIRepository.XMLNS_OAI + this.strVerb)));
        this.namespaces.add(Namespace.getNamespace((String)"dc", (String)OAIRepository.XMLNS_DC));
        Element node = OaiUtil.getXpathNode("//oai:" + this.strVerb + "/oai:" + this.priGetMainNodeName(), this.namespaces, this.xml.getRootElement());
        if (node == null) {
            List nl;
            this.namespaces = new Vector();
            this.namespaces.add(Namespace.getNamespace((String)"oai", (String)OAIRepository.XMLNS_OAI_2_0));
            node = OaiUtil.getXpathNode("/oai:OAI-PMH", this.namespaces, this.xml.getRootElement());
            if (node != null && (nl = OaiUtil.getXpathList("oai:OAI-PMH/oai:error", this.namespaces, this.xml.getRootElement())).size() > 0) {
                this.oParent.frndSetErrors(nl);
                throw new OAIException(15, this.oParent.getLastOAIError().getCode() + ": " + this.oParent.getLastOAIError().getReason());
            }
        }
    }

    public int getCompleteSize() throws OAIException {
        this.priCheckInitialized();
        this.priGetResumptionToken();
        return this.iCompleteListSize;
    }

    public int getResumptionCursor() throws OAIException {
        this.priCheckInitialized();
        this.priGetResumptionToken();
        if (this.iCursor == -1) {
            return this.iRealCursor;
        }
        return this.iCursor;
    }

    public String getResumptionExpirationDate() throws OAIException {
        this.priCheckInitialized();
        this.priGetResumptionToken();
        return this.strExpirationDate;
    }

    public int getIndex() throws OAIException {
        return this.getResumptionCursor() + this.iIndex - 1;
    }

    public boolean isResponseValid() {
        return this.boolValidResponse;
    }

    public boolean more() throws OAIException {
        boolean ret = false;
        this.priCheckInitialized();
        if (this.strResumptionToken.length() > 0 || this.iIndex <= this.priGetSetCount()) {
            ret = true;
        }
        return ret;
    }

    public void moveNext() throws OAIException {
        this.priCheckInitialized();
        int cnt = this.priGetSetCount();
        if (this.more()) {
            if (this.iIndex <= cnt) {
                ++this.iIndex;
            }
            if (this.iIndex > cnt) {
                this.priResumption();
            }
        } else {
            throw new OAIException(7, "No more sets");
        }
    }

    public String getBaseURL() throws OAIException {
        this.priCheckInitialized();
        return this.strBaseURL;
    }

    public String getResponseDate() throws OAIException {
        String ret = "";
        this.priCheckInitialized();
        Element rootElement = this.xml.getRootElement();
        String name = ((Element)rootElement.getChildren().get(0)).getName();
        Element response = rootElement.getChild("responseDate", rootElement.getNamespace());
        if (response == null) {
            throw new OAIException(4, this.strVerb + " missing responseDate");
        }
        ret = response.getText();
        return ret;
    }

    public String getRequestURL() throws OAIException {
        String ret;
        block13 : {
            this.priCheckInitialized();
            ret = "";
            try {
                XPath xpath = XPath.newInstance((String)"//oai:requestURL | //oai:request");
                for (Namespace ns : this.namespaces) {
                    xpath.addNamespace(ns);
                }
                Element node = OaiUtil.getXpathNode("//oai:requestURL | //oai:request", this.namespaces, this.xml.getRootElement());
                if (node != null) {
                    ret = node.getText();
                    Attribute n = node.getAttribute("verb");
                    if (n != null) {
                        ret = ret + "?verb=" + n.getValue();
                    }
                    if ((n = node.getAttribute("identifier")) != null) {
                        ret = ret + "&identifier=" + n.getValue();
                    }
                    if ((n = node.getAttribute("metadataPrefix")) != null) {
                        ret = ret + "&metadataPrefix=" + n.getValue();
                    }
                    if ((n = node.getAttribute("from")) != null) {
                        ret = ret + "&from=" + n.getValue();
                    }
                    if ((n = node.getAttribute("until")) != null) {
                        ret = ret + "&until=" + n.getValue();
                    }
                    if ((n = node.getAttribute("set")) != null) {
                        ret = ret + "&set=" + n.getValue();
                    }
                    if ((n = node.getAttribute("resumptionToken")) != null) {
                        try {
                            ret = ret + "&resumptionToken=" + URLEncoder.encode(n.getValue(), "UTF-8");
                        }
                        catch (UnsupportedEncodingException ex) {
                            ret = ret + "&resumptionToken=" + n.getValue();
                        }
                    }
                    break block13;
                }
                throw new OAIException(4, this.strVerb + " missing requestURL/request");
            }
            catch (JDOMException te) {
                throw new OAIException(14, te.getMessage());
            }
        }
        return ret;
    }

    public Element getItem() throws OAIException {
        this.priCheckInitialized();
        return this.priGetXMLItem(this.iIndex);
    }

    public String getParams() throws OAIException {
        this.priCheckInitialized();
        return this.strParams;
    }

    public OAIRepository getRepository() {
        return this.oParent;
    }

    public String getVerb() throws OAIException {
        this.priCheckInitialized();
        return this.strVerb;
    }

    public void requery() throws OAIException {
        this.priCheckInitialized();
        this.initialize(this.oParent, this.strBaseURL, this.strVerb, this.strParams);
    }

    private void priCheckInitialized() throws OAIException {
        if (!this.boolInitialized) {
            throw new OAIException(9, "Not initialized");
        }
    }

    private Element priGetXMLItem(int i) {
        if (this.nodeList != null && i <= this.nodeList.size()) {
            return (Element)this.nodeList.get(i - 1);
        }
        return null;
    }

    private int priGetSetCount() {
        if (this.iCount >= 0) {
            return this.iCount;
        }
        this.iCount = this.nodeList.size();
        return this.iCount;
    }

    public int getResponseSize() throws OAIException {
        this.priCheckInitialized();
        return this.priGetSetCount();
    }

    private String priGetResumptionToken() throws OAIException {
        try {
            Element node = OaiUtil.getXpathNode("//oai:" + this.strVerb + "/oai:resumptionToken", this.namespaces, this.xml.getRootElement());
            if (node != null) {
                this.strResumptionToken = node.getTextTrim();
                Attribute n = node.getAttribute("expirationDate");
                this.strExpirationDate = n != null ? n.getValue() : "";
                n = node.getAttribute("completeListSize");
                if (n != null) {
                    try {
                        this.iCompleteListSize = Integer.parseInt(n.getValue());
                    }
                    catch (NumberFormatException ne) {
                        this.iCompleteListSize = -1;
                    }
                } else {
                    this.iCompleteListSize = -1;
                }
                if ((n = node.getAttribute("cursor")) != null) {
                    try {
                        this.iCursor = Integer.parseInt(n.getValue());
                    }
                    catch (NumberFormatException ne) {
                        this.iCursor = -1;
                    }
                } else {
                    this.iCursor = -1;
                }
            } else {
                this.strResumptionToken = "";
                this.strExpirationDate = "";
                this.iCompleteListSize = -1;
                this.iCursor = -1;
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        if (this.strResumptionToken.length() == 0 && this.iCompleteListSize == -1) {
            this.iCompleteListSize = this.iCursor == -1 ? (this.iRealCursor == -1 ? this.priGetSetCount() : this.priGetSetCount() + this.iRealCursor) : this.priGetSetCount() + this.iCursor;
        }
        return this.strResumptionToken;
    }

    private String priGetMainNodeName() throws OAIException {
        String ret;
        if (this.strVerb.equals("ListSets")) {
            ret = "set";
        } else if (this.strVerb.equals("ListIdentifiers")) {
            ret = this.oParent.getProtocolMajorVersion() < 2 ? "identifier" : "header";
        } else if (this.strVerb.equals("ListMetadataFormats")) {
            ret = "metadataFormat";
        } else if (this.strVerb.equals("ListRecords")) {
            ret = "record";
        } else {
            throw new OAIException(5, "Invalid verb");
        }
        return ret;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private InputSource priCreateDummyResponse(InputStream x) throws OAIException {
        String ret;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (this.oParent.getProtocolMajorVersion() < 2) {
            if (this.strVerb.equals("ListSets")) {
                ret = "<ListSets xmlns='http://www.openarchives.org/OAI/1.1/OAI_ListSets' \n";
                ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
                ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_ListSets ";
                ret = ret + "http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd'> \n";
                ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
                ret = ret + "<requestURL>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</requestURL> \n";
                ret = ret + "<set> \n";
                ret = ret + "<setSpec>junk:set" + this.iResumptionCount + "</setSpec> \n";
                ret = ret + "<setName><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></setName> \n";
                ret = ret + "</set> \n";
                ret = ret + this.priTryToGetResumptionToken(x) + "\n";
                ret = ret + "</ListSets>\n";
            } else if (this.strVerb.equals("ListIdentifiers")) {
                ret = "<ListIdentifiers xmlns='http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers' \n";
                ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
                ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers ";
                ret = ret + "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd'> \n";
                ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
                ret = ret + "<requestURL>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</requestURL> \n";
                ret = ret + "<identifier><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></identifier> \n";
                ret = ret + this.priTryToGetResumptionToken(x) + "\n";
                ret = ret + "</ListIdentifiers>\n";
            } else if (this.strVerb.equals("ListMetadataFormats")) {
                ret = "<ListMetadataFormats xmlns='http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats' \n";
                ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
                ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats ";
                ret = ret + "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd'> \n";
                ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
                ret = ret + "<requestURL>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</requestURL> \n";
                ret = ret + "<metadataFormat> \n";
                ret = ret + "<metadataPrefix>junk_metadataPrefix" + this.iResumptionCount + "</metadataPrefix> \n";
                ret = ret + "<schema><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></schema> \n";
                ret = ret + "</metadataFormat> \n";
                ret = ret + this.priTryToGetResumptionToken(x) + "\n";
                ret = ret + "</ListMetadataFormats>\n";
            } else {
                if (!this.strVerb.equals("ListRecords")) throw new OAIException(5, "Invalid verb");
                ret = "<ListRecords \n";
                ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
                ret = ret + "xmlns='http://www.openarchives.org/OAI/1.1/OAI_ListRecords' \n";
                ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/1.1/OAI_ListRecords ";
                ret = ret + "http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd'>\n";
                ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate>\n";
                ret = ret + "<requestURL>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</requestURL>";
                ret = ret + "<record>\n";
                ret = ret + "<header>\n";
                ret = this.oParent.usesOAIIdentifier() ? ret + "<identifier>" + this.oParent.getRepositoryIdentifier() + "junk:identifier" + this.iResumptionCount + "</identifier>\n" : ret + "<identifier>junk:identifier" + this.iResumptionCount + "</identifier>\n";
                ret = ret + "<datestamp>" + formatter.format(new Date()) + "</datestamp>\n";
                ret = ret + "</header>\n";
                ret = ret + "<about>\n";
                ret = ret + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></junk:junk>\n";
                ret = ret + "</about>\n";
                ret = ret + "</record>\n";
                ret = ret + this.priTryToGetResumptionToken(x) + "\n";
                ret = ret + "</ListRecords>\n";
            }
        } else if (this.strVerb.equals("ListSets")) {
            ret = "<OAI-PMH xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            ret = ret + "http://www.openarchives.org/OAI/2.0/OAI_PMH.xsd'> \n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
            ret = ret + "<request>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</request> \n";
            ret = ret + "<ListSets> \n";
            ret = ret + "<set> \n";
            ret = ret + "<setSpec>junk:set" + this.iResumptionCount + "</setSpec> \n";
            ret = ret + "<setName>INVALID SET</setName> \n";
            ret = ret + "<setDescription><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></setDescription> \n";
            ret = ret + "</set> \n";
            ret = ret + this.priTryToGetResumptionToken(x) + "\n";
            ret = ret + "</ListSets>\n";
            ret = ret + "</OAI-PMH>\n";
        } else if (this.strVerb.equals("ListIdentifiers")) {
            ret = "<OAI-PMH xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            ret = ret + "http://www.openarchives.org/OAI/2.0/OAI_PMH.xsd'> \n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
            ret = ret + "<request>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</request> \n";
            ret = ret + "<ListIdentifiers>\n";
            ret = ret + "<header>\n";
            ret = this.oParent.usesOAIIdentifier() ? ret + "<identifier>" + this.oParent.getRepositoryIdentifier() + "junk:identifier" + this.iResumptionCount + "</identifier>\n" : ret + "<identifier>junk:identifier" + this.iResumptionCount + "</identifier>\n";
            ret = ret + "<datestamp>" + formatter.format(new Date()) + "</datestamp>\n";
            ret = ret + "<setSpec><![CDATA[" + this.oParent.frndMyEncode(x) + "]]>\n";
            ret = ret + "</header>\n";
            ret = ret + this.priTryToGetResumptionToken(x) + "\n";
            ret = ret + "</ListIdentifiers>\n";
            ret = ret + "</OAI-PMH>\n";
        } else if (this.strVerb.equals("ListMetadataFormats")) {
            ret = "<OAI-PMH xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            ret = ret + "http://www.openarchives.org/OAI/2.0/OAI_PMH.xsd'> \n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
            ret = ret + "<request>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</request> \n";
            ret = ret + "<metadataFormat> \n";
            ret = ret + "<metadataPrefix>junk_metadataPrefix" + this.iResumptionCount + "</metadataPrefix> \n";
            ret = ret + "<schema><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></schema> \n";
            ret = ret + "</metadataFormat> \n";
            ret = ret + this.priTryToGetResumptionToken(x) + "\n";
            ret = ret + "</ListMetadataFormats>\n";
            ret = ret + "</OAI-PMH>\n";
        } else {
            if (!this.strVerb.equals("ListRecords")) throw new OAIException(5, "Invalid verb");
            ret = "<OAI-PMH xmlns='http://www.openarchives.org/OAI/2.0/' \n";
            ret = ret + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n";
            ret = ret + "xsi:schemaLocation='http://www.openarchives.org/OAI/2.0/ ";
            ret = ret + "http://www.openarchives.org/OAI/2.0/OAI_PMH.xsd'> \n";
            ret = ret + "<responseDate>" + formatter.format(new Date()) + "</responseDate> \n";
            ret = ret + "<request>" + this.strBaseURL + "?verb=" + this.strVerb + this.priXMLEncode(this.strParams) + "</request> \n";
            ret = ret + "<record>\n";
            ret = ret + "<header>\n";
            ret = this.oParent.usesOAIIdentifier() ? ret + "<identifier>" + this.oParent.getRepositoryIdentifier() + "junk:identifier" + this.iResumptionCount + "</identifier>\n" : ret + "<identifier>junk:identifier" + this.iResumptionCount + "</identifier>\n";
            ret = ret + "<datestamp>" + formatter.format(new Date()) + "</datestamp>\n";
            ret = ret + "</header>\n";
            ret = ret + "<about>\n";
            ret = ret + "<junk:junk xmlns:junk='junk:junk'><![CDATA[" + this.oParent.frndMyEncode(x) + "]]></junk:junk>\n";
            ret = ret + "</about>\n";
            ret = ret + "</record>\n";
            ret = ret + this.priTryToGetResumptionToken(x) + "\n";
            ret = ret + "</ListRecords>\n";
            ret = ret + "</OAI-PMH>\n";
        }
        StringReader sr = new StringReader(ret);
        return new InputSource(sr);
    }

    private String priTryToGetResumptionToken(InputStream x) throws OAIException {
        String ret;
        ret = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(x, "UTF-8"));
            String tmp = "";
            int startIdx = -1;
            int endIdx = -1;
            while ((tmp = br.readLine()) != null) {
                if (startIdx < 0 && (endIdx = (ret = tmp.substring((startIdx = tmp.indexOf("<resumptionToken")) + 17)).indexOf("</resumptionToken")) > 0) {
                    ret = ret.substring(0, endIdx);
                } else {
                    if (startIdx <= 0 || (endIdx = (ret = ret + tmp).indexOf("</resumptionToken")) <= 0) continue;
                    ret = ret.substring(0, endIdx);
                }
                break;
            }
        }
        catch (IOException ie) {
            throw new OAIException(14, ie.getMessage());
        }
        return ret;
    }

    private String priXMLEncode(String s) {
        String ret = s;
        int idx = 0;
        while ((idx = ret.indexOf(38, idx)) >= 0) {
            ret = ret.substring(0, idx) + "&amp;" + ret.substring(idx + 1);
            idx += 4;
        }
        return ret;
    }

    private void priResumption() throws OAIException {
        String rt = this.priGetResumptionToken();
        if (rt.length() == 0) {
            return;
        }
        int prevCount = this.priGetSetCount();
        this.iCount = -1;
        ++this.iResumptionCount;
        try {
            URL url = new URL(this.strBaseURL + "?verb=" + this.strVerb + "&resumptionToken=" + URLEncoder.encode(rt, "UTF-8"));
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http = this.oParent.frndTrySend(http);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            if (this.oParent.getValidation() == 1) {
                docFactory.setValidating(true);
            } else {
                docFactory.setValidating(false);
            }
            SAXBuilder docBuilder = new SAXBuilder();
            InputStream inputStream = http.getInputStream();
            try {
                this.xml = docBuilder.build(inputStream);
                this.boolValidResponse = true;
            }
            catch (IllegalArgumentException iae) {
                throw new OAIException(14, iae.getMessage());
            }
            catch (JDOMException se) {
                if (this.oParent.isFixXmlEncoding()) {
                    System.out.println("Invalid characters found !!!! " + se.getMessage());
                    http.disconnect();
                    url = new URL(this.strBaseURL + "?verb=" + this.strVerb + "&resumptionToken=" + URLEncoder.encode(rt, "UTF-8"));
                    http = (HttpURLConnection)url.openConnection();
                    http = this.oParent.frndTrySend(http);
                    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    try {
                        String pattern = "[^\\x20-\\x7e]";
                        IOUtilsv2.writeStringToFileInEncodingUTF8((String)buffer.toString(), (String)("oaireturn-" + GregorianCalendar.getInstance().getTimeInMillis() + "-" + se.getMessage().replaceAll(" ", "_") + ".xml"));
                        this.xml = docBuilder.build((Reader)new StringReader(buffer.toString().replaceAll(pattern, " ")));
                    }
                    catch (JDOMException e) {
                        throw new OAIException(13, e.getMessage());
                    }
                }
                if (this.oParent.getValidation() != 2) {
                    throw new OAIException(13, se.getMessage() + " Try loose validation.");
                }
                try {
                    http.disconnect();
                    url = new URL(this.strBaseURL + "?verb=" + this.strVerb + "&resumptionToken=" + URLEncoder.encode(rt, "UTF-8"));
                    http = (HttpURLConnection)url.openConnection();
                    http = this.oParent.frndTrySend(http);
                    this.xml = docBuilder.build(this.priCreateDummyResponse(inputStream));
                }
                catch (JDOMException se2) {
                    throw new OAIException(13, se2.getMessage());
                }
            }
            try {
                this.setNameSpace();
                XPath xpath = XPath.newInstance((String)("//oai:" + this.strVerb + "/oai:" + this.priGetMainNodeName()));
                for (Namespace ns2 : this.namespaces) {
                    xpath.addNamespace(ns2);
                }
                this.nodeList = xpath.selectNodes((Object)this.xml);
                this.oParent.frndSetNamespaceNode(this.namespaces);
                xpath = XPath.newInstance((String)"//oai:requestURL | //oai:request");
                for (Namespace ns2 : this.namespaces) {
                    xpath.addNamespace(ns2);
                }
                Element node = OaiUtil.getXpathNode("//oai:requestURL | //oai:request", this.namespaces, this.xml.getRootElement());
                if (node != null) {
                    this.oParent.frndSetRequest(node);
                }
                this.oParent.frndSetResponseDate(this.getResponseDate());
                this.iRealCursor += prevCount;
                xpath = null;
            }
            catch (JDOMException te) {
                throw new OAIException(14, te.getMessage());
            }
            catch (IllegalArgumentException iae) {
                throw new OAIException(14, iae.getMessage());
            }
            docFactory = null;
            docBuilder = null;
            url = null;
        }
        catch (MalformedURLException mue) {
            throw new OAIException(14, mue.getMessage());
        }
        catch (UnsupportedEncodingException ex) {
            throw new OAIException(14, ex.getMessage());
        }
        catch (FactoryConfigurationError fce) {
            throw new OAIException(14, fce.getMessage());
        }
        catch (IOException ie) {
            throw new OAIException(14, ie.getMessage());
        }
        this.iIndex = 1;
    }
}

