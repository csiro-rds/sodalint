package au.csiro.casda.sodalint;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import uk.ac.starlink.ttools.taplint.ReportType;
import uk.ac.starlink.ttools.taplint.Reporter;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * <add description here>
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class ValidateServiceDescriptorTest
{

    private static final String CHARSET_UTF_8 = "UTF-8";
    private ValidateServiceDescriptor vsd;

    @Before
    public void setup()
    {
        vsd = new ValidateServiceDescriptor();
    }

    /**
     * Test method forau.csiro.casda.sodalint.ValidateServiceDescriptor#verifyServiceDescriptor
     * .
     * 
     * @throws IOException
     *             If the data file cannot be read.
     */
    @Test
    public void testVerifyV13ServiceDescriptor() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent = FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_3-good.xml"));
        vsd.verifyServiceDescriptor(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        assertEquals("No message should have been reported", "", result);
    }

    /**
     * Test a v1.2 VOTable service descriptor document.
     * 
     * @throws IOException
     *             If the data file cannot be read.
     */
    @Test
    public void testVerifyV12ServiceDescriptor() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent = FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_2-good.xml"));
        vsd.verifyServiceDescriptor(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        assertEquals("No message should have been reported", "", result);
    }

    /**
     * Test the validation of filter params in a v1.3 VOTable service descriptor document.
     * 
     * @throws IOException
     *             If the data file cannot be read.
     */
    @Test
    public void testVerifyV13BadParamServiceDescriptor() throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent =
                FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_3-badparams.xml"));
        vsd.verifyServiceDescriptor(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        String[] expectedMessages = new String[] { "E-SDMP-01 Required service descriptor ID PARAM is missing.",
                "E-SDIP-01 Service descriptor POS PARAM should have an attribute ucd with the value 'phys.angArea;obs'.",
                "E-SDIP-02 Service descriptor BAND PARAM should have an attribute unit with the value 'm'.",
                "E-SDIP-03 Service descriptor BAND PARAM should have an attribute xtype with the value 'interval'.",
                "E-SDIP-04 Service descriptor BAND PARAM should have an attribute arraysize with the value '2'.",
                "E-SDIP-05 Service descriptor TIME PARAM should have an attribute arraysize with the value '2'.",
                "W-SDSP-01 Standard service descriptor CIRCLE PARAM is missing." };

        assertEquals("Messages should have been reported", StringUtils.join(expectedMessages, System.lineSeparator()),
                result.trim());
    }

    /**
     * Test the votlint validation of a v1.2 VOTable service descriptor document.
     * 
     * @throws IOException
     *             If the data file cannot be read.
     * @throws SAXException
     */
    @Test
    public void testVotlintV12Service() throws IOException, SAXException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent = FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_2-good.xml"));
        vsd.verifyResponseWithVotLint(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        assertEquals("No message should have been reported", "", result);
    }

    /**
     * Test the votlint validation of a v1.3 VOTable service descriptor document.
     * 
     * @throws IOException
     *             If the data file cannot be read.
     * @throws SAXException
     */
    @Test
    public void testVotlintV13Service() throws IOException, SAXException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent = FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_3-good.xml"));
        vsd.verifyResponseWithVotLint(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        assertEquals("No message should have been reported", "", result);
    }

    /**
     * Test the votlint validation of a known bad v1.3 VOTable service descriptor document.
     * 
     * @throws IOException
     *             If the data file cannot be read.
     * @throws SAXException
     */
    @Test
    public void testVotlintBadV13Service() throws IOException, SAXException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, false, CHARSET_UTF_8);
        Reporter reporter = new Reporter(ps, ReportType.values(), 10, false, 1024);
        String xmlContent =
                FileUtils.readFileToString(new File("src/test/resources/service-descriptor-v1_3-badref.xml"));
        vsd.verifyResponseWithVotLint(reporter, xmlContent);

        String result = baos.toString(CHARSET_UTF_8);
        System.out.println(result);
        assertEquals("Message should have been reported",
                "E-VOFY-01 : ID bad referenced from PARAM (l.30, c.29) never found", result.trim());
    }
}
