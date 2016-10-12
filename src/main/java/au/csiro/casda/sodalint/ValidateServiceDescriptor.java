package au.csiro.casda.sodalint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.starlink.ttools.taplint.Reporter;
import uk.ac.starlink.ttools.taplint.VotLintTapRunner;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2010 - 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Validate the service descriptor returned by the sync endpoint. Also validates the standard parameters.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateServiceDescriptor extends Validator implements SodaValidationTask
{

    /** {@inheritDoc} */

    @Override
    public void run(Reporter reporter, SodaService sodaService, String testDataProductId)
    {
        Node syncNode = sodaService.getSyncServiceNode();

        if (syncNode == null)
        {
            reporter.report(SodaCode.I_SYNO, "No sync SODA endpoint to test.");
            return;
        }

        try
        {
            URL syncUrl = sodaService.getUrlFromCapabilityNode(syncNode);
            reporter.report(SodaCode.I_VURL, "Validating URL: " + syncUrl);
            
            // Check endpoint exists
            String content = getSyncContent(reporter, syncUrl);
            if (StringUtils.isBlank(content))
            {
                reporter.report(SodaCode.I_SYNO, "Sync SODA endpoint does not return content.");
                return;
            }

            verifyResponseWithVotLint(reporter, content);
            verifyServiceDescriptor(reporter, content);
        }
        catch (XPathExpressionException | UnsupportedEncodingException e)
        {
            reporter.report(SodaCode.F_CODE, "Validate Service Descriptor coding error: ", e);
        }
        catch (MalformedURLException e)
        {
            reporter.report(SodaCode.E_SYUR, "Invalid sync interface access URL: ", e);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            reporter.report(SodaCode.E_SYUR, "Unable to parse service descriptor: ", e);
        }
    }

    private String getSyncContent(final Reporter reporter, URL address)
    {
        try
        {
            String content = getXmlContentFromUrl(address.toString());
            if (content == null)
            {
                reporter.report(SodaCode.E_SYCO, "Sync response contains no content");
            }
            return content;
        }
        catch (HttpResponseException e)
        {
            reporter.report(SodaCode.E_SYCO,
                    "Unexpected http response: " + e.getStatusCode() + " Reason: " + e.getMessage());
        }
        catch (UnsupportedEncodingException e)
        {
            reporter.report(SodaCode.E_SYCO, "Sync response has an unexpected content type:" + e.getMessage());
        }
        catch (IOException e)
        {
            reporter.report(SodaCode.E_SYCO, "Unable to read sync response: " + e.getMessage());
        }

        return null;
    }

    /**
     * Run targeted verification of the service descriptor XML text, including its presence and the support for standard
     * SODA parameters.
     * 
     * @param reporter
     *            The validation message destination
     * @param xmlContent
     *            The xml text of the service description.
     */
    void verifyServiceDescriptor(Reporter reporter, String xmlContent)
    {
        try
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(true);
            builderFactory.setFeature("http://xml.org/sax/features/validation", false);
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            byte[] bytes = xmlContent.getBytes("UTF-8");
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            Document document = builder.parse(is);

            // Check for resource with appropriate standard ID
            Node sodaSvcNode = getSodaServiceResource(reporter, document);
            if (sodaSvcNode == null)
            {
                reporter.report(SodaCode.E_SDNO, "No service descriptor resource found in default sync repsonse.");
                return;
            }

            // Check that an access URL is listed
            checkSodaAccessUrl(reporter, sodaSvcNode);

            // Check input params group
            checkInputParams(reporter, sodaSvcNode);

        }
        catch (ParserConfigurationException | UnsupportedEncodingException | XPathExpressionException e)
        {
            reporter.report(SodaCode.E_SDIN, "Unexpected error processing service description", e);
        }
        catch (SAXException e)
        {
            reporter.report(SodaCode.E_SDIN, "Error parsing service description", e);
        }
        catch (IOException e)
        {
            reporter.report(SodaCode.E_SDIN, "Error reading service description", e);
        }

    }

    private Node getSodaServiceResource(Reporter reporter, Document document) throws XPathExpressionException
    {
        // logDocumentContent(document);

        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "*[local-name()= 'RESOURCE' and "
                + "@utype='adhoc:service' and ./*/@value='ivo://ivoa.net/std/SODA#sync-1.0']";
        NodeList svcResList =
                (NodeList) xpath.evaluate(expression, document.getDocumentElement(), XPathConstants.NODESET);

        if (svcResList != null && svcResList.getLength() > 0)
        {
            return svcResList.item(0);
        }
        return null;
    }

    private void checkSodaAccessUrl(Reporter reporter, Node sodaSvcNode) throws XPathExpressionException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList accessUrlList =
                (NodeList) xpath.evaluate("*[local-name()='PARAM']", sodaSvcNode, XPathConstants.NODESET);

        if (accessUrlList == null || accessUrlList.getLength() == 0)
        {
            reporter.report(SodaCode.E_SDMP, "Service descriptor is missing accessURL PARAM.");
            return;
        }
        Node node = accessUrlList.item(0);
        if (node.getAttributes().getNamedItem("value") == null)
        {
            reporter.report(SodaCode.E_SDMP, "Service descriptor accessURL PARAM does not have a value.");
            return;
        }
    }

    private void checkInputParams(Reporter reporter, Node sodaSvcNode) throws XPathExpressionException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String[] requiredAttrs = new String[] { "name", "ucd", "datatype" };
        NodeList paramList =
                (NodeList) xpath.evaluate("*[local-name()='GROUP' and @name='inputParams']/*[local-name()='PARAM']",
                        sodaSvcNode, XPathConstants.NODESET);

        // <PARAM name="ID" ucd="meta.ref.url;meta.curation" datatype="char" arraysize="*" />
        Map<String, Node> paramMap = new HashMap<>();
        for (int i = 0; i < paramList.getLength(); i++)
        {
            Node paramNode = paramList.item(i);
            NamedNodeMap attributes = paramNode.getAttributes();
            String paramName = "Unnamed";
            boolean valid = true;
            for (String attrName : requiredAttrs)
            {
                Node attrNode = attributes.getNamedItem(attrName);
                if (attrNode == null)
                {
                    reporter.report(SodaCode.E_SDIP, "Service descriptor " + paramName
                            + " PARAM is missing the required attribute " + attrName + ".");
                    valid = false;
                }
                else if ("name".equals(attrName))
                {
                    paramName = attrNode.getNodeValue();
                }
            }
            if (valid)
            {
                paramMap.put(paramName, paramNode);
            }

        }

        // Check for ID support - error if not present
        if (!paramMap.containsKey("ID"))
        {
            reporter.report(SodaCode.E_SDMP, "Required service descriptor ID PARAM is missing.");
        }

        for (SodaParameter standardParam : SodaParameter.values())
        {
            if (paramMap.containsKey(standardParam.name()))
            {
                checkParamAttributes(reporter, paramMap.get(standardParam.name()), standardParam);
            }
            else if (!"ID".equals(standardParam.name()))
            {
                reporter.report(SodaCode.W_SDSP,
                        "Standard service descriptor " + standardParam.name() + " PARAM is missing.");
            }
        }
    }

    private void checkParamAttributes(Reporter reporter, Node paramNode, SodaParameter sodaParam)
    {
        NamedNodeMap attributes = paramNode.getAttributes();
        String paramName = attributes.getNamedItem("name").getNodeValue();
        for (Entry<String, String> reqAttr : sodaParam.getRequiredAttribs().entrySet())
        {
            Node attrNode = attributes.getNamedItem(reqAttr.getKey());
            if (attrNode == null)
            {
                reporter.report(SodaCode.E_SDIP, "Service descriptor " + paramName
                        + " PARAM is missing the required attribute " + reqAttr.getKey() + ".");
            }
            else if (!reqAttr.getValue().equals(attrNode.getNodeValue()))
            {
                reporter.report(SodaCode.E_SDIP, "Service descriptor " + paramName + " PARAM should have an attribute "
                        + reqAttr.getKey() + " with the value '" + reqAttr.getValue() + "'.");
            }
        }

    }

    /**
     * Use VOTLint to check the standard compliance of the service descriptor.
     * 
     * @param reporter
     *            The validation message destination
     * @param xmlContent
     *            The xml text of the service description.
     * @throws IOException
     *             If the content cannot be read.
     * @throws SAXException
     *             If the content cannot be parsed.
     */
    void verifyResponseWithVotLint(Reporter reporter, String xmlContent) throws IOException, SAXException
    {
        byte[] bytes = xmlContent.getBytes("UTF-8");
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        VotLintTapRunner runner = VotLintTapRunner.createGetSyncRunner(true);
        runner.readResultDocument(reporter, is);
    }
}
