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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.junit.Test;

/**
 * Test for {@link PageLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class PageLayoutGefTest extends RcpGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_normal() throws Exception {
		openJavaInfo(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    String editorArea = layout.getEditorArea();",
				"    addFastViews(layout);",
				"    addViewShortcuts(layout);",
				"    addPerspectiveShortcuts(layout);",
				"    layout.addView('org.eclipse.jdt.ui.PackageExplorer', IPageLayout.LEFT, 0.3f, editorArea);",
				"    layout.addView('org.eclipse.jdt.ui.TypeHierarchy', IPageLayout.BOTTOM, 0.7f, editorArea);",
				"  }",
				"  private void addFastViews(IPageLayout layout) {",
				"  }",
				"  private void addViewShortcuts(IPageLayout layout) {",
				"  }",
				"  private void addPerspectiveShortcuts(IPageLayout layout) {",
				"  }",
				"}");
	}

	/**
	 * If unknown ID is used as reference, we may be will render bad, but should not fail.
	 */
	@Test
	public void test_referenceUnknownView() throws Exception {
		openJavaInfo(
				"public class Test implements IPerspectiveFactory {",
				"  public Test() {",
				"  }",
				"  public void createInitialLayout(IPageLayout layout) {",
				"    layout.addView('my.View', IPageLayout.LEFT, 0.3f, 'unknownID');",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String[] getTestSource_decorate(String... lines) {
		lines = CodeUtils.join(new String[]{"package test;", "import org.eclipse.ui.*;"}, lines);
		return lines;
	}
}
