package au.csiro.casda.sodalint;

import uk.ac.starlink.ttools.taplint.ReportCode;
import uk.ac.starlink.ttools.taplint.ReportType;

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
 * The set of response codes used in SODA specific validation.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public enum SodaCode implements ReportCode
{
    /** No sync endpoint to test */
    I_SYNO(), 
    /** No async endpoint to test */
    I_ASNO,

    /** Unsupported standard filter parameter. */
    W_SDSP,
    
    /** No SODA endpoint found */
    E_CPEP(),
    /** No interface in SODA endpoint */
    E_CPIF,
    /** General capabilities response error */
    E_CPRS, 
    /** Invalid sync URL */
    E_SYUR,
    /** Invalid sync content */
    E_SYCO,
    /** Invalid async URL */
    E_ASUR,
    /** Invalid async content */
    E_ASCO, 
    /** No service descriptor resource */
    E_SDNO, 
    /** Invalid  service descriptor */
    E_SDIN,
    /** Missing param in service descriptor */
    E_SDMP,
    /** Invalid param */
    E_SDIP,
    
    /** A fatal error indicating a problem in the validator code. */
    F_CODE;

    public ReportType getType()
    {
        return ReportType.forChar(name().charAt(0));
    }

    public String getLabel()
    {
        return name().substring(2);
    }

}
