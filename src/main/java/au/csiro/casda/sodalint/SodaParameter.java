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
    /** The ID image selection parameter*/
    ID("meta.id;meta.dataset", "", "char", "*", ""), 
    /** The general POS filter parameter for spatial regions. */
    POS("phys.angArea;obs", "", "char", "*", "region"), 
    /** The BAND filter parameter for wavelength cutouts. */
    BAND("em.wl;stat.interval", "m", "double", "2", "interval"), 
    /** The TIME filter parameter for time cutouts. */
    TIME("time.interval;obs.exposure", "d", "double", "2", "interval"), 
    /** The POL filter parameter for polarisation cutouts. */
    POL("meta.code;phys.polarization", "", "char", "*", ""), 
    /** The CIRCLE filter parameter for spatial cutouts. */
    CIRCLE("phys.angArea;obs", "deg", "double", "3", "circle"), 
    /** The POLYGON filter parameter for spatial cutouts. */
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
