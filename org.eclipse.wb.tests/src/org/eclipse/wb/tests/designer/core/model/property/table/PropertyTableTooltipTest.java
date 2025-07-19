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
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.IPropertyTooltipSite;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.junit.jupiter.api.Test;

/**
 * Tests for basic {@link PropertyTable} features.
 *
 * @author scheglov_ke
 */
public class PropertyTableTooltipTest extends AbstractPropertyTableTest {
	private static final PropertyEditor stringEditor = StringPropertyEditor.INSTANCE;

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for hover over empty space - no property.
	 */
	@Test
	public void test_1_noProperty() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		waitEventLoop(10);
		assertNull(getTooltip());
		//
		doHoverTooltip(50, 100);
		assertNull(getTooltip());
	}

	/**
	 * Test for long property title tooltip.
	 */
	@Test
	public void test_2_shortPropertyTitle() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		waitEventLoop(10);
		assertNull(getTooltip());
		//
		doHoverTitleTooltip();
		assertNull(getTooltip());
	}

	/**
	 * Test for long property title tooltip, hide using MouseExit.
	 */
	@Test
	public void test_3_longPropertyTitle_hideExit() throws Exception {
		prepare_test_3_showTooltip();
		// move mouse outside - hide tooltip
		m_sender.postMouseMove(100, 15);
		waitEventLoop(10);
		assertNotNull(getTooltip());
		m_sender.mouseExit(50, 100);
		waitEventLoop(10);
		assertNull(getTooltip());
	}

	/**
	 * Test for long property title tooltip, hide using MousePressed/MouseReleased.
	 */
	@Test
	public void test_3_longPropertyTitle_hideClick() throws Exception {
		prepare_test_3_showTooltip();
		// click on tooltip - hide tooltip
		m_sender.postMouseMove(55, 10);
		waitEventLoop(10);
		assertNotNull(getTooltip());
		getTooltipSender().click();
		waitEventLoop(10);
		assertNull(getTooltip());
	}

	private void prepare_test_3_showTooltip() throws Exception {
		Property property =
				new TestProperty("textTextText0123456789", true, "New button", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		waitEventLoop(10);
		assertNull(getTooltip());
		// show tooltip
		doHoverTitleTooltip();
		assertNotNull(getTooltip());
	}

	/**
	 * Test for {@link PropertyTooltipTextProvider}.
	 */
	@Test
	public void test_4_textProvider() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor) {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == PropertyTooltipProvider.class) {
					return adapter.cast(new PropertyTooltipTextProvider() {
						@Override
						protected String getText(Property _property) {
							return "aaaaaa bbbbb ccccccc dddddddd eeeeeee fffffffff ggggggggg hhhhhh iiiii jjjjj.";
						}
					});
				}
				return super.getAdapter(adapter);
			}
		};
		m_propertyTable.setInput(new Property[]{property});
		// show tooltip
		{
			doHoverTitleTooltip();
			Shell tooltip = getTooltip();
			assertNotNull(tooltip);
			assertTrue(tooltip.getSize().y > 30);
		}
	}

	/**
	 * Test for {@link PropertyTooltipProvider} that returns <code>null</code> as control.
	 */
	@Test
	public void test_5_emptyProvider() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor) {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == PropertyTooltipProvider.class) {
					return adapter.cast(new PropertyTooltipProvider() {
						@Override
						public Control createTooltipControl(Property _property,
								Composite parent,
								IPropertyTooltipSite site) {
							return null;
						}
					});
				}
				return super.getAdapter(adapter);
			}
		};
		m_propertyTable.setInput(new Property[]{property});
		// show tooltip
		{
			doHoverTitleTooltip();
			assertNull(getTooltip());
		}
	}

	/**
	 * Test for {@link PropertyTooltipTextProvider} with {@link PropertyTooltipProvider#BELOW}.
	 */
	@Test
	public void test_6_textProviderBelow() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor) {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter == PropertyTooltipProvider.class) {
					return adapter.cast(new PropertyTooltipTextProvider() {
						@Override
						public int getTooltipPosition() {
							return BELOW;
						}

						@Override
						protected String getText(Property _property) {
							return "My tooltip below.";
						}
					});
				}
				return super.getAdapter(adapter);
			}
		};
		m_propertyTable.setInput(new Property[]{property});
		// show tooltip
		{
			doHoverTitleTooltip();
			Shell tooltip = getTooltip();
			assertNotNull(tooltip);
			int location = tooltip.getLocation().y;
			int expectedLocation = m_shell.getLocation().y + 10;
			assertTrue(location > expectedLocation, () -> location + " not below " + expectedLocation);
		}
		// hide using exit from PropertyTable
		m_sender.mouseExit(50, 100);
		waitEventLoop(10);
		assertNull(getTooltip());
	}

	/**
	 * Short text in {@link TextDisplayPropertyEditor} - no tooltip.
	 */
	@Test
	public void test_7_value_noTooltip() throws Exception {
		Property property = new TestProperty("text", true, "New button", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		waitEventLoop(10);
		assertNull(getTooltip());
		// show tooltip
		doHoverValueTooltip();
		waitEventLoop(10);
		assertNull(getTooltip());
	}

	/**
	 * Long text in {@link TextDisplayPropertyEditor} - show tooltip.
	 */
	@Test
	public void test_8_value_textTooltip() throws Exception {
		Property property =
				new TestProperty("text", true, "New button 01234567890123456789", stringEditor);
		m_propertyTable.setInput(new Property[]{property});
		waitEventLoop(10);
		assertNull(getTooltip());
		// show tooltip
		doHoverValueTooltip();
		waitEventLoop(10);
		assertNotNull(getTooltip());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Hovers on title.
	 */
	private void doHoverTitleTooltip() throws InterruptedException {
		doHoverTooltip(m_propertyTable.getSplitter() - 10, 10);
	}

	/**
	 * Hovers on value.
	 */
	private void doHoverValueTooltip() throws InterruptedException {
		doHoverTooltip(m_propertyTable.getSplitter() + 10, 10);
	}

	/**
	 * Hovers on given coordinates.
	 */
	private void doHoverTooltip(int x, int y) throws InterruptedException {
		m_sender.moveTo(x, y);
		waitEventLoop(10);
		m_sender.mouseHover(x, y);
		waitEventLoop(10);
	}
}
