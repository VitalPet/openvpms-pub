/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: DataLoadMojoTestCase.java 3335 2009-05-08 06:46:38Z tanderson $
 */
package org.openvpms.maven.data;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.openvpms.maven.archetype.ArchetypeLoadMojo;

import java.io.File;


/**
 * Tests the {@link DataLoadMojo} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2009-05-08 06:46:38 +0000 (Fri, 08 May 2009) $
 */
public class DataLoadMojoTestCase extends AbstractMojoTestCase {

    /**
     * Verifies that the mojo can be looked up.
     *
     * @throws Exception for any error
     */
    public void testMojoLookup() throws Exception {
        File pluginXml = new File(getBasedir(), "src/test/resources/plugin-config.xml");
        DataLoadMojo mojo = (DataLoadMojo) lookupMojo("load", pluginXml);
        assertNotNull(mojo);
    }

    /**
     * Verifies the mojo can be executed.
     *
     * @throws Exception for any error
     */
    public void testMojoExecution() throws Exception {
        File pluginXml = new File(getBasedir(), "src/test/resources/plugin-config.xml");
        DataLoadMojo mojo = (DataLoadMojo) lookupMojo("load", pluginXml);
        mojo.execute();
    }

}
