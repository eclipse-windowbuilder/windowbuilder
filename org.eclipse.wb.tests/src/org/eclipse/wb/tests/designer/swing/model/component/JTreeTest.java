/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.models.tree.TreeModelPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

/**
 * Tests for {@link JTree} support.
 *
 * @author scheglov_ke
 */
public class JTreeTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_JTree_parsing() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"import javax.swing.tree.*;",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JTree tree = new JTree();",
						"    add(tree);",
						"    tree.setModel(new DefaultTreeModel(",
						"      new DefaultMutableTreeNode('(root)') {",
						"        {",
						"          DefaultMutableTreeNode node1 = new DefaultMutableTreeNode('1');",
						"            DefaultMutableTreeNode node2 = new DefaultMutableTreeNode('11');",
						"            node1.add(node2);",
						"          add(node1);",
						"          node1 = new DefaultMutableTreeNode('2');",
						"            node1.add(new DefaultMutableTreeNode('21'));",
						"          add(node1);",
						"        }",
						"      }",
						"    ));",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo treeInfo = panel.getChildrenComponents().get(0);
		JTree treeObject = (JTree) treeInfo.getObject();
		// validate model
		{
			TreeModel model = treeObject.getModel();
			assertNotNull(model);
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
			{
				assertEquals("(root)", rootNode.getUserObject());
				assertEquals(2, rootNode.getChildCount());
			}
			{
				DefaultMutableTreeNode node_1 = (DefaultMutableTreeNode) rootNode.getChildAt(0);
				assertEquals("1", node_1.getUserObject());
				assertEquals(1, node_1.getChildCount());
				{
					DefaultMutableTreeNode node_2 = (DefaultMutableTreeNode) node_1.getChildAt(0);
					assertEquals("11", node_2.getUserObject());
					assertEquals(0, node_2.getChildCount());
				}
			}
			{
				DefaultMutableTreeNode node_1 = (DefaultMutableTreeNode) rootNode.getChildAt(1);
				assertEquals("2", node_1.getUserObject());
				assertEquals(1, node_1.getChildCount());
				{
					DefaultMutableTreeNode node_2 = (DefaultMutableTreeNode) node_1.getChildAt(0);
					assertEquals("21", node_2.getUserObject());
					assertEquals(0, node_2.getChildCount());
				}
			}
		}
		// check "model" property
		{
			Property modelProperty = treeInfo.getPropertyByTitle("model");
			PropertyEditor modelEditor = modelProperty.getEditor();
			// text
			{
				String text =
						(String) ReflectionUtils.invokeMethod2(
								modelEditor,
								"getText",
								Property.class,
								modelProperty);
				assertEquals("(root), +1, ++11, +2, ++21", text);
			}
			// tooltip
			{
				String tooltip = getPropertyTooltipText(modelEditor, modelProperty);
				assertEquals(StringUtils.join(new String[]{
						"(root)",
						"    1",
						"        11",
						"    2",
				"        21"}, "\n"), tooltip);
				// position
				PropertyTooltipProvider provider = modelEditor.getAdapter(PropertyTooltipProvider.class);
				assertSame(PropertyTooltipProvider.BELOW, provider.getTooltipPosition());
			}
		}
	}

	/**
	 * Test when there are no model.
	 */
	@Test
	public void test_emptyModel() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"import javax.swing.tree.*;",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JTree tree = new JTree();",
						"    add(tree);",
						"    tree.setModel(null);",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo treeInfo = panel.getChildrenComponents().get(0);
		// check "model" property
		{
			Property modelProperty = treeInfo.getPropertyByTitle("model");
			PropertyEditor modelEditor = modelProperty.getEditor();
			// text
			{
				String text =
						(String) ReflectionUtils.invokeMethod2(
								modelEditor,
								"getText",
								Property.class,
								modelProperty);
				assertNull(text);
			}
			// tooltip
			{
				String tooltip = getPropertyTooltipText(modelEditor, modelProperty);
				assertNull(tooltip);
			}
		}
	}

	/**
	 * Test for {@link TreeModelPropertyEditor}, setting new items.
	 */
	@Test
	public void test_setItems() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"import javax.swing.tree.*;",
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JTree tree = new JTree();",
						"    add(tree);",
						"  }",
						"}");
		panel.refresh();
		//
		ComponentInfo treeInfo = panel.getChildrenComponents().get(0);
		Property modelProperty = treeInfo.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		// prepare items
		Object rootItem;
		{
			rootItem = createItemInformation(0, "(root)");
			Object item_1 = addItemInformation(rootItem, "aaa");
			addItemInformation(item_1, "1");
			addItemInformation(item_1, "2");
			Object item_2 = addItemInformation(rootItem, "bbb");
			addItemInformation(item_2, "3");
		}
		// set items
		ReflectionUtils.invokeMethod(
				modelEditor,
				"setItems(org.eclipse.wb.internal.core.model.property.Property,int,org.eclipse.wb.internal.swing.model.property.editor.models.tree.TreeModelDialog.ItemInformation)",
				modelProperty,
				3,
				rootItem);
		assertEditor(
				"import javax.swing.tree.*;",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JTree tree = new JTree();",
				"    tree.setModel(new DefaultTreeModel(",
				"      new DefaultMutableTreeNode('(root)') {",
				"        {",
				"          DefaultMutableTreeNode node_1;",
				"          node_1 = new DefaultMutableTreeNode('aaa');",
				"            node_1.add(new DefaultMutableTreeNode('1'));",
				"            node_1.add(new DefaultMutableTreeNode('2'));",
				"          add(node_1);",
				"          node_1 = new DefaultMutableTreeNode('bbb');",
				"            node_1.add(new DefaultMutableTreeNode('3'));",
				"          add(node_1);",
				"        }",
				"      }",
				"    ));",
				"    add(tree);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return new <code>ItemInformation</code> instance.
	 */
	private static Object createItemInformation(int level, String text) throws Exception {
		Class<?> itemClass =
				Class.forName("org.eclipse.wb.internal.swing.model.property.editor.models.tree.TreeModelDialog$ItemInformation");
		return ReflectionUtils.getConstructor(itemClass, int.class, String.class).newInstance(
				level,
				text);
	}

	/**
	 * Adds new <code>ItemInformation</code> to given parent.
	 *
	 * @return added <code>ItemInformation</code> instance.
	 */
	public static Object addItemInformation(Object parentItem, String text) throws Exception {
		int parentLevel = (Integer) ReflectionUtils.invokeMethod2(parentItem, "getLevel");
		Object childItem = createItemInformation(parentLevel + 1, text);
		Object parentChildren = ReflectionUtils.invokeMethod2(parentItem, "getChildren");
		ReflectionUtils.invokeMethod2(parentChildren, "add", Object.class, childItem);
		return childItem;
	}
}
