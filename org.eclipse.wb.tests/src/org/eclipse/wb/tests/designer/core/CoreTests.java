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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */

@RunWith(Suite.class)
@SuiteClasses({
    BundleResourceProviderTest.class,
    DesignerPluginTest.class,
    EnvironmentUtilsTest.class,
    VersionTest.class,
    UtilTests.class,
    AstEvaluationEngineTests.class,
    ModelTests.class,
    NlsTests.class,
    PaletteTests.class,
    CoreDbTests.class

    })
public class CoreTests {
}
