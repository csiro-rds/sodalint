package au.csiro.casda.sodalint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.HttpResponseException;
import org.w3c.dom.Node;

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
 * Validate the async endpoint.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateAsync extends Validator implements SodaValidationTask
{

    /** {@inheritDoc} */

    @Override
    public void run(Reporter reporter, SodaService sodaService, String testDataProductId)
    {
        Node asyncNode = sodaService.getAsyncServiceNode();

        if (asyncNode == null)
        {
            reporter.report(SodaCode.I_ASNO, "No async SODA endpoint to test.");
            return;
        }

        try
        {
            URL asyncUrl = sodaService.getUrlFromCapabilityNode(asyncNode);
            reporter.report(SodaCode.I_VURL, "Validating URL: " + asyncUrl);
            // Check endpoint exists
            getAsyncContent(reporter, asyncUrl);
        }
        catch (XPathExpressionException e)
        {
            reporter.report(SodaCode.F_CODE, "Validate async coding error: ", e);

            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            reporter.report(SodaCode.E_SYUR, "Invalid async interface access URL: ", e);

            e.printStackTrace();
        }
    }

    private String getAsyncContent(final Reporter reporter, URL address)
    {
        try
        {
            String content = getXmlContentFromUrl(address.toString());
            if (content == null)
            {
                reporter.report(SodaCode.E_ASCO, "Async response contains no content");
            }
            return content;
        }
        catch (HttpResponseException e)
        {
            reporter.report(SodaCode.E_ASCO,
                    "Unexpected http response: " + e.getStatusCode() + " Reason: " + e.getMessage());
        }
        catch (UnsupportedEncodingException e)
        {
            reporter.report(SodaCode.E_ASCO, "Async response has an unexpected content type:" + e.getMessage());
        }
        catch (IOException e)
        {
            reporter.report(SodaCode.E_ASCO, "Unable to read async response: " + e.getMessage());
        }

        return null;
    }
}
