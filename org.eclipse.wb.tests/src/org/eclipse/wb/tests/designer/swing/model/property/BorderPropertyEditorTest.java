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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

/**
 * Test for {@link BorderPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class BorderPropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getText_defaultBorder() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals(null, getPropertyText(borderProperty));
	}

	@Test
	public void test_getText_noBorder() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setBorder(null);",
						"  }",
						"}");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals("(no border)", getPropertyText(borderProperty));
	}

	@Test
	public void test_getText_EmptyBorder() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setBorder(new EmptyBorder(0, 0, 0, 0));",
						"  }",
						"}");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals("EmptyBorder", getPropertyText(borderProperty));
	}

	@Test
	public void test_getClipboardSource_EmptyBorder() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      button.setBorder(new EmptyBorder(1, 2, 3, 4));",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = getJavaInfoByName("button");
		// property
		{
			GenericProperty borderProperty = (GenericProperty) button.getPropertyByTitle("border");
			PropertyEditor propertyEditor = borderProperty.getEditor();
			assertEquals(
					"new javax.swing.border.EmptyBorder(1, 2, 3, 4)",
					((IClipboardSourceProvider) propertyEditor).getClipboardSource(borderProperty));
		}
		// do copy/paste
		doCopyPaste(button, new PasteProcedure<ComponentInfo>() {
			@Override
			public void run(ComponentInfo copy) throws Exception {
				((FlowLayoutInfo) panel.getLayout()).add(copy, null);
			}
		});
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      button.setBorder(new EmptyBorder(1, 2, 3, 4));",
				"      add(button);",
				"    }",
				"    {",
				"      JButton button = new JButton();",
				"      button.setBorder(new EmptyBorder(1, 2, 3, 4));",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * If we can not copy/paste expression, because it contains references on variables or something
	 * other, then ignore.
	 */
	@Test
	public void test_getClipboardSource_hasNotConstants() throws Exception {
		final ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JButton button = new JButton();",
						"      int notConst = 4;",
						"      button.setBorder(new EmptyBorder(1, 2, 3, notConst));",
						"      add(button);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo button = getJavaInfoByName("button");
		// property
		{
			GenericProperty borderProperty = (GenericProperty) button.getPropertyByTitle("border");
			PropertyEditor propertyEditor = borderProperty.getEditor();
			IClipboardSourceProvider pe = (IClipboardSourceProvider) propertyEditor;
			String cs = pe.getClipboardSource(borderProperty);
			assertEquals(null, cs);
		}
		// do copy/paste
		doCopyPaste(button, new PasteProcedure<ComponentInfo>() {
			@Override
			public void run(ComponentInfo copy) throws Exception {
				((FlowLayoutInfo) panel.getLayout()).add(copy, null);
			}
		});
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    {",
				"      JButton button = new JButton();",
				"      int notConst = 4;",
				"      button.setBorder(new EmptyBorder(1, 2, 3, notConst));",
				"      add(button);",
				"    }",
				"    {",
				"      JButton button = new JButton();",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}
}