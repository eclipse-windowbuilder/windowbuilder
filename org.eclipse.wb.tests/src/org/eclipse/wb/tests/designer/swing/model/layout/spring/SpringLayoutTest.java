/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing.model.layout.spring;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.ComponentAttachmentInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Component;
import java.util.List;

import javax.swing.SpringLayout;

/**
 * Test for {@link SpringLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class SpringLayoutTest extends AbstractLayoutTest {
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
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for parsing and model.
	 */
	@Test
	public void test_parse() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new SpringLayout());",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		// hierarchy
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/setLayout(new SpringLayout())/ /add(button)/}",
				"  {new: javax.swing.SpringLayout} {empty} {/setLayout(new SpringLayout())/}",
				"  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
		assertInstanceOf(SpringLayoutInfo.class, panel.getLayout());
		// without constraints "button" is located at (0,0)
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			Rectangle modelBounds = button.getModelBounds();
			assertEquals(modelBounds.x, 0);
			assertEquals(modelBounds.y, 0);
		}
	}

	/**
	 * Test that {@link SpringLayout#putConstraint(String,Component,int,String,Component)} is
	 * executable.
	 */
	@Test
	public void test_putConstraints() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		// "button" is located at (5,10)
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			Rectangle modelBounds = button.getModelBounds();
			assertEquals(modelBounds.x, 5);
			assertEquals(modelBounds.y, 10);
		}
	}

	/**
	 * Test that {@link SpringLayout#putConstraint(String,Component,int,String,Component)} is
	 * executable.
	 */
	@Test
	public void test_putConstraints_beforeAdd() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		// "button" is located at (5,10)
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			Rectangle modelBounds = button.getModelBounds();
			assertEquals(modelBounds.x, 5);
			assertEquals(modelBounds.y, 10);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringLayoutInfo#getAttachment(AbstractComponentInfo, int)}.
	 */
	@Test
	public void test_getAttachment() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		assertFalse(layout.getAttachmentLeft(button).isVirtual());
		assertFalse(layout.getAttachmentTop(button).isVirtual());
		assertTrue(layout.getAttachmentRight(button).isVirtual());
		assertTrue(layout.getAttachmentBottom(button).isVirtual());
	}

	/**
	 * Test for {@link SpringLayoutInfo#isAttached(AbstractComponentInfo, int)}.
	 */
	@Test
	public void test_isVirtual() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		assertTrue(layout.isAttached(button, PositionConstants.LEFT));
		assertTrue(layout.isAttached(button, PositionConstants.TOP));
		assertFalse(layout.isAttached(button, PositionConstants.RIGHT));
		assertFalse(layout.isAttached(button, PositionConstants.BOTTOM));
	}

	/**
	 * Tests for {@link SpringLayoutInfo#getAttachedToWidget(AbstractComponentInfo, int)}.
	 */
	@Test
	public void test_getAttachedToWidget() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JButton button_1 = new JButton();",
						"  private final JButton button_2 = new JButton();",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      add(button_1);",
						"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
						"    }",
						"    {",
						"      add(button_2);",
						"      layout.putConstraint(SpringLayout.WEST, button_2, 5, SpringLayout.EAST, button_1);",
						"      layout.putConstraint(SpringLayout.NORTH, button_2, 0, SpringLayout.NORTH, button_1);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// "button_1" attachments
		{
			assertSame(null, layout.getAttachedToWidget(button_1, PositionConstants.LEFT));
			assertSame(null, layout.getAttachedToWidget(button_1, PositionConstants.TOP));
			assertSame(null, layout.getAttachedToWidget(button_1, PositionConstants.RIGHT));
			assertSame(null, layout.getAttachedToWidget(button_1, PositionConstants.BOTTOM));
		}
		// "button_2" attachments
		{
			assertSame(button_1, layout.getAttachedToWidget(button_2, PositionConstants.LEFT));
			assertSame(button_1, layout.getAttachedToWidget(button_2, PositionConstants.TOP));
			assertSame(null, layout.getAttachedToWidget(button_2, PositionConstants.RIGHT));
			assertSame(null, layout.getAttachedToWidget(button_2, PositionConstants.BOTTOM));
		}
	}

	/**
	 * Tests for {@link SpringLayoutInfo#getComponentAttachmentInfo(AbstractComponentInfo, int)}.
	 */
	@Test
	@Deprecated
	public void test_getComponentAttachmentInfo_parent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.EAST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// LEFT
		{
			ComponentAttachmentInfo attachment =
					layout.getComponentAttachmentInfo(button, PositionConstants.LEFT);
			assertNull(attachment);
		}
		// TOP
		{
			ComponentAttachmentInfo attachment =
					layout.getComponentAttachmentInfo(button, PositionConstants.TOP);
			assertNull(attachment);
		}
	}

	/**
	 * Tests for {@link SpringLayoutInfo#getComponentAttachmentInfo(AbstractComponentInfo, int)}.
	 */
	@Test
	@Deprecated
	public void test_getComponentAttachmentInfo() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    JButton targetButton = new JButton();",
						"    add(targetButton);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.EAST, targetButton);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, targetButton);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo targetButton = panel.getChildrenComponents().get(0);
		ComponentInfo button = panel.getChildrenComponents().get(1);
		// LEFT
		{
			ComponentAttachmentInfo attachment =
					layout.getComponentAttachmentInfo(button, PositionConstants.LEFT);
			assertNotNull(attachment);
			assertSame(targetButton, attachment.getTarget());
			assertEquals(PositionConstants.RIGHT, attachment.getAlignment());
		}
		// TOP
		{
			ComponentAttachmentInfo attachment =
					layout.getComponentAttachmentInfo(button, PositionConstants.TOP);
			assertNotNull(attachment);
			assertSame(targetButton, attachment.getTarget());
			assertEquals(PositionConstants.TOP, attachment.getAlignment());
		}
		// RIGHT
		{
			ComponentAttachmentInfo attachment =
					layout.getComponentAttachmentInfo(button, PositionConstants.RIGHT);
			assertNull(attachment);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Constraints" property
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_property_constraints() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		Property property = PropertyUtils.getByPath(button, "Constraints");
		// state
		assertNotNull(property);
		assertTrue(property.isModified());
	}

	@Test
	public void test_property_attachmentRemove() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		Property property = PropertyUtils.getByPath(button, "Constraints/WEST");
		// initial state
		assertNotNull(property);
		assertTrue(property.isModified());
		// remove
		property.setValue(Property.UNKNOWN_VALUE);
		assertFalse(property.isModified());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertEquals(0, button.getBounds().x);
	}

	/**
	 * Check property values for virtual attachment.
	 */
	@Test
	public void test_property_virtualGet() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		//
		{
			Property offset = PropertyUtils.getByPath(button, "Constraints/WEST/offset");
			assertFalse(offset.isModified());
			assertNull(offset.getValue());
		}
		{
			Property anchor = PropertyUtils.getByPath(button, "Constraints/WEST/anchor");
			assertFalse(anchor.isModified());
			assertNull(anchor.getValue());
		}
		{
			Property side = PropertyUtils.getByPath(button, "Constraints/WEST/side");
			assertFalse(side.isModified());
			assertNull(side.getValue());
		}
	}

	@Test
	public void test_property_setOffset() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		Property offset = PropertyUtils.getByPath(button, "Constraints/WEST/offset");
		// initial state
		assertTrue(offset.isModified());
		assertEquals(offset.getValue(), 10);
		// set new
		offset.setValue(50);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 50, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		assertTrue(offset.isModified());
		assertEquals(offset.getValue(), 50);
		// ignore "remove"
		{
			String source = m_lastEditor.getSource();
			offset.setValue(Property.UNKNOWN_VALUE);
			assertEquals(source, m_lastEditor.getSource());
		}
	}

	@Test
	public void test_property_setOffset_virtual() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		//
		Property offset = PropertyUtils.getByPath(button, "Constraints/WEST/offset");
		offset.setValue(10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_property_setAnchorSide() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		//
		Property anchorSide = PropertyUtils.getByPath(button, "Constraints/NORTH/side");
		assertTrue(anchorSide.isModified());
		assertEquals(anchorSide.getValue(), SpringLayout.NORTH);
		anchorSide.setValue(SpringLayout.SOUTH);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.SOUTH, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_property_setAnchorSide_virtual() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
		refresh();
		ComponentInfo button = getJavaInfoByName("button");
		//
		Property anchorSide = PropertyUtils.getByPath(button, "Constraints/NORTH/side");
		assertFalse(anchorSide.isModified());
		anchorSide.setValue(SpringLayout.SOUTH);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.NORTH, button, 0, SpringLayout.SOUTH, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_property_setAnchor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout springLayout = new SpringLayout();",
						"    setLayout(springLayout);",
						"    //",
						"    JButton button_1 = new JButton();",
						"    add(button_1);",
						"    //",
						"    JButton button_2 = new JButton();",
						"    springLayout.putConstraint(SpringLayout.WEST, button_2, 10, SpringLayout.EAST, this);",
						"    add(button_2);",
						"  }",
						"}");
		refresh();
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		//
		Property anchor = PropertyUtils.getByPath(button_2, "Constraints/WEST/anchor");
		assertTrue(anchor.isModified());
		assertSame(anchor.getValue(), panel);
		anchor.setValue(button_1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    JButton button_2 = new JButton();",
				"    springLayout.putConstraint(SpringLayout.WEST, button_2, 10, SpringLayout.EAST, button_1);",
				"    add(button_2);",
				"  }",
				"}");
	}

	@Test
	public void test_property_setAnchor_virtual() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    JButton button_2 = new JButton();",
				"    add(button_2);",
				"  }",
				"}");
		refresh();
		ComponentInfo button_1 = getJavaInfoByName("button_1");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		//
		Property anchor = PropertyUtils.getByPath(button_2, "Constraints/WEST/anchor");
		assertFalse(anchor.isModified());
		assertEquals(null, getPropertyText(anchor));
		anchor.setValue(button_1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    JButton button_2 = new JButton();",
				"    springLayout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
				"    add(button_2);",
				"  }",
				"}");
	}

	/**
	 * Test combo {@link PropertyEditor} of "anchor" property.
	 */
	@Test
	public void test_property_setAnchor_combo() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    JButton button_2 = new JButton();",
				"    springLayout.putConstraint(SpringLayout.WEST, button_2, 10, SpringLayout.EAST, this);",
				"    add(button_2);",
				"  }",
				"}");
		refresh();
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		Property property = PropertyUtils.getByPath(button_2, "Constraints/WEST/anchor");
		//
		addComboPropertyItems(property);
		// check items
		{
			List<String> items = getComboPropertyItems();
			Assertions.assertThat(items).containsExactly("(javax.swing.JPanel)", "button_1");
		}
		// select current item
		{
			setComboPropertySelection(1);
			setComboPropertySelection(property);
			assertEquals(0, getComboPropertySelection());
		}
		// set new item
		setComboPropertyValue(property, 1);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    //",
				"    JButton button_1 = new JButton();",
				"    add(button_1);",
				"    //",
				"    JButton button_2 = new JButton();",
				"    springLayout.putConstraint(SpringLayout.WEST, button_2, 10, SpringLayout.EAST, button_1);",
				"    add(button_2);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout manipulation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringLayoutInfo#detach(AbstractComponentInfo, int)}.
	 */
	@Test
	public void test_detach() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// attached initially
		assertTrue(layout.isAttached(button, PositionConstants.LEFT));
		// detach
		layout.detach(button, PositionConstants.LEFT);
		assertFalse(layout.isAttached(button, PositionConstants.LEFT));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
				"    }",
				"  }",
				"}");
		// detach again, no change
		layout.detach(button, PositionConstants.LEFT);
		assertFalse(layout.isAttached(button, PositionConstants.LEFT));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"      layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_attachAbsolute() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		layout.attachAbsolute(button, PositionConstants.LEFT, 5);
		layout.attachAbsolute(button, PositionConstants.BOTTOM, 10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
				"      layout.putConstraint(SpringLayout.SOUTH, button, -10, SpringLayout.SOUTH, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#adjustAttachmentOffset(AbstractComponentInfo, int, int)}.
	 */
	@Test
	public void test_adjustAttachmentOffset() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		layout.adjustAttachmentOffset(button, PositionConstants.LEFT, 10);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 15, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// attachWidgetSequientially()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for
	 * {@link SpringLayoutInfo#attachWidgetSequientially(AbstractComponentInfo, AbstractComponentInfo, int, int)}
	 * .
	 */
	@Test
	public void test_attachWidgetSequientially_leadingSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton buttonA = new JButton();",
						"      add(buttonA);",
						"    }",
						"    {",
						"      JButton buttonB = new JButton();",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		//
		layout.attachWidgetSequientially(buttonB, buttonA, PositionConstants.LEFT, 5);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    {",
				"      JButton buttonB = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, buttonB, 5, SpringLayout.EAST, buttonA);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for
	 * {@link SpringLayoutInfo#attachWidgetSequientially(AbstractComponentInfo, AbstractComponentInfo, int, int)}
	 * .
	 */
	@Test
	public void test_attachWidgetSequientially_trailingSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton buttonA = new JButton();",
						"      add(buttonA);",
						"    }",
						"    {",
						"      JButton buttonB = new JButton();",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		//
		layout.attachWidgetSequientially(buttonB, buttonA, PositionConstants.RIGHT, 5);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    {",
				"      JButton buttonB = new JButton();",
				"      layout.putConstraint(SpringLayout.EAST, buttonB, -5, SpringLayout.WEST, buttonA);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// attachWidgetParallelly()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for
	 * {@link SpringLayoutInfo#attachWidgetParallelly(AbstractComponentInfo, AbstractComponentInfo, int, int)}
	 * .
	 */
	@Test
	public void test_attachWidgetParallelly_leadingSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton buttonA = new JButton();",
						"      add(buttonA);",
						"    }",
						"    {",
						"      JButton buttonB = new JButton();",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		//
		layout.attachWidgetParallelly(buttonB, buttonA, PositionConstants.LEFT, 5);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    {",
				"      JButton buttonB = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, buttonB, 5, SpringLayout.WEST, buttonA);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for
	 * {@link SpringLayoutInfo#attachWidgetParallelly(AbstractComponentInfo, AbstractComponentInfo, int, int)}
	 * .
	 */
	@Test
	public void test_attachWidgetParallelly_trailingSide() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton buttonA = new JButton();",
						"      add(buttonA);",
						"    }",
						"    {",
						"      JButton buttonB = new JButton();",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		//
		layout.attachWidgetParallelly(buttonB, buttonA, PositionConstants.RIGHT, 5);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    {",
				"      JButton buttonB = new JButton();",
				"      layout.putConstraint(SpringLayout.EAST, buttonB, -5, SpringLayout.EAST, buttonA);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setExplicitSize()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_horizontal_leadingAttached_resizeLeading() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, button, 50, SpringLayout.WEST, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.LEFT, PositionConstants.LEFT, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 50, SpringLayout.WEST, this);",
				"      layout.putConstraint(SpringLayout.EAST, button, "
						+ (50 + preferredSize.width)
						+ ", SpringLayout.WEST, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_horizontal_leadingAttached_resizeTrailing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.LEFT, PositionConstants.RIGHT, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
				"      layout.putConstraint(SpringLayout.EAST, button, "
						+ (5 + preferredSize.width + 10)
						+ ", SpringLayout.WEST, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_horizontal_trailingAttached_resizeTrailing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.EAST, button, -50, SpringLayout.EAST, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.RIGHT, PositionConstants.RIGHT, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, "
						+ (-50 - preferredSize.width)
						+ ", SpringLayout.EAST, this);",
						"      layout.putConstraint(SpringLayout.EAST, button, -50, SpringLayout.EAST, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_horizontal_trailingAttached_resizeLeading() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.EAST, button, -5, SpringLayout.EAST, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.RIGHT, PositionConstants.LEFT, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, "
						+ (-5 - preferredSize.width - 10)
						+ ", SpringLayout.EAST, this);",
						"      layout.putConstraint(SpringLayout.EAST, button, -5, SpringLayout.EAST, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_vertical_leadingAttached_resizeLeading() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button, 50, SpringLayout.NORTH, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.TOP, PositionConstants.TOP, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button, 50, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.SOUTH, button, "
						+ (50 + preferredSize.height)
						+ ", SpringLayout.NORTH, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_vertical_leadingAttached_resizeTrailing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.TOP, PositionConstants.BOTTOM, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.SOUTH, button, "
						+ (5 + preferredSize.height + 10)
						+ ", SpringLayout.NORTH, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_vertical_trailingAttached_resizeTrailing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.SOUTH, button, -50, SpringLayout.SOUTH, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.BOTTOM, PositionConstants.BOTTOM, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button, "
						+ (-50 - preferredSize.height)
						+ ", SpringLayout.SOUTH, this);",
						"      layout.putConstraint(SpringLayout.SOUTH, button, -50, SpringLayout.SOUTH, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize_vertical_trailingAttached_resizeLeading() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button = new JButton();",
						"      layout.putConstraint(SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, this);",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Dimension preferredSize = button.getPreferredSize();
		// resize +10
		layout.setExplicitSize(button, PositionConstants.BOTTOM, PositionConstants.TOP, +10);
		panel.refresh();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button, "
						+ (-5 - preferredSize.height - 10)
						+ ", SpringLayout.SOUTH, this);",
						"      layout.putConstraint(SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, this);",
						"      add(button);",
						"    }",
						"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE, i.e. reorder
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringLayoutInfo#command_MOVE(ComponentInfo, ComponentInfo)}.
	 */
	@Test
	public void test_MOVE_absolute() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton button_1 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
						"      add(button_1);",
						"    }",
						"    {",
						"      JButton button_2 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_2, 20, SpringLayout.WEST, this);",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		layout.command_MOVE(button_1, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button_2 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_2, 20, SpringLayout.WEST, this);",
				"      add(button_2);",
				"    }",
				"    {",
				"      JButton button_1 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
				"      add(button_1);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#command_MOVE(ComponentInfo, ComponentInfo)}.
	 */
	@Test
	public void test_MOVE_source() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  private JButton button_2;",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      button_1 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
						"      add(button_1);",
						"    }",
						"    {",
						"      button_2 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		ComponentInfo button_2 = panel.getChildrenComponents().get(1);
		// move forward
		layout.command_MOVE(button_2, button_1);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button_1;",
				"  private JButton button_2;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      button_2 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
				"      add(button_2);",
				"    }",
				"    {",
				"      button_1 = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
				"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
				"      add(button_1);",
				"    }",
				"  }",
				"}");
		// move backward
		layout.command_MOVE(button_2, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button_1;",
				"  private JButton button_2;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      button_1 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
				"      add(button_1);",
				"    }",
				"    {",
				"      button_2 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
				"      add(button_2);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SpringLayoutInfo#command_MOVE(ComponentInfo, ComponentInfo)}.
	 */
	@Test
	public void test_MOVE_anchor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton button_1;",
						"  private JButton button_2;",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      button_1 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
						"      add(button_1);",
						"    }",
						"    {",
						"      button_2 = new JButton();",
						"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
						"      layout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
						"      add(button_2);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button_1 = panel.getChildrenComponents().get(0);
		//
		layout.command_MOVE(button_1, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button_1;",
				"  private JButton button_2;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      button_2 = new JButton();",
				"      layout.putConstraint(SpringLayout.NORTH, button_2, 100, SpringLayout.NORTH, this);",
				"      add(button_2);",
				"    }",
				"    {",
				"      button_1 = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, button_1);",
				"      layout.putConstraint(SpringLayout.NORTH, button_1, 10, SpringLayout.NORTH, this);",
				"      layout.putConstraint(SpringLayout.WEST, button_1, 5, SpringLayout.WEST, this);",
				"      add(button_1);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setLayout()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Convert into {@link SpringLayoutInfo}.
	 */
	@Test
	public void test_setLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"      button.setBounds(10, 10, 50, 50);",
						"    }",
						"  }",
						"}");
		refresh();
		// set layout
		SpringLayoutInfo sl = createJavaInfo("javax.swing.SpringLayout");
		panel.setLayout(sl);
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout springLayout = new SpringLayout();",
				"    setLayout(springLayout);",
				"    {",
				"      JButton button = new JButton();",
				"      springLayout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.NORTH, this);",
				"      springLayout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      springLayout.putConstraint(SpringLayout.SOUTH, button, 60, SpringLayout.NORTH, this);",
				"      springLayout.putConstraint(SpringLayout.EAST, button, 60, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}
}
