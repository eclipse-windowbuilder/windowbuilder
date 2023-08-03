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
package org.eclipse.wb.tests.designer.XML;

import org.eclipse.wb.tests.designer.XML.editor.XmlEditorTests;
import org.eclipse.wb.tests.designer.XML.gef.GefTests;
import org.eclipse.wb.tests.designer.XML.model.ModelTests;
import org.eclipse.wb.tests.designer.XML.palette.PaletteTests;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All XML tests.
 *
 * @author scheglov_ke
 */
@Ignore
@RunWith(Suite.class)
@SuiteClasses({
		ActivatorTest.class,
		ClassLoadingTest.class,
		ModelTests.class,
		XmlEditorTests.class,
		GefTests.class,
		PaletteTests.class
})
public class XmlTests {
}
