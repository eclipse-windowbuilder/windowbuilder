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
package org.eclipse.wb.tests.designer.swt.model;

import org.eclipse.wb.tests.designer.swt.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.swt.model.layouts.LayoutTests;
import org.eclipse.wb.tests.designer.swt.model.menu.MenuTests;
import org.eclipse.wb.tests.designer.swt.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.swt.model.widgets.WidgetTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * SWT model tests.
 *
 * @author sablin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
		WidgetTests.class,
		PropertiesTests.class,
		LayoutTests.class,
		JFaceTests.class,
		MenuTests.class,
		ClipboardTest.class
})
public class ModelTests {
}