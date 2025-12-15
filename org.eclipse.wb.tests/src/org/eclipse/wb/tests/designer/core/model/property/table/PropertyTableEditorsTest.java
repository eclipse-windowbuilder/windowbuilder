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
package org.eclipse.wb.tests.designer.core.model.property.table;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.tests.utils.PropertyTableUtils;

import org.eclipse.swt.graphics.Point;

import org.junit.jupiter.api.Test;

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
		assertNull(PropertyTableUtils.getActiveEditor(m_propertyTable));
		// click empty space - no editor
		m_sender.click(m_propertyTable.getSplitter() + 10, 100, 1);
		waitEventLoop(10);
		assertNull(PropertyTableUtils.getActiveEditor(m_propertyTable));
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
		assertNull(PropertyTableUtils.getValueLocation(m_propertyTable, null));
		Point locationSXY = PropertyTableUtils.getStateLocation(m_propertyTable, propertyXY);
		// expand "location"
		{
			m_sender.click(locationSXY, 1);
			waitEventLoop(10);
			assertNull(PropertyTableUtils.getActiveEditor(m_propertyTable));
		}
		// activate "x"
		{
			Point locationVX = PropertyTableUtils.getValueLocation(m_propertyTable, propertyX);
			m_sender.click(locationVX, 1);
			assertSame(xEditor, PropertyTableUtils.getActiveEditor(m_propertyTable));
			waitEventLoop(10);
		}
		// activate "y"
		{
			Point locationVY = PropertyTableUtils.getValueLocation(m_propertyTable, propertyY);
			m_sender.click(locationVY, 1);
			assertSame(yEditor, PropertyTableUtils.getActiveEditor(m_propertyTable));
			waitEventLoop(10);
		}
		// collapse "location"
		{
			m_sender.click(locationSXY, 1);
			waitEventLoop(10);
			assertNull(PropertyTableUtils.getActiveEditor(m_propertyTable));
		}
	}
}
