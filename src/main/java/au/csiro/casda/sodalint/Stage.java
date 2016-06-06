package au.csiro.casda.sodalint;

import java.util.HashMap;
import java.util.Map;

import uk.ac.starlink.ttools.taplint.AdhocCode;
import uk.ac.starlink.ttools.taplint.ReportType;
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
 * SODA Lint test stage. Each stage has a code which is how it is reported and requested by the caller.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public enum Stage
{
    CAP_XML("CPV", new ValidateCapabilitiesXsd()), CAPABILITIES("CAP", new ValidateCapabilities()), AVAIL_XML("AVV",
            new ValidateAvailabilityXsd()), EXAMPLES("EXM", null), SERVICE_DESC("SVD",
                    new ValidateServiceDescriptor()), ERR_VAL("ERR",
                            null), SYNC("SYN", new ValidateSync()), ASYNC("ASY", new ValidateAsync());

    private static Map<String, Stage> codeMap;

    private final String code;
    private final SodaValidationTask stageClass;

    private Stage(String code, SodaValidationTask stageClass)
    {
        this.code = code;
        this.stageClass = stageClass;
    }

    public String getCode()
    {
        return code;
    }

    /**
     * Run the validation for this stage.
     * 
     * @param reporter
     *            validation message destination
     * @param sodaService
     *            SODA service description
     * @param testDataProductId
     *            id of a valid data product which can be tested
     */
    public void run(final Reporter reporter, final SodaService sodaService, final String testDataProductId)
    {
        if (stageClass != null)
        {
            stageClass.run(reporter, sodaService, testDataProductId);
        }
        else
        {
            reporter.report(new AdhocCode(ReportType.FAILURE, "STAG"), "No validator implmented for stage " + code);
        }
    }

    /**
     * Check if the code is valid.
     * 
     * @param code
     *            The code to be checked.
     * @return True if the code represents a stage, false if not.
     */
    public static boolean isValidCode(String code)
    {
        if (codeMap == null)
        {
            buildCodeMap();
        }

        return codeMap.containsKey(code.toUpperCase());
    }

    /**
     * @param code
     *            The code of the stage to be retrieved.
     * @return The stage matching the code, or null if not a valid code.
     */
    public static Stage getStageForCode(String code)
    {
        if (codeMap == null)
        {
            buildCodeMap();
        }

        return codeMap.get(code.toUpperCase());
    }

    private static synchronized void buildCodeMap()
    {
        if (codeMap != null)
        {
            return;
        }

        Map<String, Stage> stageMap = new HashMap<>();
        for (Stage stage : Stage.values())
        {
            stageMap.put(stage.getCode(), stage);
        }
        codeMap = stageMap;
    }
}
