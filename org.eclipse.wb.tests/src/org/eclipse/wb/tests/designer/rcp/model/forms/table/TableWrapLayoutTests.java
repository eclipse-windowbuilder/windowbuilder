/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link TableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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