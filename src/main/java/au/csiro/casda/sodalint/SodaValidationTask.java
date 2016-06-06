package au.csiro.casda.sodalint;

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
 * A task which can be carried out to validate the SODA implementation. Tasks must be stateless allowing multiple 
 * validations runs at the same time. 
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public interface SodaValidationTask
{

    /**
     * Run a validation task.
     * 
     * @param reporter
     *            validation message destination
     * @param sodaService
     *            SODA service description
     * @param testDataProductId
     *            id of a valid data product which can be tested
     */
    public void run(final Reporter reporter, final SodaService sodaService, final String testDataProductId);
    
}
