package au.csiro.casda.sodalint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

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
 * Base class for validation of the web endpoints
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class Validator
{

    /**
     * Retrieve the content from an address using a GET request.
     *  
     * @param address The address to be queried.
     * @return The content, or null if no content could be read.
     * @throws HttpResponseException If a non 200 response code is returned.
     * @throws UnsupportedEncodingException If the content does not have an XML format. 
     * @throws IOException If the content could not be read.
     */
    protected String getXmlContentFromUrl(String address)
            throws HttpResponseException, UnsupportedEncodingException, IOException
    {
        Response response = Request.Get(address).execute();
        HttpResponse httpResponse = response.returnResponse();
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() != 200)
        {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null)
        {
            return null;
        }
        ContentType contentType = ContentType.getOrDefault(entity);
        if (!ContentType.APPLICATION_XML.getMimeType().equals(contentType.getMimeType()))
        {
            throw new UnsupportedEncodingException(contentType.toString());
        }
        String content = readTextContent(entity);
        if (StringUtils.isBlank(content))
        {
            return null;
        }
    
        return content;
    }

    /**
     * Read text content from a HttpEntity.
     * 
     * @param entity The http entity to be queried.
     * @return The text content
     * @throws IOException If the content cannot be read.
     */
    protected String readTextContent(HttpEntity entity) throws IOException
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent())))
        {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null)
            {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    protected void logDocumentContent(Document document)
    {
        OutputFormat format = new OutputFormat(document);
        StringWriter stringOut = new StringWriter();
        XMLSerializer serial = new XMLSerializer(stringOut, format);
        try
        {
            serial.serialize(document);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        // Display the XML
        System.out.println(stringOut.toString());
    }

}