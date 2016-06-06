package au.csiro.casda.sodalint;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
 * Defines the standard parameters (ID and filters) for SODA.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public enum SodaParameter
{
    ID("meta.ref.url;meta.curation", "", "char", "*", ""), 
    POS("phys.angArea;obs", "", "char", "*", ""), 
    BAND("em.wl", "m", "double", "*", "interval"), 
    TIME("time.interval;obs.exposure", "d", "double", "*", "interval"), 
    POL("meta.code;phys.polarization", "", "char", "*", ""), 
    CIRCLE("phys.angArea;obs", "deg", "double", "3", "circle"), 
    POLYGON("phys.angArea;obs", "deg", "double", "*", "polygon");
    
    private final Map<String, String> requiredAttribs;

    private SodaParameter(String ucd, String unit, String datatype,
            String arraysize, String xtype)
    {
        requiredAttribs = new HashMap<>();
        if (StringUtils.isNotBlank(ucd))
        {
            requiredAttribs.put("ucd", ucd);
        }
        if (StringUtils.isNotBlank(unit))
        {
            requiredAttribs.put("unit", unit);
        }
        if (StringUtils.isNotBlank(datatype))
        {
            requiredAttribs.put("datatype", datatype);
        }
        if (StringUtils.isNotBlank(arraysize))
        {
            requiredAttribs.put("arraysize", arraysize);
        }
        if (StringUtils.isNotBlank(xtype))
        {
            requiredAttribs.put("xtype", xtype);
        }

    }

    public Map<String, String> getRequiredAttribs()
    {
        return requiredAttribs;
    }
}
