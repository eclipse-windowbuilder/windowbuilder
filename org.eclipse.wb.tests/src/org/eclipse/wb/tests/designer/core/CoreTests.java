/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author scheglov_ke
 */

@Suite
@SelectClasses({
	BundleResourceProviderTest.class,
	DesignerPluginTest.class,
	EnvironmentUtilsTest.class,
	UtilTests.class,
	AstEvaluationEngineTests.class,
	ModelTests.class,
	NlsTests.class,
	PaletteTests.class,
	CoreDbTests.class

})
public class CoreTests {
}
