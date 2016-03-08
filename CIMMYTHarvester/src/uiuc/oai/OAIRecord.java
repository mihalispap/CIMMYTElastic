/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  org.jdom.Content
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.JDOMException
 *  org.jdom.Namespace
 *  org.jdom.xpath.XPath
 */
package uiuc.oai;

import java.util.List;
import java.util.Vector;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import uiuc.oai.OAIException;
import uiuc.oai.OAIRepository;
import uiuc.oai.OaiUtil;

public class OAIRecord {
    private String strStatus = "";
    private String strIdentifier = "";
    private String strDatestamp = "";
    private String strMetadataPrefix = "";
    private boolean boolIdOnly = true;
    private boolean boolValid = true;
    private Element xmlRecord = null;
    private OAIRepository repo;

    public String getMetadataNamespaceURI() throws OAIException {
        String ret = "";
        this.priCheckIdOnly();
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            XPath xpath = XPath.newInstance((String)"//oai:metadata/*");
            xpath.addNamespace(oains);
            Object selectSingleNode = xpath.selectSingleNode((Object)this.xmlRecord);
            Element element = (Element)selectSingleNode;
            if (element != null) {
                ret = element.getNamespaceURI();
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public String getAboutNamespaceURI() throws OAIException {
        return this.getAboutNamespaceURI(0);
    }

    public String getAboutNamespaceURI(int i) throws OAIException {
        String ret = "";
        this.priCheckIdOnly();
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            Vector<Namespace> nsVector = new Vector<Namespace>();
            nsVector.add(oains);
            Element node = OaiUtil.getXpathNode("oai:about[" + (i + 1) + "]/*", nsVector, this.xmlRecord);
            if (node != null) {
                ret = node.getNamespaceURI();
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    private void priCheckIdOnly() throws OAIException {
        if (this.boolIdOnly) {
            this.refreshRecord();
        }
    }

    public String getDatestamp() throws OAIException {
        if (this.repo.getProtocolMajorVersion() < 2) {
            this.priCheckIdOnly();
        }
        return this.strDatestamp;
    }

    public boolean deleted() {
        if (this.strStatus.equals("deleted")) {
            return true;
        }
        return false;
    }

    public boolean isRecordValid() {
        return this.boolValid;
    }

    public String getIdentifier() {
        return this.strIdentifier;
    }

    public String getStatus() {
        return this.strStatus;
    }

    public Element getMetadata() throws OAIException {
        Element ret = null;
        this.priCheckIdOnly();
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            Vector<Namespace> nsVector = new Vector<Namespace>();
            nsVector.add(oains);
            Element node = OaiUtil.getXpathNode("oai:metadata/*", nsVector, this.xmlRecord);
            if (node != null) {
                node = (Element)node.clone();
                node.detach();
                ret = new Document(node).getRootElement();
            }
        }
        catch (JDOMException e) {
            throw new OAIException(14, e.getMessage());
        }
        return ret;
    }

    public int getAboutCount() throws OAIException {
        int ret = 0;
        this.priCheckIdOnly();
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            XPath xpath = XPath.newInstance((String)"oai:about/*");
            xpath.addNamespace(oains);
            List list = xpath.selectNodes((Object)this.xmlRecord);
            ret = list.size();
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public Element getAbout() throws OAIException {
        return this.getAbout(0);
    }

    public Element getAbout(int i) throws OAIException {
        Element ret = null;
        this.priCheckIdOnly();
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            Vector<Namespace> nsVector = new Vector<Namespace>();
            nsVector.add(oains);
            Element node = OaiUtil.getXpathNode("oai:about[" + (i + 1) + "]/*", nsVector, this.xmlRecord);
            if (node != null) {
                ret = new Document((Element)node.detach()).getRootElement();
            }
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public boolean isIdentifierOnly() {
        return this.boolIdOnly;
    }

    public String getMetadataPrefix() throws OAIException {
        if (this.repo.getProtocolMajorVersion() < 2) {
            this.priCheckIdOnly();
        }
        return this.strMetadataPrefix;
    }

    public Element getRecord() throws OAIException {
        this.priCheckIdOnly();
        return new Document((Element)this.xmlRecord.clone()).getRootElement();
    }

    public void refreshRecord() throws OAIException {
        if (this.strMetadataPrefix.length() == 0) {
            this.strMetadataPrefix = "oai_dc";
        }
        this.refreshRecord(this.strMetadataPrefix);
    }

    public void refreshRecord(String meta) throws OAIException {
        OAIRecord rec = this.repo.getRecord(this.strIdentifier, meta);
        if (rec == null) {
            throw new OAIException(1, "Record failed to refresh, illegal or nonexisting identifier?");
        }
        this.frndSetDatestamp(rec.getDatestamp());
        this.frndSetIdentifier(rec.getIdentifier());
        this.frndSetIdOnly(rec.isIdentifierOnly());
        this.frndSetMetadataPrefix(meta);
        this.frndSetStatus(rec.deleted() ? "deleted" : "");
        this.frndSetRecord(rec.getRecord());
        this.frndSetValid(rec.isRecordValid());
        this.boolIdOnly = false;
    }

    public int getSetSpecCount() throws OAIException {
        int ret = 0;
        try {
            Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
            XPath xpath = XPath.newInstance((String)"//oai:setSpec");
            xpath.addNamespace(oains);
            List list = xpath.selectNodes((Object)this.xmlRecord);
            ret = list.size();
        }
        catch (JDOMException te) {
            throw new OAIException(14, te.getMessage());
        }
        return ret;
    }

    public String getSetSpec() throws OAIException {
        return this.getSetSpec(0);
    }

    public String getSetSpec(int i) throws OAIException {
        String ret = "";
        if (this.repo.getProtocolMajorVersion() < 2) {
            throw new OAIException(15, "'setSpec' of header is not supported.");
        }
        if (this.getSetSpecCount() > 0 && i < this.getSetSpecCount()) {
            try {
                Namespace oains = Namespace.getNamespace((String)"oai", (String)"http://www.openarchives.org/OAI/2.0/");
                Vector<Namespace> nsVector = new Vector<Namespace>();
                nsVector.add(oains);
                Element n = OaiUtil.getXpathNode("//oai:setSpec[" + (i + 1) + "]/text()", nsVector, this.xmlRecord);
                if (n != null) {
                    ret = n.getText();
                }
            }
            catch (JDOMException te) {
                throw new OAIException(14, te.getMessage());
            }
        }
        return ret;
    }

    protected void frndSetIdentifier(String i) {
        this.strIdentifier = i;
    }

    protected void frndSetDatestamp(String d) {
        this.strDatestamp = d;
    }

    protected void frndSetRepository(OAIRepository r) {
        this.repo = r;
    }

    protected void frndSetMetadataPrefix(String m) {
        this.strMetadataPrefix = m;
    }

    protected void frndSetValid(boolean v) {
        this.boolValid = v;
    }

    protected void frndSetIdOnly(boolean b) {
        this.boolIdOnly = b;
    }

    protected void frndSetRecord(Element n) {
        this.xmlRecord = n;
    }

    protected void frndSetStatus(String s) {
        this.strStatus = s;
    }
}

