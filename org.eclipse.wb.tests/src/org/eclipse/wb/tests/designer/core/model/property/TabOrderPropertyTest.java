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
package org.eclipse.wb.tests.designer.core.model.property;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.order.TabOrderInfo;
import org.eclipse.wb.internal.core.model.property.order.TabOrderProperty;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.Test;

import java.util.List;

/**
 * Test for {@link TabOrderProperty}.
 *
 * @author lobas_av
 */
public class TabOrderPropertyTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_common() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"  }",
						"}");
		TestTabOrderProperty property = new TestTabOrderProperty(panel, null, null, null, null);
		assertEquals("tab order", property.getTitle());
		assertFalse(property.isModified());
	}

	@Test
	public void test_tooltip() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"  }",
						"}");
		// create property without tooltip
		TestTabOrderProperty property = new TestTabOrderProperty(panel, null, null, null, null);
		//
		assertNull(property.getAdapter(Object.class));
		assertNull(property.getAdapter(PropertyTooltipProvider.class));
		// create property with tooltip
		property = new TestTabOrderProperty(panel, null, null, null, "Tooltip");
		//
		assertNull(property.getAdapter(Object.class));
		PropertyTooltipProvider tooltipProvider = property.getAdapter(PropertyTooltipProvider.class);
		assertInstanceOf(PropertyTooltipTextProvider.class, tooltipProvider);
		assertNotNull(ReflectionUtils.invokeMethod(
				tooltipProvider,
				"getText(org.eclipse.wb.internal.core.model.property.Property)",
				property));
	}

	@Test
	public void test_noValue() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"    JLabel label = new JLabel();",
						"    add(label);",
						"  }",
						"}");
		panel.refresh();
		//
		assertEquals(2, panel.getChildrenComponents().size());
		// prepare defaults
		List<AbstractComponentInfo> defaultControls = Lists.newArrayList();
		defaultControls.add(panel.getChildrenComponents().get(0));
		// create property
		TestTabOrderProperty property =
				new TestTabOrderProperty(panel, panel.getChildrenComponents(), defaultControls, null, null);
		// check value
		Object value = property.getValue();
		assertNotNull(value);
		assertInstanceOf(TabOrderInfo.class, value);
		// check order value
		TabOrderInfo orderInfo = (TabOrderInfo) value;
		assertEquals("", property.getDisplayText());
		assertEquals(2, orderInfo.getInfos().size());
		assertSame(panel.getChildrenComponents().get(0), orderInfo.getInfos().get(0));
		assertSame(panel.getChildrenComponents().get(1), orderInfo.getInfos().get(1));
		//
		assertEquals(1, orderInfo.getOrderedInfos().size());
		assertSame(panel.getChildrenComponents().get(0), orderInfo.getOrderedInfos().get(0));
		// editor
		assertEquals("", getPropertyText(property));
	}

	@Test
	public void test_value() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"    JComboBox combo = new JComboBox();",
						"    add(combo);",
						"    JLabel label = new JLabel();",
						"    add(label);",
						"    setTabOrder(new JComponent[]{label, button});",
						"  }",
						"  public void setTabOrder(JComponent []orders) {",
						"  }",
						"}");
		panel.refresh();
		//
		assertEquals(3, panel.getChildrenComponents().size());
		//
		ComponentInfo button = panel.getChildrenComponents().get(0);
		ComponentInfo combo = panel.getChildrenComponents().get(1);
		ComponentInfo label = panel.getChildrenComponents().get(2);
		//
		// prepare defaults
		List<AbstractComponentInfo> defaultControls = Lists.newArrayList();
		defaultControls.add(button);
		// prepare array
		TypeDeclaration type = (TypeDeclaration) m_lastEditor.getAstUnit().types().get(0);
		ExpressionStatement statement =
				(ExpressionStatement) type.getMethods()[0].getBody().statements().get(6);
		MethodInvocation invocation = (MethodInvocation) statement.getExpression();
		ArrayCreation creation = (ArrayCreation) invocation.arguments().get(0);
		// create property
		TestTabOrderProperty property =
				new TestTabOrderProperty(panel,
						panel.getChildrenComponents(),
						defaultControls,
						creation.getInitializer(),
						null);
		assertTrue(property.isModified());
		// editor
		assertEquals("[label, button]", getPropertyText(property));
		// check value
		Object value = property.getValue();
		assertNotNull(value);
		assertInstanceOf(TabOrderInfo.class, value);
		// check order value
		TabOrderInfo orderInfo = (TabOrderInfo) value;
		assertEquals("[label, button]", property.getDisplayText());
		assertEquals(3, orderInfo.getInfos().size());
		assertSame(label, orderInfo.getInfos().get(0));
		assertSame(button, orderInfo.getInfos().get(1));
		assertSame(combo, orderInfo.getInfos().get(2));
		//
		assertEquals(2, orderInfo.getOrderedInfos().size());
		assertSame(label, orderInfo.getOrderedInfos().get(0));
		assertSame(button, orderInfo.getOrderedInfos().get(1));
		//
		assertFalse(property.assert_isRemoveFlag());
		assertNull(property.assert_getSource());
		// set new "UNKNOWN" value
		property.setValue(Property.UNKNOWN_VALUE);
		assertTrue(property.assert_isRemoveFlag());
		assertNull(property.assert_getSource());
		property.assert_clearState();
		// set new "EMPTY" value
		orderInfo.getOrderedInfos().clear();
		property.setValue(orderInfo);
		assertTrue(property.assert_isRemoveFlag());
		assertNull(property.assert_getSource());
		property.assert_clearState();
		// set new "button label" value
		{
			orderInfo.getOrderedInfos().add(button);
			orderInfo.getOrderedInfos().add(label);
			property.setValue(orderInfo);
			//
			String expected = TemplateUtils.format("'{'{0}, {1}'}'", button, label);
			assertTrue(property.assert_isRemoveFlag());
			assertEquals(expected, property.assert_getSource());
			//
			property.assert_clearState();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static class TestTabOrderProperty extends TabOrderProperty {
		private final List<? extends AbstractComponentInfo> m_allInfos;
		private final List<? extends AbstractComponentInfo> m_defaultInfos;
		private final ArrayInitializer m_initializer;
		private final String m_tooltip;
		private String m_source;
		private boolean m_removeFlag;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public TestTabOrderProperty(JavaInfo container,
				List<? extends AbstractComponentInfo> allInfos,
				List<? extends AbstractComponentInfo> defaultInfos,
				ArrayInitializer initializer,
				String tooltip) {
			super(container);
			m_allInfos = allInfos != null ? allInfos : ImmutableList.<AbstractComponentInfo>of();
			m_defaultInfos = defaultInfos;
			m_initializer = initializer;
			m_tooltip = tooltip;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		//
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected ArrayInitializer getOrderedArray() throws Exception {
			return m_initializer;
		}

		@Override
		protected List<? extends AbstractComponentInfo> getTabPossibleChildren() throws Exception {
			return m_allInfos;
		}

		@Override
		protected boolean isDefaultOrdered(AbstractComponentInfo component) throws Exception {
			return m_defaultInfos.contains(component);
		}

		@Override
		protected void removePropertyAssociation() throws Exception {
			m_removeFlag = true;
		}

		@Override
		protected void setOrderedArraySource(String source) throws Exception {
			m_source = source;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// State
		//
		////////////////////////////////////////////////////////////////////////////
		public boolean assert_isRemoveFlag() {
			return m_removeFlag;
		}

		public String assert_getSource() {
			return m_source;
		}

		public void assert_clearState() {
			m_removeFlag = false;
			m_source = null;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Tooltip
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected String getPropertyTooltipText() {
			return m_tooltip;
		}
	}
}