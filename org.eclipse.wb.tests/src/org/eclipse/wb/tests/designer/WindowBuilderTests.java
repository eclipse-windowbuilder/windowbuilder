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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.tests.designer.XML.XmlTests;
import org.eclipse.wb.tests.designer.XWT.XwtTests;
import org.eclipse.wb.tests.designer.core.CoreTests;
import org.eclipse.wb.tests.designer.editor.EditorTests;
import org.eclipse.wb.tests.designer.rcp.RcpTests;
import org.eclipse.wb.tests.designer.swing.SwingTests;
import org.eclipse.wb.tests.designer.swt.SwtTests;
import org.eclipse.wb.tests.utils.CommonTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All WindowBuilder tests.
 *
 * @author scheglov_ke
 */

@RunWith(Suite.class)
@SuiteClasses({
    CommonTests.class,
    CoreTests.class,
    SwingTests.class,
    SwtTests.class,
    RcpTests.class,
    XmlTests.class,
    XwtTests.class,
    EditorTests.class,

})

public class WindowBuilderTests {
}
