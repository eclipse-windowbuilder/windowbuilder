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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.describer.JavaSourceUiDescriber;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.ide.IDE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link JavaSourceUiDescriber}.
 *
 * @author scheglov_ke
 */
public class ContentDescriberTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		DesignerPlugin.getPreferences().setValue(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI, true);
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		DesignerPlugin.getPreferences().setToDefault(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI);
		super.tearDown();
		do_projectDispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_notGUI() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ("package test;", "public class Test {", "  // filler", "}"));
		waitForContentType();
		assertFalse(isDesignerType(file));
	}

	@Test
	public void test_disableRecognition() throws Exception {
		DesignerPlugin.getPreferences().setValue(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI, false);
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"import javax.swing.*;",
								"public class Test extends JPanel {",
								"  // filler",
								"}"));
		waitForContentType();
		assertFalse(isDesignerType(file));
	}

	@Test
	public void test_useExcludePattern() throws Exception {
		TestBundle testBundle = new TestBundle();
		try {
			testBundle.addExtension(
					"org.eclipse.wb.core.designerContentPatterns",
					new String[]{"<excludePattern>use this string to exclude</excludePattern>"});
			testBundle.install();
			//
			IFile file =
					setFileContentSrc(
							"test/Test.java",
							getSourceDQ(
									"package test;",
									"import javax.swing.*;",
									"public class Test extends JPanel {",
									"  // use this string to exclude",
									"}"));
			waitForContentType();
			assertFalse(isDesignerType(file));
		} finally {
			testBundle.dispose();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AWT/Swing
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_AWT_Applet() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"import java.applet.Applet;",
								"public class Test extends Applet {",
								"  // filler",
								"}"));
		waitForContentType();
		assertTrue(isDesignerType(file));
	}

	@Test
	public void test_Swing() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"import javax.swing.*;",
								"public class Test extends JPanel {",
								"  // filler",
								"}"));
		waitForContentType();
		assertTrue(isDesignerType(file));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SWT/RCP
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_SWT() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"public class Test {",
								"  // org.eclipse.swt.widgets.Button",
								"}"));
		waitForContentType();
		assertTrue(isDesignerType(file));
	}

	@Test
	public void test_RCP_ActionBarAdvisor() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"public class Test {",
								"  // org.eclipse.ui.application.ActionBarAdvisor",
								"}"));
		waitForContentType();
		assertTrue(isDesignerType(file));
	}

	@Test
	public void test_RCP_IPerspectiveFactory() throws Exception {
		IFile file =
				setFileContentSrc(
						"test/Test.java",
						getSourceDQ(
								"package test;",
								"public class Test {",
								"  // org.eclipse.ui.IPerspectiveFactory",
								"}"));
		waitForContentType();
		assertTrue(isDesignerType(file));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isDesignerType(IFile file) {
		IContentType contentType = IDE.getContentType(file);
		return "org.eclipse.wb.core.java.javaSourceGUI".equals(contentType.getId());
	}

	private void waitForContentType() throws Exception {
		waitEventLoop(1);
		waitForAutoBuild();
	}
}
