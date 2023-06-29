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
package org.eclipse.wb.tests.designer.XML.model.generic;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Tests for <code>double-click.flipBooleanProperty</code> support.
 *
 * @author scheglov_ke
 */
public class FlipBooleanPropertyGefTest extends XwtGefTest {
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
	public void test_doFlip() throws Exception {
		prepareMyPanel(
				"  <parameters>",
				"    <parameter name='double-click.flipBooleanProperty'>myExpanded</parameter>",
				"  </parameters>");
		waitForAutoBuild();
		// open editor
		XmlObjectInfo panel = openMyPanel();
		// flip: false -> true
		canvas.doubleClick(panel);
		assertXML("<t:MyComponent myExpanded='true'/>");
		// flip: true -> false
		canvas.doubleClick(panel);
		assertXML("<t:MyComponent/>");
	}

	/**
	 * If no specified property, then ignore.
	 */
	public void test_noSuchProperty() throws Exception {
		prepareMyPanel(
				"  <parameters>",
				"    <parameter name='double-click.flipBooleanProperty'>noSuchProperty</parameter>",
				"  </parameters>");
		// open editor
		XmlObjectInfo panel = openMyPanel();
		// do double click, but property to flip does not exist, so ignore
		canvas.doubleClick(panel);
		assertXML("<t:MyComponent/>");
	}

	/**
	 * If specified property is not boolean, then ignore.
	 */
	public void test_notBooleanProperty() throws Exception {
		prepareMyPanel(
				"  <parameters>",
				"    <parameter name='double-click.flipBooleanProperty'>background</parameter>",
				"  </parameters>");
		// open editor
		XmlObjectInfo panel = openMyPanel();
		// do double click, but property to flip does not exist, so ignore
		canvas.doubleClick(panel);
		assertXML("<t:MyComponent/>");
	}

	public void test_noFlipParameter() throws Exception {
		prepareMyPanel(
				"  <parameters>",
				"    <parameter name='noFlipParameter'>nothing</parameter>",
				"  </parameters>");
		// open editor
		XmlObjectInfo panel = openMyPanel();
		// do double click, but property to flip does not exist, so ignore
		canvas.doubleClick(panel);
		assertXML("<t:MyComponent/>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String[] m_expandedLines = new String[]{
			"  private boolean m_expanded;",
			"  public boolean getMyExpanded() {",
			"    return m_expanded;",
			"  }",
			"  public void setMyExpanded(boolean expanded) {",
			"    m_expanded = expanded;",
	"  }"};

	private void prepareMyPanel(String... descriptionLines) throws Exception {
		prepareMyComponent(m_expandedLines, descriptionLines);
	}

	private XmlObjectInfo openMyPanel() throws Exception {
		return openEditor("<t:MyComponent/>");
	}
}
