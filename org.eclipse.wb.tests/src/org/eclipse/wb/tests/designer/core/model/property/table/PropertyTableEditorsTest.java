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
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.swt.graphics.Point;

import org.junit.Test;

/**
 * Tests for {@link PropertyEditor}'s in {@link PropertyTable}.
 *
 * @author scheglov_ke
 */
public class PropertyTableEditorsTest extends AbstractPropertyTableTest {
	private static final PropertyEditor stringEditor = StringPropertyEditor.INSTANCE;

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Click in empty space - no editor.
	 */
	@Test
	public void test_1_noProperty() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		assertNull(m_propertyTable.forTests_getActiveEditor());
		// click empty space - no editor
		m_sender.click(m_propertyTable.forTests_getSplitter() + 10, 100, 1);
		waitEventLoop(10);
		assertNull(m_propertyTable.forTests_getActiveEditor());
	}

	/**
	 * Test {@link PropertyEditor} activate/deactivate using {@link StringPropertyEditor}.
	 */
	@Test
	public void test_2_activateDeactivate() throws Exception {
		PropertyEditor xEditor = StringPropertyEditor.INSTANCE;
		PropertyEditor yEditor = StringPropertyEditor.INSTANCE;
		Property propertyX = new TestProperty("x", true, "100", xEditor);
		Property propertyY = new TestProperty("y", true, "200", yEditor);
		Property propertyXY =
				new TestProperty("location", true, "100, 200", new ComplexEditor(new Property[]{
						propertyX,
						propertyY}));
		m_propertyTable.setInput(new Property[]{propertyXY});
		waitEventLoop(10);
		// prepare locations
		assertNull(m_propertyTable.forTests_getValueLocation(null));
		Point locationSXY = m_propertyTable.forTests_getStateLocation(propertyXY);
		// expand "location"
		{
			m_sender.click(locationSXY, 1);
			waitEventLoop(10);
			assertNull(m_propertyTable.forTests_getActiveEditor());
		}
		// activate "x"
		{
			Point locationVX = m_propertyTable.forTests_getValueLocation(propertyX);
			m_sender.click(locationVX, 1);
			assertSame(xEditor, m_propertyTable.forTests_getActiveEditor());
			waitEventLoop(10);
		}
		// activate "y"
		{
			Point locationVY = m_propertyTable.forTests_getValueLocation(propertyY);
			m_sender.click(locationVY, 1);
			assertSame(yEditor, m_propertyTable.forTests_getActiveEditor());
			waitEventLoop(10);
		}
		// collapse "location"
		{
			m_sender.click(locationSXY, 1);
			waitEventLoop(10);
			assertNull(m_propertyTable.forTests_getActiveEditor());
		}
	}
}
