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

import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * Tests for {@link JTextField} support.
 *
 * @author scheglov_ke
 */
public class JTextFieldTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link JTextField} exposes model for {@link Document} using
	 * {@link JTextField#getDocument()}.
	 * <p>
	 * Disabled by Kosta 20090512.
	 */
	@Test
	public void test_exposedDocument() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JTextField textField = new JTextField();",
				"    add(textField);",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(textField)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JTextField} {local-unique: textField} {/new JTextField()/ /add(textField)/}");
	}
}
