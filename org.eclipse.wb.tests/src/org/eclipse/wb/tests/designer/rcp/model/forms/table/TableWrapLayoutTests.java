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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link TableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		TableWrapDataTest.class,
		TableWrapLayoutSelectionActionsTest.class,
		TableWrapLayoutTest.class,
		TabelWrapLayoutParametersTest.class,
		TableWrapLayoutExposedTest.class,
		TableWrapLayoutClipboardTest.class,
		TableWrapLayoutGefTest.class
})
public class TableWrapLayoutTests {
}