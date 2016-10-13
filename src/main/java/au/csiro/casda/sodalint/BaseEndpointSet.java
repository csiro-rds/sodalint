package au.csiro.casda.sodalint;

import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.starlink.vo.EndpointSet;

/**
 * Minimal implementation of EndpointSet for use in XSD validation of pages.
 *  
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class BaseEndpointSet implements EndpointSet
{

    private URL serviceUrl;

    /**
     * Create a new BaseEndpointSet for a SODA service being tested
     * @param serviceUrl Base url of the SODA service being tested.
     */
    public BaseEndpointSet(URL serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public URL getAsyncEndpoint()
    {
        return null;
    }

    @Override
    public URL getAvailabilityEndpoint()
    {
        return getUrl("availability");
    }

    @Override
    public URL getCapabilitiesEndpoint()
    {
        return getUrl("capabilities");
    }

    @Override
    public URL getExamplesEndpoint()
    {
        return null;
    }

    @Override
    public String getIdentity()
    {
        return null;
    }

    @Override
    public URL getSyncEndpoint()
    {
        return null;
    }

    @Override
    public URL getTablesEndpoint()
    {
        return null;
    }

    private URL getUrl(String endpointPath)
    {
        try
        {
            return new URL(serviceUrl, endpointPath);
        }
        catch (MalformedURLException e)
        {
            System.err.println("Failed to build endpoint from " + serviceUrl + " and " + endpointPath + " due to: "
                    + e.getMessage());
            return null;
        }
    }

}
