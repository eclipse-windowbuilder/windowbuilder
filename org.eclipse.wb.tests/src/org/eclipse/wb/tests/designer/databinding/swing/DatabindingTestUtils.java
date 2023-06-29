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
package org.eclipse.wb.tests.designer.databinding.swing;

import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * @author sablin_aa
 */
public class DatabindingTestUtils {
	/**
	 * Configures given {@link TestProject} for using Swing DB.
	 */
	public static void configure(TestProject testProject) throws Exception {
		testProject.addBundleJars("org.eclipse.wb.tests.support", "/resources/Swing/jsr295");
	}

	/**
	 * @return the source for Swing class in package "test".
	 */
	public static String getTestSource(String... lines) {
		return DesignerTestCase.getSource(new String[][]{
			new String[]{
					"package test;",
					"import java.awt.*;",
					"import java.awt.event.*;",
					"import javax.swing.*;",
					"import javax.swing.border.*;",
			"import org.jdesktop.beansbinding.*;"},
			lines});
	}

	/**
	 * @return the {@link JPanelInfo} for Swing source of class "Test" in package "test".
	 */
	public static JPanelInfo parseTestSource(AbstractJavaInfoTest javaInfoTest, String[] lines)
			throws Exception {
		return (JPanelInfo) javaInfoTest.parseSource("test", "Test.java", getTestSource(lines));
	}
}