package au.csiro.casda.sodalint;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * A container for data about a SODA service.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class SodaService
{
    private URL serviceUrl;
    private Node syncServiceNode;
    private Node asyncServiceNode;

    public SodaService(URL serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    public URL getServiceUrl()
    {
        return serviceUrl;
    }

    public void setServiceUrl(URL serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    public Node getSyncServiceNode()
    {
        return syncServiceNode;
    }

    public void setSyncServiceNode(Node syncServiceNode)
    {
        this.syncServiceNode = syncServiceNode;
    }

    public Node getAsyncServiceNode()
    {
        return asyncServiceNode;
    }

    public void setAsyncServiceNode(Node asyncServiceNode)
    {
        this.asyncServiceNode = asyncServiceNode;
    }

    /**
     * Retrieve the interface access url for a specific capability. If node has multiple interface access URLs then only
     * the first will be returned.
     * 
     * @param capNode
     *            The node to be queried.
     * @return The interface access URL, or null if none listed.
     * @throws XPathExpressionException
     *             If there is an xpath coding problem
     * @throws MalformedURLException
     *             If the interface access url is not a valid URL
     */
    public URL getUrlFromCapabilityNode(Node capNode) throws XPathExpressionException, MalformedURLException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList interfaces = (NodeList) xpath.evaluate("interface/accessURL", capNode, XPathConstants.NODESET);
        if (interfaces != null && interfaces.getLength() > 0)
        {
            Node urlNode = interfaces.item(0);
            return new URL(urlNode.getTextContent());
        }
        return null;
    }

}
