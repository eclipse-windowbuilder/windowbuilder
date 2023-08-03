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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.tests.designer.core.nls.ui.NlsUiTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		LocaleInfoTest.class,
		BundleInfoTest.class,
		NlsSupportTest.class,
		EditableSupportTest.class,
		SourceEclipseOldTest.class,
		SourceEclipseModernTest.class,
		SourceDirectTest.class,
		SourceFieldTest.class,
		SourceAbstractSpecialTest.class,
		NlsUiTests.class
})
public class NlsTests {
}
