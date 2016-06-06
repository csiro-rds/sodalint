package au.csiro.casda.sodalint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.starlink.ttools.taplint.FixedCode;
import uk.ac.starlink.ttools.taplint.Reporter;

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
 * Validate the capabilities end point.
 * 
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateCapabilities extends Validator implements SodaValidationTask
{

    /** {@inheritDoc} */
    @Override
    public void run(Reporter reporter, SodaService sodaService, String testDataProductId)
    {
        String xmlContent = getCapabilities(reporter, sodaService.getServiceUrl());
        if (StringUtils.isNotBlank(xmlContent))
        {
            validateCapabilities(reporter, xmlContent, sodaService);
        }
        reporter.summariseUnreportedMessages(reporter.getSectionCode());
    }

    private String getCapabilities(final Reporter reporter, URL serviceUrl)
    {
        String baseUrl = serviceUrl.toString();
        if (!baseUrl.endsWith("/"))
        {
            baseUrl += "/";
        }

        String address = baseUrl + "capabilities";
        try
        {
            String content = getXmlContentFromUrl(address);
            if (content == null)
            {
                reporter.report(SodaCode.E_CPRS, "Capabilities response contains no content");
            }
            return content;
        }
        catch (HttpResponseException e)
        {
            reporter.report(SodaCode.E_CPRS,
                    "Unexpected http response: " + e.getStatusCode() + " Reason: " + e.getMessage());
        }
        catch (UnsupportedEncodingException e)
        {
            reporter.report(SodaCode.E_CPRS, "Capabilities response has an unexpected content type:" + e.getMessage());
        }
        catch (IOException e)
        {
            reporter.report(SodaCode.E_CPRS, "Unable to read capabilities response: " + e.getMessage());
        }

        return null;
    }

    void validateCapabilities(Reporter reporter, String xmlContent, SodaService sodaService)
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

            checkForSyncAsync(reporter, document, sodaService);
        }
        catch (ParserConfigurationException | UnsupportedEncodingException | XPathExpressionException e)
        {
            reporter.report(SodaCode.E_CPRS, "Unexpected error processing capability", e);
        }
        catch (SAXException e)
        {
            reporter.report(FixedCode.E_CPSX, "Error parsing capabilities metadata", e);
        }
        catch (IOException e)
        {
            reporter.report(FixedCode.E_CPIO, "Error reading capabilities metadata", e);
        }

    }

    /**
     * One or more of the sync and async SODA endpoints should be listed in the capabilities document.
     * 
     * @param reporter
     *            validation message destination
     * @param document
     *            The capabilities XML document
     * @param sodaService
     * @throws XPathExpressionException
     */
    private void checkForSyncAsync(Reporter reporter, Document document, SodaService sodaService)
            throws XPathExpressionException
    {
        String sodaStdIdPrefix = "ivo://ivoa.net/std/SODA";
        String sodaStdIdSync = "ivo://ivoa.net/std/SODA#sync-1.0.";
        String sodaStdIdAsync = "ivo://ivoa.net/std/SODA#async-1.0.";
        String accessDataStdIdSync = "ivo://ivoa.net/std/AccessData#sync";
        String accessDataStdIdAsync = "ivo://ivoa.net/std/AccessData#async";

        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList capNodeList =
                (NodeList) xpath.evaluate("capability", document.getDocumentElement(), XPathConstants.NODESET);
        boolean hasSoda = false;
        Node syncCapNode = null;
        Node asyncCapNode = null;

        for (int i = 0; i < capNodeList.getLength(); i++)
        {
            Node capNode = capNodeList.item(i);
            Node stdIdAttr = capNode.getAttributes().getNamedItem("standardID");

            if (stdIdAttr != null && StringUtils.isNotBlank(stdIdAttr.getNodeValue()))
            {
                String stdId = stdIdAttr.getNodeValue();
                if (stdId.startsWith(sodaStdIdPrefix))
                {
                    hasSoda = true;
                }
                if (stdId.equals(sodaStdIdSync))
                {
                    syncCapNode = capNode;
                }
                else if (stdId.equals(sodaStdIdAsync))
                {
                    asyncCapNode = capNode;
                }
                else if (stdId.equals(accessDataStdIdSync))
                {
                    reporter.report(SodaCode.E_CPEP, "SODA endpoint uses outdated AccessData id: " + stdId);
                    syncCapNode = capNode;
                }
                else if (stdId.equals(accessDataStdIdAsync))
                {
                    reporter.report(SodaCode.E_CPEP, "SODA endpoint uses outdated AccessData id: " + stdId);
                    asyncCapNode = capNode;
                }
            }
        }

        if (syncCapNode == null && asyncCapNode == null)
        {
            if (hasSoda)
            {
                reporter.report(SodaCode.E_CPEP, "SODA endpoints found but do not have v1.0. sync or async qualifiers");
            }
            else
            {
                reporter.report(SodaCode.E_CPEP, "SODA requires at least one of the sync and async endpoints");
            }
        }

        if (syncCapNode != null)
        {
            NodeList interfaces = (NodeList) xpath.evaluate("interface", syncCapNode, XPathConstants.NODESET);
            if (interfaces == null || interfaces.getLength() == 0)
            {
                reporter.report(SodaCode.E_CPIF, "SODA sync endpoint does not contain an interface");
            }
            else
            {
                sodaService.setSyncServiceNode(syncCapNode);
            }
        }
        if (asyncCapNode != null)
        {
            NodeList interfaces = (NodeList) xpath.evaluate("interface", asyncCapNode, XPathConstants.NODESET);
            if (interfaces == null || interfaces.getLength() == 0)
            {
                reporter.report(SodaCode.E_CPIF, "SODA async endpoint does not contain an interface");
            }
            sodaService.setAsyncServiceNode(asyncCapNode);
        }
    }

}
