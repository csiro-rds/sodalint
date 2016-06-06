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
 * Validate the sync endpoint.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateSync extends Validator implements SodaValidationTask
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
            // Check endpoint exists
            getSyncContent(reporter, syncUrl);
        }
        catch (XPathExpressionException e)
        {
            reporter.report(SodaCode.F_CODE, "Validate Sync coding error: ", e);

            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            reporter.report(SodaCode.E_SYUR, "Invalid sync interface access URL: ", e);

            e.printStackTrace();
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
}
