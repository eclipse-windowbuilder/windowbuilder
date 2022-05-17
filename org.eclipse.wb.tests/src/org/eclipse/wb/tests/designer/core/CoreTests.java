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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.tests.designer.core.databinding.CoreDbTests;
import org.eclipse.wb.tests.designer.core.eval.AstEvaluationEngineTests;
import org.eclipse.wb.tests.designer.core.model.ModelTests;
import org.eclipse.wb.tests.designer.core.nls.NlsTests;
import org.eclipse.wb.tests.designer.core.palette.PaletteTests;
import org.eclipse.wb.tests.designer.core.util.UtilTests;
import org.eclipse.wb.tests.designer.core.util.VersionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */

public class CoreTests extends TestCase {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core");
    suite.addTestSuite(BundleResourceProviderTest.class);
    suite.addTestSuite(DesignerPluginTest.class);
    suite.addTestSuite(EnvironmentUtilsTest.class);
    suite.addTestSuite(VersionTest.class);
    suite.addTest(UtilTests.suite());
    suite.addTest(AstEvaluationEngineTests.suite());
    suite.addTest(ModelTests.suite());
    suite.addTest(NlsTests.suite());
    suite.addTest(PaletteTests.suite());
    suite.addTest(CoreDbTests.suite());
    return suite;
  }
}
