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
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BoxLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.BoxSupport;
import org.eclipse.wb.internal.swing.model.property.editor.alignment.AlignmentXPropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.alignment.AlignmentYPropertyEditor;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.junit.Test;

import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 * Test for {@link BoxLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class BoxLayoutTest extends AbstractLayoutTest {
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
	 * Test for installing.
	 */
	@Test
	public void test_setLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		setLayout(panel, BoxLayout.class);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));",
				"  }",
				"}");
		assertInstanceOf(BoxLayoutInfo.class, panel.getLayout());
	}

	/**
	 * {@link BoxLayoutInfo} should add compound system "Alignment" property for each child
	 * {@link ComponentInfo}.
	 */
	@Test
	public void test_propertyAlignment() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));",
						"    {",
						"      JButton button = new JButton();",
						"      add(button);",
						"    }",
						"  }",
						"}");
		assertInstanceOf(BoxLayoutInfo.class, panel.getLayout());
		panel.refresh();
		// check for "Alignment" property of "button"
		ComponentInfo button = panel.getChildrenComponents().get(0);
		Property alignmentProperty = button.getPropertyByTitle("Alignment");
		assertTrue(alignmentProperty.getCategory().isSystem());
		// check sub-properties
		Property[] subProperties = getSubProperties(alignmentProperty);
		assertEquals(2, subProperties.length);
		assertEquals("alignmentX", subProperties[0].getTitle());
		assertEquals("alignmentY", subProperties[1].getTitle());
		assertFalse(subProperties[0].getCategory().isAdvanced());
		assertFalse(subProperties[1].getCategory().isAdvanced());
		assertInstanceOf(AlignmentXPropertyEditor.class, subProperties[0].getEditor());
		assertInstanceOf(AlignmentYPropertyEditor.class, subProperties[1].getEditor());
	}

	/**
	 * We should not create property for "Constructor/target", because it causes infinite recursion
	 * during displaying {@link ContainerInfo} properties.
	 */
	@Test
	public void test_noConstructor_targetProperty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));",
						"  }",
						"}");
		BoxLayoutInfo layout = (BoxLayoutInfo) panel.getLayout();
		//
		assertNotNull(PropertyUtils.getByPath(layout, "Constructor/axis"));
		assertNull(PropertyUtils.getByPath(layout, "Constructor/target"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isHorizontal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link BoxLayoutInfo#isHorizontal()}, and {@link BoxLayout#X_AXIS} style.
	 */
	@Test
	public void test_isHorizontal_X_AXIS() throws Exception {
		check_isHorizontal("BoxLayout.X_AXIS", true);
	}

	/**
	 * Test for {@link BoxLayoutInfo#isHorizontal()}, and {@link BoxLayout#LINE_AXIS} style.
	 */
	@Test
	public void test_isHorizontal_LINE_AXIS() throws Exception {
		check_isHorizontal("BoxLayout.LINE_AXIS", true);
	}

	/**
	 * Test for {@link BoxLayoutInfo#isHorizontal()}, and {@link BoxLayout#Y_AXIS} style.
	 */
	@Test
	public void test_isHorizontal_Y_AXIS() throws Exception {
		check_isHorizontal("BoxLayout.Y_AXIS", false);
	}

	/**
	 * Test for {@link BoxLayoutInfo#isHorizontal()}, and {@link BoxLayout#PAGE_AXIS} style.
	 */
	@Test
	public void test_isHorizontal_PAGE_AXIS() throws Exception {
		check_isHorizontal("BoxLayout.PAGE_AXIS", false);
	}

	/**
	 * Checks {@link BoxLayoutInfo#isHorizontal()}.
	 */
	private void check_isHorizontal(String axisSource, boolean expectedHorizontal) throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(new BoxLayout(this, " + axisSource + "));",
						"  }",
						"}");
		panel.refresh();
		BoxLayoutInfo layout = (BoxLayoutInfo) panel.getLayout();
		assertEquals(expectedHorizontal, layout.isHorizontal());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Box
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link Box#createHorizontalBox()} returns {@link ContainerInfo} with
	 * {@link BoxLayoutInfo}.
	 */
	@Test
	public void test_Box_createHorizontalBox() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JComponent box = Box.createHorizontalBox();",
				"    add(box);",
				"  }",
				"}");
		refresh();
		ContainerInfo box = getJavaInfoByName("box");
		// has BoxLayout
		{
			assertTrue(box.hasLayout());
			BoxLayoutInfo boxLayout = (BoxLayoutInfo) box.getLayout();
			assertTrue(boxLayout.isHorizontal());
		}
		// can not change LayoutManager
		assertFalse(box.canSetLayout());
	}

	/**
	 * Test that {@link Box#createVerticalBox()} returns {@link ContainerInfo} with
	 * {@link BoxLayoutInfo}.
	 */
	@Test
	public void test_Box_createVerticalBox() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JComponent box = Box.createVerticalBox();",
				"    add(box);",
				"  }",
				"}");
		refresh();
		ContainerInfo box = getJavaInfoByName("box");
		// has BoxLayout
		{
			assertTrue(box.hasLayout());
			BoxLayoutInfo boxLayout = (BoxLayoutInfo) box.getLayout();
			assertFalse(boxLayout.isHorizontal());
		}
		// can not change LayoutManager
		assertFalse(box.canSetLayout());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BoxSupport
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link BoxSupport#setStrutSize(ComponentInfo, String)};
	 */
	@Test
	public void test_BoxSupport_setStrutSize() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    Component strut = Box.createHorizontalStrut(10);",
						"    add(strut);",
						"  }",
						"}");
		ComponentInfo strut = panel.getChildrenComponents().get(0);
		// set new size
		BoxSupport.setStrutSize(strut, IntegerConverter.INSTANCE.toJavaSource(panel, 20));
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    Component strut = Box.createHorizontalStrut(20);",
				"    add(strut);",
				"  }",
				"}");
	}
}
