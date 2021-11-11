package au.csiro.casda.sodalint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import uk.ac.starlink.task.Executable;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.taplint.ReportType;
import uk.ac.starlink.ttools.taplint.TextOutputReporter;

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
 * Main gateway for SODA validation - based on TAPLint.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class SodaLinter
{

    private static final String SODA_VERSION = "PR-SODA-1.0-20160920";
    
    /**
     * Creates and returns an executable for TAP validation.
     *
     * @param reporter
     *            validation message destination
     * @param serviceUrl
     *            SODA service URL
     * @param stageCodeSet
     *            unordered collection of code strings indicating which stages should be run
     * @param testDataProductId
     *            id of a valid data product which can be tested
     * @return SODA validator executable
     * @throws TaskException if the stage code is not recognised.
     */
    public Executable createExecutable(final TextOutputReporter reporter, final URL serviceUrl, Set<String> stageCodeSet,
            final String testDataProductId) throws TaskException
    {

         // Prepare a checked and ordered sequence of codes determining which stages will be executed. Note the order is
         //that defined by the list of known codes, not that defined by the input set.
        List<String> unknownCodes = new ArrayList<>();
        final Set<Stage> stages = new TreeSet<>();
        for (String code : stageCodeSet)
        {
            if (Stage.isValidCode(code))
            {
                stages.add(Stage.getStageForCode(code));
            }
        }
        if (!unknownCodes.isEmpty())
        {
            throw new TaskException("Unknown stage codes " + unknownCodes);
        }

        /*
         * Create and return an executable which will run the requested stages.
         */
        return new Executable()
        {
            public void execute()
            {
                SodaService sodaService = new SodaService(serviceUrl);
                List<String> announcements = getAnnouncements();
                announcements.add("Running stages: " + stages);
                reporter.start(announcements.toArray(new String[0]));
                for (Stage stage : stages)
                {
                    reporter.startSection(stage.getCode(), stage.toString());
                    stage.run(reporter, sodaService, testDataProductId);
                    reporter.summariseUnreportedMessages(stage.getCode());
                    reporter.endSection();
                }
                reporter.end();
            }
        };
    }

    /**
     * Returns a list of startup announcements with which the taplint application introduces itself.
     *
     * @return announcement lines
     */
    private static List<String> getAnnouncements()
    {

        /* Version report. */
        String version = getSodaLintVersion();
        String versionLine = new StringBuilder().append("This is sodalint ").append(version)
                .append(" validating against ").append(SODA_VERSION).toString();

        /* Count by report type of known FixedCode instances. */
        Map<ReportType, int[]> codeMap = new LinkedHashMap<ReportType, int[]>();
        for (ReportType type : Arrays.asList(ReportType.values()))
        {
            codeMap.put(type, new int[1]);
        }
        for (SodaCode code : Arrays.asList(SodaCode.values()))
        {
            codeMap.get(code.getType())[0]++;
        }
        StringBuffer cbuf = new StringBuffer().append("Static report types: ");
        for (Map.Entry<ReportType, int[]> entry : codeMap.entrySet())
        {
            cbuf.append(entry.getKey()).append("(").append(entry.getValue()[0]).append(")").append(", ");
        }
        cbuf.setLength(cbuf.length() - 2);
        String codesLine = cbuf.toString();

        /* Return lines. */
        List<String> announcements = new ArrayList<>();
        announcements.add(versionLine);
        announcements.add(codesLine);
        return announcements;
    }

    private static String getSodaLintVersion()
    {
        Properties props = new Properties();
		try (InputStream resourceAsStream = SodaLinter.class.getResourceAsStream("/version.properties"))
		{
            if (resourceAsStream != null)
            {
                props.load(resourceAsStream);
            }
        }
        catch (IOException e)
        {
            System.out.println("Unable to load version.properties: " + e.getMessage());
        }
        String version = props.getProperty("build.number");
        return version == null ? "" : version;
    }

    /**
     * Command line handler for SodaLinter
     * @param args The command line arguments
     * @throws Exception If an otherwise uncaught error occurs.
     */
    public static void main(String[] args) throws Exception
    {
        SodaLinter linter = new SodaLinter();
        String[] stages = null;
        final int defaultMaxRepeat = 9;
        final int defaultMaxLineLen = 1024;
        int maxRepeat = defaultMaxRepeat;
        int maxLineLen = defaultMaxLineLen;
        String sodaUrl = null;
        boolean error = false;

        for (String arg : args)
        {
            if (arg.startsWith("stages="))
            {
                stages = arg.substring("stages=".length()).split(" ");
            }
            else if (arg.startsWith("maxrepeat="))
            {
                String value = arg.substring("maxrepeat=".length());
                if (StringUtils.isNumeric(value))
                {
                    maxRepeat = Integer.parseInt(value);
                }
                else
                {
                    error = true;
                }
            }
            else if (arg.startsWith("truncate="))
            {
                String value = arg.substring("truncate=".length());
                if (StringUtils.isNumeric(value))
                {
                    maxLineLen = Integer.parseInt(value);
                }
                else
                {
                    error = true;
                }
            }
            else
            {
                sodaUrl = arg.startsWith("sodaurl=") ? arg.substring("sodaurl=".length()) : arg;
            }
        }
        if (sodaUrl == null || error)
        {
            System.out.println("Usage: java -jar sodalint-full.jar [stages=\"CPV|CAP|AVV|EXM|SVD|ERR|SYN|ASY[ ...]\"] "
                    + "[maxrepeat=<int-value>] [truncate=<int-value>] [sodaurl=]<url-value>");
            System.exit(1);
        }
        TextOutputReporter reporter = new TextOutputReporter(System.out, ReportType.values(), maxRepeat, false, maxLineLen);
        // URL serviceUrl = new URL("https://casda-dev-app.pawsey.org.au/casda_data_access/data/");
        URL serviceUrl = new URL(sodaUrl);
        String[] defaultStages =
                new String[] { Stage.CAP_XML.getCode(), Stage.AVAIL_XML.getCode(), Stage.CAPABILITIES.getCode(),
                        Stage.SYNC.getCode(), Stage.ASYNC.getCode(), Stage.SERVICE_DESC.getCode() };
        Set<String> codes = new HashSet<>(Arrays.asList(stages != null ? stages : defaultStages));
        Executable executable = linter.createExecutable(reporter, serviceUrl, codes, null);
        executable.execute();

    }
}
