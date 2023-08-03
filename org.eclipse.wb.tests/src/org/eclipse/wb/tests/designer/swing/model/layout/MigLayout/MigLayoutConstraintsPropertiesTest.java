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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.MigLayout.model.CellConstraintsSupport;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for "Constraints" property contributed by {@link CellConstraintsSupport}.
 *
 * @author scheglov_ke
 */
public class MigLayoutConstraintsPropertiesTest extends AbstractMigLayoutTest {
	private CellConstraintsSupport m_constraints;
	private ComplexProperty m_cellProperty;
	private Property[] m_subProperties;

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
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel implements IConstants {",
						"  public Test() {",
						"    setLayout(new MigLayout('', '[][][]', '[][][][]'));",
						"    add(new JButton(C_1), 'cell 1 2');",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		m_constraints = MigLayoutInfo.getConstraints(button);
		{
			m_cellProperty = (ComplexProperty) button.getPropertyByTitle("Constraints");
			assertNotNull(m_cellProperty);
			assertEquals("cell 1 2", getPropertyText(m_cellProperty));
			//
			m_subProperties = m_cellProperty.getProperties();
			assertEquals(6, m_subProperties.length);
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		{
			m_constraints = null;
			m_cellProperty = null;
			m_subProperties = null;
		}
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link CellConstraintsSupport#getPropertyByTitle(String)}.
	 */
	@Test
	public void test_getPropertyByTitle() throws Exception {
		assertNull(m_constraints.getPropertyByTitle("noSuchProperty"));
		assertSame(m_subProperties[0], m_constraints.getPropertyByTitle("grid x"));
		assertSame(m_subProperties[1], m_constraints.getPropertyByTitle("grid y"));
		assertSame(m_subProperties[4], m_constraints.getPropertyByTitle("h alignment"));
	}

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
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 2 2");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_y() throws Exception {
		Property property = m_subProperties[1];
		assertEquals("grid y", property.getTitle());
		assertTrue(property.isModified());
		assertEquals(2, property.getValue());
		// set value
		{
			String expectedSource = StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 1 1");
			property.setValue(1);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_w() throws Exception {
		Property property = m_subProperties[2];
		assertEquals("grid width", property.getTitle());
		assertFalse(property.isModified());
		assertEquals(1, property.getValue());
		// set value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 1 2 2 1");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2 2 1", "cell 1 2");
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
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 1 2 1 2");
			property.setValue(2);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2 1 2", "cell 1 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_align_h() throws Exception {
		Property property = m_subProperties[4];
		assertEquals("h alignment", property.getTitle());
		assertSame(MigColumnInfo.Alignment.DEFAULT, property.getValue());
		assertFalse(property.isModified());
		// set value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 1 2,alignx right");
			property.setValue(MigColumnInfo.Alignment.RIGHT);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2,alignx right", "cell 1 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}

	@Test
	public void test_property_align_v() throws Exception {
		Property property = m_subProperties[5];
		assertEquals("v alignment", property.getTitle());
		assertSame(MigRowInfo.Alignment.DEFAULT, property.getValue());
		assertFalse(property.isModified());
		// set value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2", "cell 1 2,aligny bottom");
			property.setValue(MigRowInfo.Alignment.BOTTOM);
			assertEditor(expectedSource, m_lastEditor);
		}
		// reset value
		{
			String expectedSource =
					StringUtils.replace(m_lastEditor.getSource(), "cell 1 2,aligny bottom", "cell 1 2");
			property.setValue(Property.UNKNOWN_VALUE);
			assertEditor(expectedSource, m_lastEditor);
		}
	}
}
