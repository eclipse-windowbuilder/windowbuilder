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
package org.eclipse.wb.tests.designer.swt.model;

import org.eclipse.wb.tests.designer.swt.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.swt.model.layouts.LayoutTests;
import org.eclipse.wb.tests.designer.swt.model.menu.MenuTests;
import org.eclipse.wb.tests.designer.swt.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.swt.model.widgets.WidgetTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * SWT model tests.
 *
 * @author sablin_aa
 */
@Suite
@SelectClasses({
		WidgetTests.class,
		PropertiesTests.class,
		LayoutTests.class,
		JFaceTests.class,
		MenuTests.class,
		ClipboardTest.class
})
public class ModelTests {
}