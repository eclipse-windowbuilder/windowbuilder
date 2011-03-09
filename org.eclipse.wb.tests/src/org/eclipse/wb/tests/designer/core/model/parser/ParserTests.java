/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.core.model.creation.CreationsTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class ParserTests extends DesignerSuiteTests {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Suite
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.parser");
    suite.addTest(createSingleSuite(SimpleParserTest.class));
    suite.addTest(createSingleSuite(SwingParserTest.class));
    suite.addTest(createSingleSuite(ParserBroadcastsTest.class));
    suite.addTest(createSingleSuite(BadNodesTest.class));
    suite.addTest(createSingleSuite(EditorStateTest.class));
    suite.addTest(createSingleSuite(ExecuteOnParseTest.class));
    suite.addTest(createSingleSuite(GenerationSettingsTest.class));
    suite.addTest(CreationsTests.suite());
    return suite;
  }
}
