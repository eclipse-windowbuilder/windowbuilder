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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.eclipse.wb.internal.swing.jsr296.model.FrameViewInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Test for {@link FrameViewInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class FrameViewGefTest extends SwingGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		m_testProject.addBundleJars("org.eclipse.wb.tests.support", "/resources/Swing/jsr296");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	public void DISABLE_test_0() throws Exception {
		FrameViewInfo view =
				openEditor(
						"import org.jdesktop.application.*;",
						"public class Test extends FrameView {",
						"  public Test(Application application, boolean enabled) {",
						"    super(application);",
						"    {",
						"      JPanel component = new JPanel();",
						"      setComponent(component);",
						"      component.setEnabled(enabled);",
						"    }",
						"  }",
						"}");
		ComponentInfo component = getJavaInfoByName("component");
		//
		canvas.assertNotNullEditPart(view);
		canvas.assertNotNullEditPart(component);
	}
}
