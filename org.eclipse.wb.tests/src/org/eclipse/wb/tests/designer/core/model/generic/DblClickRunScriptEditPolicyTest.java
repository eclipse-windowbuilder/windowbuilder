/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.model.generic;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.junit.jupiter.api.Test;

/**
 * Tests for <code>double-click.runScript</code> support.
 *
 * @author scheglov_ke
 */
public class DblClickRunScriptEditPolicyTest extends SwingGefTest {
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
	@Test
	public void test_doFlip() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource("""
						// filler filler filler filler filler
						public class MyPanel extends JPanel {
						}"""));
		setFileContentSrc(
				"test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='double-click.runScript'>getPropertyByTitle('enabled').setValue(false)</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// open editor
		ContainerInfo panel = openContainer("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends MyPanel {
					public Test() {
					}
				}""");
		// double click
		canvas.doubleClick(panel);
		assertEditor("""
				// filler filler filler filler filler
				// filler filler filler filler filler
				public class Test extends MyPanel {
					public Test() {
						setEnabled(false);
					}
				}""");
	}
}
