/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.FormLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.CellConstraints.Alignment;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CellConstraintsSupport}.
 *
 * @author scheglov_ke
 */
public class CellConstraintsSupportTest extends AbstractFormLayoutTest {
	private ContainerInfo m_panel;
	private ComponentInfo m_button;
	private ComplexProperty m_cellProperty;
	private Property[] m_subProperties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Set up
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		//
		if (m_testProject != null) {
			m_panel =
					parseContainer(
							"public class Test extends JPanel {",
							"  public Test() {",
							"    setLayout(new FormLayout(new ColumnSpec[] {",
							"        FormSpecs.RELATED_GAP_COLSPEC,",
							"        FormSpecs.DEFAULT_COLSPEC,},",
							"      new RowSpec[] {",
							"        FormSpecs.RELATED_GAP_ROWSPEC,",
							"        FormSpecs.DEFAULT_ROWSPEC,",
							"        FormSpecs.DEFAULT_ROWSPEC,}));",
							"    {",
							"      JButton button = new JButton();",
							"      add(button, '1, 2');",
							"    }",
							"  }",
							"}");
			m_panel.refresh();
			//
			m_button = m_panel.getChildrenComponents().get(0);
			{
				m_cellProperty = (ComplexProperty) m_button.getPropertyByTitle("Constraints");
				assertNotNull(m_cellProperty);
				m_subProperties = m_cellProperty.getProperties();
				assertEquals(6, m_subProperties.length);
			}
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		if (m_panel != null) {
			m_panel.refresh_dispose();
			m_panel = null;
			//
			m_button = null;
			m_cellProperty = null;
			m_subProperties = null;
		}
		//
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CellConstraintsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CellConstraintsSupport_span() throws Exception {
		CellConstraintsSupport support = FormLayoutInfo.getConstraints(m_button);
		// check for caching CellConstraintsSupport
		assertSame(FormLayoutInfo.getConstraints(m_button), support);
		// set spanning
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, 2, 1");
			support.setSpan(true, new Rectangle(1, 2, 2, 1));
			support.write();
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setAlign*
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setAlignH() throws Exception {
		CellConstraintsSupport support = FormLayoutInfo.getConstraints(m_button);
		//
		String expectedSource =
				StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, left, default");
		support.setAlignH(CellConstraints.LEFT);
		support.write();
		assertEditor(expectedSource, m_lastEditor);
	}

	@Test
	public void test_setAlignV() throws Exception {
		CellConstraintsSupport support = FormLayoutInfo.getConstraints(m_button);
		//
		String expectedSource =
				StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, default, top");
		support.setAlignV(CellConstraints.TOP);
		support.write();
		assertEditor(expectedSource, m_lastEditor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_property_x() throws Exception {
		Property property = m_subProperties[0];
		assertEquals("grid x", property.getTitle());
		assertTrue(property.isModified());
		assertEquals(1, property.getValue());
		// value for "grid x" can not be reset
		{
			String expectedSource = m_lastEditor.getSource();
			property.setValue(Property.UNKNOWN_VALUE);
			assertEquals(1, property.getValue());
			assertEditor(expectedSource, m_lastEditor);
		}
		// set value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2", "2, 2");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
		// restore value
		property.setValue(1);
	}

	@Test
	public void test_property_y() throws Exception {
		Property property = m_subProperties[1];
		assertEquals("grid y", property.getTitle());
		assertTrue(property.isModified());
		assertEquals(2, property.getValue());
		// set value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 1");
			property.setValue(1);
			assertEditor(expectedSource, m_lastEditor);
		}
		// restore value
		property.setValue(2);
	}

	@Test
	public void test_property_w() throws Exception {
		Property property = m_subProperties[2];
		assertEquals("grid width", property.getTitle());
		assertFalse(property.isModified());
		assertEquals(1, property.getValue());
		// set value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, 2, 1");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2, 2, 1", "1, 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_h() throws Exception {
		Property property = m_subProperties[3];
		assertEquals("grid height", property.getTitle());
		assertFalse(property.isModified());
		assertEquals(1, property.getValue());
		// set value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, 1, 2");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "1, 2, 1, 2", "1, 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_align_h() throws Exception {
		Property property = m_subProperties[4];
		assertEquals("h alignment", property.getTitle());
		assertFalse(property.isModified());
		assertEquals(CellConstraints.DEFAULT, property.getValue());
		// set value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, right, default");
			property.setValue(CellConstraints.RIGHT);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "1, 2, right, default", "1, 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_align_v() throws Exception {
		Property property = m_subProperties[5];
		assertEquals("v alignment", property.getTitle());
		assertFalse(property.isModified());
		assertEquals(CellConstraints.DEFAULT, property.getValue());
		// set value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, default, bottom");
			property.setValue(CellConstraints.BOTTOM);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "1, 2, default, bottom", "1, 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getSmallAlignmentImage
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getSmallAlignmentImage() throws Exception {
		check_getSmallAlignmentImage(CellConstraints.DEFAULT, true, false);
		check_getSmallAlignmentImage(CellConstraints.LEFT, true, true);
		check_getSmallAlignmentImage(CellConstraints.CENTER, true, true);
		check_getSmallAlignmentImage(CellConstraints.RIGHT, true, true);
		check_getSmallAlignmentImage(CellConstraints.FILL, true, true);
		//
		check_getSmallAlignmentImage(CellConstraints.DEFAULT, false, false);
		check_getSmallAlignmentImage(CellConstraints.TOP, false, true);
		check_getSmallAlignmentImage(CellConstraints.CENTER, false, true);
		check_getSmallAlignmentImage(CellConstraints.BOTTOM, false, true);
		check_getSmallAlignmentImage(CellConstraints.FILL, false, true);
	}

	private void check_getSmallAlignmentImage(Alignment alignment,
			boolean horizontal,
			boolean notNullExpected) {
		CellConstraintsSupport constraints = FormLayoutInfo.getConstraints(m_button);
		// configure constraints
		if (horizontal) {
			constraints.setAlignH(alignment);
		} else {
			constraints.setAlignV(alignment);
		}
		// check image
		ImageDescriptor alignmentImage = constraints.getSmallAlignmentImageDescriptor(horizontal);
		if (notNullExpected) {
			assertNotNull(alignmentImage);
		} else {
			assertNull(alignmentImage);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contextMenu() throws Exception {
		// prepare context menu
		IMenuManager manager;
		{
			manager = getDesignerMenuManager();
			m_button.getBroadcastObject().addContextMenu(null, m_button, manager);
		}
		// horizontal
		{
			IMenuManager manager2 = findChildMenuManager(manager, "Horizontal alignment");
			assertNotNull(manager2);
			assertNotNull(findChildAction(manager2, "&Default"));
			assertNotNull(findChildAction(manager2, "&Left"));
			assertNotNull(findChildAction(manager2, "&Center"));
			assertNotNull(findChildAction(manager2, "&Right"));
			assertNotNull(findChildAction(manager2, "&Fill"));
			// try to use "Left" action
			{
				String expectedSource =
						StringUtils.replace(m_lastEditor.getSource(), "1, 2", "1, 2, left, default");
				IAction action = findChildAction(manager2, "&Left");
				action.setChecked(true);
				action.run();
				assertEditor(expectedSource, m_lastEditor);
			}
		}
		// vertical
		{
			IMenuManager manager2 = findChildMenuManager(manager, "Vertical alignment");
			assertNotNull(manager2);
			assertNotNull(findChildAction(manager2, "&Default"));
			assertNotNull(findChildAction(manager2, "&Top"));
			assertNotNull(findChildAction(manager2, "&Center"));
			assertNotNull(findChildAction(manager2, "&Bottom"));
			assertNotNull(findChildAction(manager2, "&Fill"));
			// try to use "Top" action
			{
				String expectedSource =
						StringUtils.replace(m_lastEditor.getSource(), "1, 2, left, default", "1, 2, left, top");
				IAction action = findChildAction(manager2, "&Top");
				action.setChecked(true);
				action.run();
				assertEditor(expectedSource, m_lastEditor);
			}
		}
	}
}
