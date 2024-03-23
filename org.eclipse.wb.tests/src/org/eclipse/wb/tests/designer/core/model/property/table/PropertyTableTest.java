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
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.draw2d.Cursors;
import org.eclipse.swt.graphics.Point;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for basic {@link PropertyTable} features.
 *
 * @author scheglov_ke
 */
public class PropertyTableTest extends AbstractPropertyTableTest {
	private TestProperty m_locationProperty;
	private TestProperty m_boundsProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Test life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// set up properties
		{
			PropertyEditor stringEditor = StringPropertyEditor.INSTANCE;
			PropertyEditor intEditor = IntegerPropertyEditor.INSTANCE;
			//
			{
				ComplexEditor locationEditor =
						new ComplexEditor(new Property[]{
								new TestProperty("x", false, 100, intEditor),
								new TestProperty("y", false, 150, intEditor)});
				m_locationProperty = new TestProperty("location", true, "(100, 150)", locationEditor);
				ComplexEditor boundsEditor =
						new ComplexEditor(new Property[]{
								m_locationProperty,
								new TestProperty("width", false, 50, intEditor),
								new TestProperty("height", false, 10, intEditor),});
				m_boundsProperty = new TestProperty("bounds", true, "(100, 150, 50, 10)", boundsEditor);
			}
			//
			m_propertyTable.setInput(new Property[]{
					new TestProperty("text 1", true, "New button", stringEditor),
					new TestProperty("text 2", true, "New button", stringEditor),
					new TestProperty("text 3", true, "New button", stringEditor),
					new TestProperty("variable", false, "button", stringEditor),
					new TestProperty("tooltip", true, "Very long description of component", stringEditor),
					m_boundsProperty,});
			waitEventLoop(0);
		}
	}

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
	 * Test for clearing properties.
	 */
	@Test
	public void test_setInput_null() throws Exception {
		m_propertyTable.setInput(null);
		waitEventLoop(0);
	}

	/**
	 * Tests cursor.
	 */
	@Test
	public void test_cursor() throws Exception {
		m_sender.moveTo(10, 10);
		assertNull(m_propertyTable.getControl().getCursor());
		//
		int splitter = m_propertyTable.forTests_getSplitter();
		for (int x = splitter - 1; x <= splitter + 1; x++) {
			m_sender.moveTo(x, 10);
			assertSame(Cursors.SIZEWE, m_propertyTable.getControl().getCursor());
		}
		//
		m_sender.moveTo(splitter + 2, 10);
		assertNull(m_propertyTable.getControl().getCursor());
	}

	/**
	 * Tests splitter resize.
	 */
	@Test
	public void test_splitter() throws Exception {
		int width = m_propertyTable.getControl().getClientArea().width;
		// check initial position
		int splitter = (int) (width * 0.4);
		assertEquals(splitter, m_propertyTable.forTests_getSplitter());
		// do resize, enough space for title
		splitter = check_dragSplitter(splitter, 100, 100);
		// do resize, too little space for title
		splitter = check_dragSplitter(splitter, 10, 75);
		// do resize, too little space for value
		splitter = check_dragSplitter(splitter, width - 10, width - 75);
	}

	/**
	 * Drags splitter from <code>from</code> to <code>to</code> and expects value of splitter
	 * <code>expected</code>.
	 */
	private int check_dragSplitter(int from, int to, int expected) throws InterruptedException {
		m_sender.startDrag(from, 10, 1);
		m_sender.dragTo(to, 10);
		m_sender.endDrag();
		waitEventLoop(0);
		assertEquals(expected, m_propertyTable.forTests_getSplitter());
		return m_propertyTable.forTests_getSplitter();
	}

	/**
	 * Test for expand/collapse complex property
	 */
	@Test
	public void test_expandCollapse() throws Exception {
		// for not existing property location is "null"
		assertNull(m_propertyTable.forTests_getStateLocation(null));
		// prepare location
		Point boundsL = m_propertyTable.forTests_getStateLocation(m_boundsProperty);
		assertNotNull(boundsL);
		// expand "bounds"
		assertEquals(6, m_propertyTable.forTests_getPropertiesCount());
		m_sender.click(boundsL, 1);
		waitEventLoop(0);
		assertEquals(9, m_propertyTable.forTests_getPropertiesCount());
		// expand "location"
		Point locationL = m_propertyTable.forTests_getStateLocation(m_locationProperty);
		assertNotNull(locationL);
		m_sender.click(locationL, 1);
		waitEventLoop(0);
		assertEquals(11, m_propertyTable.forTests_getPropertiesCount());
		// collapse "bounds"
		m_sender.click(boundsL, 1);
		waitEventLoop(0);
		assertEquals(6, m_propertyTable.forTests_getPropertiesCount());
		// expand "bounds"
		m_sender.click(boundsL, 1);
		waitEventLoop(0);
		assertEquals(11, m_propertyTable.forTests_getPropertiesCount());
	}

	/**
	 * Test for clipped "<No Properties>" message.
	 */
	@Test
	public void test_noPropertiesClip() throws Exception {
		m_propertyTable.setInput(null);
		m_shell.setSize(100, 100);
		waitEventLoop(0);
	}

	/**
	 * Set of sub-properties is changed to same complex property.
	 */
	@Test
	public void test_setInput_complexPropertySubPropertiesChanged() throws Exception {
		TestProperty propertyA = new TestProperty("a", true, 1, IntegerPropertyEditor.INSTANCE);
		TestProperty propertyB = new TestProperty("b", true, 2, IntegerPropertyEditor.INSTANCE);
		//
		ComplexProperty complexProperty = new ComplexProperty("complex", "(Dynamic complex property)");
		complexProperty.setProperties(new Property[]{propertyA});
		m_propertyTable.setInput(new Property[]{complexProperty});
		waitEventLoop(0);
		// prepare location
		Point stateLocation = m_propertyTable.forTests_getStateLocation(complexProperty);
		assertNotNull(stateLocation);
		// expand "complex"
		assertEquals(1, m_propertyTable.forTests_getPropertiesCount());
		m_sender.click(stateLocation, 1);
		waitEventLoop(0);
		assertEquals(2, m_propertyTable.forTests_getPropertiesCount());
		// now update "complexProperty" and include also "propertyB"
		complexProperty.setProperties(new Property[]{propertyA, propertyB});
		m_propertyTable.setInput(new Property[]{complexProperty});
		waitEventLoop(0);
		assertEquals(3, m_propertyTable.forTests_getPropertiesCount());
	}
}
