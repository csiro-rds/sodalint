package au.csiro.casda.sodalint;

import uk.ac.starlink.ttools.taplint.IvoaSchemaResolver;
import uk.ac.starlink.ttools.taplint.Reporter;
import uk.ac.starlink.ttools.taplint.XsdStage;

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
 * Validate the SODA capabilities document against the XML schema.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateCapabilitiesXsd implements SodaValidationTask
{

    /** {@inheritDoc} */
    @Override
    public void run(Reporter reporter, SodaService sodaService, String testDataProductId)
    {
        XsdStage tcapXsdStage = XsdStage.createXsdStage(IvoaSchemaResolver.CAPABILITIES_URI, "capabilities",
                "/capabilities", true, "capabilities");

        tcapXsdStage.run(reporter, sodaService.getServiceUrl());
    }

}
