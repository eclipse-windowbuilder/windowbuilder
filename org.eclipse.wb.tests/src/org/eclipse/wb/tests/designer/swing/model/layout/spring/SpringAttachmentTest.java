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
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringAttachmentInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.jupiter.api.Test;

import javax.swing.SpringLayout;

/**
 * Test for {@link SpringAttachmentInfo}.
 *
 * @author scheglov_ke
 */
public class SpringAttachmentTest extends AbstractLayoutTest {
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
	// Test
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringAttachmentInfo#getSpringSide(int)}.
	 */
	@Test
	public void test_getSpringSide() throws Exception {
		assertSame(SpringLayout.WEST, SpringAttachmentInfo.getSpringSide(PositionConstants.LEFT));
		assertSame(SpringLayout.EAST, SpringAttachmentInfo.getSpringSide(PositionConstants.RIGHT));
		assertSame(SpringLayout.NORTH, SpringAttachmentInfo.getSpringSide(PositionConstants.TOP));
		assertSame(SpringLayout.SOUTH, SpringAttachmentInfo.getSpringSide(PositionConstants.BOTTOM));
		try {
			SpringAttachmentInfo.getSpringSide(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test for {@link SpringAttachmentInfo#getSpringSide(int)}.
	 */
	@Test
	public void test_getSpringSideSource() throws Exception {
		assertEquals(
				"javax.swing.SpringLayout.WEST",
				SpringAttachmentInfo.getSpringSideSource(PositionConstants.LEFT));
		assertEquals(
				"javax.swing.SpringLayout.EAST",
				SpringAttachmentInfo.getSpringSideSource(PositionConstants.RIGHT));
		assertEquals(
				"javax.swing.SpringLayout.NORTH",
				SpringAttachmentInfo.getSpringSideSource(PositionConstants.TOP));
		assertEquals(
				"javax.swing.SpringLayout.SOUTH",
				SpringAttachmentInfo.getSpringSideSource(PositionConstants.BOTTOM));
		try {
			SpringAttachmentInfo.getSpringSideSource(-1);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test for {@link SpringAttachmentInfo#getFrameworkSide(String)}.
	 */
	@Test
	public void test_getFrameworkSide() throws Exception {
		assertEquals(PositionConstants.LEFT, SpringAttachmentInfo.getFrameworkSide(SpringLayout.WEST));
		assertEquals(PositionConstants.TOP, SpringAttachmentInfo.getFrameworkSide(SpringLayout.NORTH));
		assertEquals(PositionConstants.RIGHT, SpringAttachmentInfo.getFrameworkSide(SpringLayout.EAST));
		assertEquals(
				PositionConstants.BOTTOM,
				SpringAttachmentInfo.getFrameworkSide(SpringLayout.SOUTH));
		try {
			SpringAttachmentInfo.getFrameworkSide("no such side");
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Test for {@link SpringLayoutInfo#getAttachment(AbstractComponentInfo, int)}.
	 */
	@Test
	public void test_getAttchment() throws Exception {
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
		// ask for attachment
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
		// attachments are cached
		assertSame(attachment, layout.getAttachmentLeft(button));
		// ask for other  sides
		assertNotSame(attachment, layout.getAttachmentRight(button));
		assertNotSame(attachment, layout.getAttachmentTop(button));
		assertNotSame(attachment, layout.getAttachmentBottom(button));
	}

	/**
	 * Test for {@link SpringAttachmentInfo#getSide()}.
	 */
	@Test
	public void test_getSide() throws Exception {
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
		// LEFT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
			assertEquals(PositionConstants.LEFT, attachment.getSide());
		}
		// TOP
		{
			SpringAttachmentInfo attachment = layout.getAttachmentTop(button);
			assertEquals(PositionConstants.TOP, attachment.getSide());
		}
		// RIGHT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentRight(button);
			assertEquals(PositionConstants.RIGHT, attachment.getSide());
		}
		// BOTTOM
		{
			SpringAttachmentInfo attachment = layout.getAttachmentBottom(button);
			assertEquals(PositionConstants.BOTTOM, attachment.getSide());
		}
	}

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
		// "button" is located at (5,10)
		{
			Rectangle modelBounds = button.getModelBounds();
			assertEquals(modelBounds.x, 5);
			assertEquals(modelBounds.y, 10);
		}
		// LEFT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
			assertFalse(attachment.isVirtual());
		}
		// TOP
		{
			SpringAttachmentInfo attachment = layout.getAttachmentTop(button);
			assertFalse(attachment.isVirtual());
		}
		// RIGHT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentRight(button);
			assertTrue(attachment.isVirtual());
		}
		// BOTTOM
		{
			SpringAttachmentInfo attachment = layout.getAttachmentBottom(button);
			assertTrue(attachment.isVirtual());
		}
	}

	/**
	 * Tests for {@link SpringAttachmentInfo#getAnchorComponent()} and
	 * {@link SpringAttachmentInfo#getAnchorSide()}.
	 */
	@Test
	public void test_getAnchor() throws Exception {
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
		// prepare bounds
		Rectangle bounds_1 = button_1.getModelBounds();
		Rectangle bounds_2 = button_2.getModelBounds();
		// "button_1" is located at (5,10)
		{
			assertEquals(bounds_1.x, 5);
			assertEquals(bounds_1.y, 10);
		}
		// "button_2" is located at (right_1+5,y_1)
		{
			assertEquals(bounds_2.x, bounds_1.right() + 5);
			assertEquals(bounds_2.y, bounds_1.y);
		}
		// "button_1" attachments
		{
			// left
			{
				SpringAttachmentInfo attachment = layout.getAttachmentLeft(button_1);
				assertFalse(attachment.isVirtual());
				assertSame(panel, attachment.getAnchorComponent());
				assertEquals(PositionConstants.LEFT, attachment.getAnchorSide());
				assertEquals(5, attachment.getOffset());
			}
			// top
			{
				SpringAttachmentInfo attachment = layout.getAttachmentTop(button_1);
				assertFalse(attachment.isVirtual());
				assertSame(panel, attachment.getAnchorComponent());
				assertEquals(PositionConstants.TOP, attachment.getAnchorSide());
				assertEquals(10, attachment.getOffset());
			}
		}
		// "button_2" attachments
		{
			// left
			{
				SpringAttachmentInfo attachment = layout.getAttachmentLeft(button_2);
				assertFalse(attachment.isVirtual());
				assertSame(button_1, attachment.getAnchorComponent());
				assertEquals(PositionConstants.RIGHT, attachment.getAnchorSide());
				assertEquals(5, attachment.getOffset());
			}
			// top
			{
				SpringAttachmentInfo attachment = layout.getAttachmentTop(button_2);
				assertFalse(attachment.isVirtual());
				assertSame(button_1, attachment.getAnchorComponent());
				assertEquals(PositionConstants.TOP, attachment.getAnchorSide());
				assertEquals(0, attachment.getOffset());
			}
		}
	}

	/**
	 * Tests for {@link SpringAttachmentInfo#getOffset()}.
	 */
	@Test
	public void test_getDistance() throws Exception {
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
						"      layout.putConstraint(SpringLayout.SOUTH, button, -10, SpringLayout.SOUTH, this);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// left
		{
			SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.LEFT, attachment.getAnchorSide());
			assertEquals(5, attachment.getOffset());
		}
		// bottom
		{
			SpringAttachmentInfo attachment = layout.getAttachmentBottom(button);
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.BOTTOM, attachment.getAnchorSide());
			assertEquals(-10, attachment.getOffset());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Write
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_write_existingAttachment() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
		// initial state
		{
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.LEFT, attachment.getAnchorSide());
			assertEquals(5, attachment.getOffset());
		}
		// set offset
		attachment.setOffset(10);
		attachment.write();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
		// set anchor side
		attachment.setAnchorSide(PositionConstants.RIGHT);
		attachment.write();
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.EAST, this);",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_write_newAttachment() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
		// initial state
		{
			assertTrue(attachment.isVirtual());
			assertSame(null, attachment.getAnchorComponent());
			assertEquals(0, attachment.getAnchorSide());
			assertEquals(0, attachment.getOffset());
		}
		// set offset
		attachment.setAnchorComponent(panel);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
		assertEditor(
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
		assertEquals(
				"{new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/"
						+ " /layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this)/}",
						button.toString());
		// new state
		{
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.LEFT, attachment.getAnchorSide());
			assertEquals(5, attachment.getOffset());
		}
	}

	/**
	 * We mistakenly considered "layout" as visible in "getButton()".
	 */
	@Test
	public void test_write_newAttachment_lazy() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JButton m_button;",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    add(getButton());",
						"  }",
						"  private JButton getButton() {",
						"    if (m_button == null) {",
						"      m_button = new JButton();",
						"    }",
						"    return m_button;",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
		// initial state
		{
			assertTrue(attachment.isVirtual());
			assertSame(null, attachment.getAnchorComponent());
			assertEquals(0, attachment.getAnchorSide());
			assertEquals(0, attachment.getOffset());
		}
		// set offset
		attachment.setAnchorComponent(panel);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton m_button;",
				"  private SpringLayout layout;",
				"  public Test() {",
				"    layout = new SpringLayout();",
				"    setLayout(layout);",
				"    add(getButton());",
				"  }",
				"  private JButton getButton() {",
				"    if (m_button == null) {",
				"      m_button = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, m_button, 5, SpringLayout.WEST, this);",
				"    }",
				"    return m_button;",
				"  }",
				"}");
		assertEquals(
				"{new: javax.swing.JButton} {lazy: m_button getButton()} {"
						+ "/new JButton()/ "
						+ "/m_button/ "
						+ "/add(getButton())/ /layout.putConstraint(SpringLayout.WEST, m_button, 5, SpringLayout.WEST, this)/}",
						button.toString());
		// new state
		{
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.LEFT, attachment.getAnchorSide());
			assertEquals(5, attachment.getOffset());
		}
	}

	/**
	 * <code>putConstraint()</code> invocations should be placed in order NORTH, WEST, SOUTH, EAST.
	 */
	@Test
	public void test_write_newAttachment_sorted() throws Exception {
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
		// add NORTH
		{
			SpringAttachmentInfo attachment = layout.getAttachmentTop(button);
			attachment.setAnchorComponent(panel);
			attachment.setAnchorSide(PositionConstants.TOP);
			attachment.setOffset(5);
			attachment.write();
			assertEditor(
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
		}
		// add LEFT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
			attachment.setAnchorComponent(panel);
			attachment.setAnchorSide(PositionConstants.LEFT);
			attachment.setOffset(5);
			attachment.write();
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    SpringLayout layout = new SpringLayout();",
					"    setLayout(layout);",
					"    {",
					"      JButton button = new JButton();",
					"      layout.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, this);",
					"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
					"      add(button);",
					"    }",
					"  }",
					"}");
		}
		// add BOTTOM
		{
			SpringAttachmentInfo attachment = layout.getAttachmentBottom(button);
			attachment.setAnchorComponent(panel);
			attachment.setAnchorSide(PositionConstants.BOTTOM);
			attachment.setOffset(-5);
			attachment.write();
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    SpringLayout layout = new SpringLayout();",
					"    setLayout(layout);",
					"    {",
					"      JButton button = new JButton();",
					"      layout.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, this);",
					"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
					"      layout.putConstraint(SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, this);",
					"      add(button);",
					"    }",
					"  }",
					"}");
		}
		// add RIGHT
		{
			SpringAttachmentInfo attachment = layout.getAttachmentRight(button);
			attachment.setAnchorComponent(panel);
			attachment.setAnchorSide(PositionConstants.RIGHT);
			attachment.setOffset(-5);
			attachment.write();
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    SpringLayout layout = new SpringLayout();",
					"    setLayout(layout);",
					"    {",
					"      JButton button = new JButton();",
					"      layout.putConstraint(SpringLayout.NORTH, button, 5, SpringLayout.NORTH, this);",
					"      layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.WEST, this);",
					"      layout.putConstraint(SpringLayout.SOUTH, button, -5, SpringLayout.SOUTH, this);",
					"      layout.putConstraint(SpringLayout.EAST, button, -5, SpringLayout.EAST, this);",
					"      add(button);",
					"    }",
					"  }",
					"}");
		}
	}

	@Test
	public void test_write_newAttachment_trailingSide() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentRight(button);
		// initial state
		{
			assertTrue(attachment.isVirtual());
			assertSame(null, attachment.getAnchorComponent());
			assertEquals(0, attachment.getAnchorSide());
			assertEquals(0, attachment.getOffset());
		}
		// set offset
		attachment.setAnchorComponent(panel);
		attachment.setAnchorSide(PositionConstants.RIGHT);
		attachment.setOffset(-5);
		attachment.write();
		assertEditor(
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
		// new state
		{
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
			assertEquals(PositionConstants.RIGHT, attachment.getAnchorSide());
			assertEquals(-5, attachment.getOffset());
		}
	}

	/**
	 * Reference component declared before.
	 */
	@Test
	public void test_write_newAttachment_backReference() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonB);
		attachment.setAnchorComponent(buttonA);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
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
		assertEquals(
				"{new: javax.swing.JButton} {field-unique: buttonA} {/new JButton()/ /add(buttonA)/"
						+ " /layout.putConstraint(SpringLayout.WEST, buttonB, 5, SpringLayout.WEST, buttonA)/}",
						buttonA.toString());
		assertEquals(
				"{new: javax.swing.JButton} {local-unique: buttonB} {/new JButton()/ /add(buttonB)/"
						+ " /layout.putConstraint(SpringLayout.WEST, buttonB, 5, SpringLayout.WEST, buttonA)/}",
						buttonB.toString());
	}

	/**
	 * Reference component declared after.
	 */
	@Test
	public void test_write_newAttachment_forwardReference() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonA);
		attachment.setAnchorComponent(buttonB);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
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
				"      layout.putConstraint(SpringLayout.WEST, buttonA, 5, SpringLayout.WEST, buttonB);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_write_updateAttachment_backReference() throws Exception {
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
						"      layout.putConstraint(SpringLayout.WEST, buttonB, 5, SpringLayout.WEST, this);",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		//
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonB);
		attachment.setAnchorComponent(buttonA);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
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

	@Test
	public void test_write_updateAttachment_forwardReference() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    SpringLayout layout = new SpringLayout();",
						"    setLayout(layout);",
						"    {",
						"      JButton buttonA = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, buttonA, 5, SpringLayout.WEST, this);",
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonA);
		attachment.setAnchorComponent(buttonB);
		attachment.setAnchorSide(PositionConstants.LEFT);
		attachment.setOffset(5);
		attachment.write();
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
				"      layout.putConstraint(SpringLayout.WEST, buttonA, 5, SpringLayout.WEST, buttonB);",
				"      add(buttonB);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// delete()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SpringAttachmentInfo#delete()}.
	 */
	@Test
	public void test_delete() throws Exception {
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
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(button);
		// initial state
		{
			assertFalse(attachment.isVirtual());
			assertSame(panel, attachment.getAnchorComponent());
		}
		// do delete
		attachment.delete();
		assertTrue(attachment.isVirtual());
		assertSame(null, attachment.getAnchorComponent());
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

	////////////////////////////////////////////////////////////////////////////
	//
	// adjustAfterComponentMove()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_adjustAfterComponentMove_source() throws Exception {
		ContainerInfo panel =
				parseContainer(
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
						"      layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonB);
		// move "buttonB" before "buttonA", using core
		panel.startEdit();
		JavaInfoUtils.move(buttonB, null, panel, buttonA);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton buttonB = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
				"      add(buttonB);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"  }",
				"}");
		// perform adjustment
		attachment.adjustAfterComponentMove();
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  private JButton buttonB;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonB = new JButton();",
				"      add(buttonB);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
				"      add(buttonA);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_adjustAfterComponentMove_anchor() throws Exception {
		ContainerInfo panel =
				parseContainer(
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
						"      layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
						"      add(buttonB);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		// move "buttonA" last, using core
		panel.startEdit();
		JavaInfoUtils.move(buttonA, null, panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton buttonB = new JButton();",
				"      add(buttonB);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
				"  }",
				"}");
		// perform adjustment
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonB);
		attachment.adjustAfterComponentMove();
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  private JButton buttonB;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      buttonB = new JButton();",
				"      add(buttonB);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, buttonB, 0, SpringLayout.WEST, buttonA);",
				"      add(buttonA);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Source is converted to {@link FieldUniqueVariableSupport}, but this changes its name.
	 */
	@Test
	public void test_adjustAfterComponentMove_anchor2() throws Exception {
		ContainerInfo panel =
				parseContainer(
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
						"      JButton button_1 = new JButton();",
						"      layout.putConstraint(SpringLayout.WEST, button_1, 0, SpringLayout.WEST, buttonA);",
						"      add(button_1);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
		SpringLayoutInfo layout = (SpringLayoutInfo) panel.getLayout();
		ComponentInfo buttonA = panel.getChildrenComponents().get(0);
		ComponentInfo buttonB = panel.getChildrenComponents().get(1);
		// move "buttonA" last, using core
		panel.startEdit();
		JavaInfoUtils.move(buttonA, null, panel, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      JButton button_1 = new JButton();",
				"      add(button_1);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      add(buttonA);",
				"    }",
				"    layout.putConstraint(SpringLayout.WEST, button_1, 0, SpringLayout.WEST, buttonA);",
				"  }",
				"}");
		// perform adjustment
		SpringAttachmentInfo attachment = layout.getAttachmentLeft(buttonB);
		attachment.adjustAfterComponentMove();
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton buttonA;",
				"  private JButton button;",
				"  public Test() {",
				"    SpringLayout layout = new SpringLayout();",
				"    setLayout(layout);",
				"    {",
				"      button = new JButton();",
				"      add(button);",
				"    }",
				"    {",
				"      buttonA = new JButton();",
				"      layout.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.WEST, buttonA);",
				"      add(buttonA);",
				"    }",
				"  }",
				"}");
	}
}
