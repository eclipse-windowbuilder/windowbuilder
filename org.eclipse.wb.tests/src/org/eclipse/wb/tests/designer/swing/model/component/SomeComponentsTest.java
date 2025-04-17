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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.awt.Canvas;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * Test for different components.
 *
 * @author scheglov_ke
 */
public class SomeComponentsTest extends SwingModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link JEditorPane} has two methods to set "URL" property. Test that we have both.
	 */
	@Test
	public void test_JEditorPane() throws Exception {
		ContainerInfo panel =
				(ContainerInfo) parseSource(
						"test",
						"Test.java",
						getSourceDQ(
								"package test;",
								"import javax.swing.*;",
								"class Test extends JPanel {",
								"  Test() {",
								"    add(new JEditorPane());",
								"  }",
								"}"));
		ComponentInfo editorPane = panel.getChildrenComponents().get(0);
		// we have both properties, so String variant uses qualified title
		assertNotNull(editorPane.getPropertyByTitle("page(java.lang.String)"));
		// but we don't have PropertyEditor for URL, so no such property
		assertNull(editorPane.getPropertyByTitle("page(java.net.URL)"));
	}

	/**
	 * When we put {@link Canvas} on {@link JScrollPane} and it has empty size, this causes exception.
	 * <p>
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=367254
	 */
	@Test
	public void test_Canvas_JScrollPane() throws Exception {
		parseJavaInfo(
				"import java.awt.Canvas;",
				"class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BorderLayout());",
				"    {",
				"      JScrollPane scrollPane = new JScrollPane();",
				"      add(scrollPane);",
				"      {",
				"        Canvas canvas = new Canvas();",
				"        scrollPane.setRowHeaderView(canvas);",
				"      }",
				"    }",
				"  }",
				"}");
		refresh();
	}
}
