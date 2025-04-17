/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.junit.Before;

/**
 * Test for {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractMigLayoutTest extends AbstractLayoutTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		do_configureNewProject();
	}

	static void do_configureNewProject() throws Exception {
		m_testProject.addPlugin("com.miglayout.core");
		m_testProject.addPlugin("com.miglayout.swing");
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// create IConstants with C_* constants
		setFileContentSrc(
				"test/IConstants.java",
				getSourceDQ(
						"package test;",
						"public interface IConstants {",
						"  String C_1 = '1';",
						"  String C_2 = '2';",
						"  String C_3 = '3';",
						"  String C_4 = '4';",
						"  String C_5 = '5';",
						"  String C_6 = '6';",
						"  String C_7 = '7';",
						"  String C_8 = '8';",
						"}"));
		waitForAutoBuild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	protected boolean m_includeMigImports = true;

	@Override
	public String getTestSource(String... lines) {
		if (m_includeMigImports) {
			try {
				if (m_javaProject.findType("net.miginfocom.swing.MigLayout") != null) {
					lines =
							CodeUtils.join(new String[]{
									"import net.miginfocom.layout.*;",
							"import net.miginfocom.swing.*;"}, lines);
				}
			} catch (Throwable e) {
				throw ReflectionUtils.propagate(e);
			}
		}
		return super.getTestSource(lines);
	}

	/**
	 * Asserts that given {@link ComponentInfo} has expected cell bounds on {@link MigLayoutInfo}.
	 */
	protected static void assertCellBounds(ComponentInfo component,
			int expectedX,
			int expectedY,
			int expectedWidth,
			int expectedHeight) {
		CellConstraintsSupport constraints = MigLayoutInfo.getConstraints(component);
		assertEquals(expectedX, constraints.getX());
		assertEquals(expectedY, constraints.getY());
		assertEquals(expectedWidth, constraints.getWidth());
		assertEquals(expectedHeight, constraints.getHeight());
	}
}
