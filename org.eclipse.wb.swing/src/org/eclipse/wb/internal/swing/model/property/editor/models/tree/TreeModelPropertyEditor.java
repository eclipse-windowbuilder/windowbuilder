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
package org.eclipse.wb.internal.swing.model.property.editor.models.tree;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.swing.model.property.editor.models.tree.TreeModelDialog.ItemInformation;

import org.eclipse.jface.window.Window;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * {@link PropertyEditor} for {@link TreeModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TreeModelPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new TreeModelPropertyEditor();

  private TreeModelPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof TreeModel) {
      TreeModel model = (TreeModel) value;
      TreeNode rootNode = (TreeNode) model.getRoot();
      //
      StringBuffer buffer = new StringBuffer();
      getText_appendNodes(buffer, rootNode, "");
      return buffer.toString();
    }
    // no string presentation
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    TreeModelDialog treeModelDialog =
        new TreeModelDialog(DesignerPlugin.getShell(), property.getTitle());
    {
      String text = getTooltip(property);
      text = StringUtils.defaultString(text);
      text = StringUtils.replace(text, "    ", "\t");
      treeModelDialog.setText(text);
    }
    // open dialog
    if (treeModelDialog.open() == Window.OK) {
      int maxLevel = treeModelDialog.getMaxLevel();
      ItemInformation rootItem = treeModelDialog.getResultItems().get(0);
      setItems(property, maxLevel, rootItem);
    }
  }

  private void setItems(Property property, int maxLevel, ItemInformation rootItem) throws Exception {
    if (property instanceof GenericProperty) {
      GenericProperty genericProperty = (GenericProperty) property;
      JavaInfo javaInfo = genericProperty.getJavaInfo();
      // prepare lines
      List<String> lines = Lists.newArrayList();
      // header
      {
        lines.add("new javax.swing.tree.DefaultTreeModel(");
        lines.add("\tnew javax.swing.tree.DefaultMutableTreeNode("
            + StringConverter.INSTANCE.toJavaSource(javaInfo, rootItem.getText())
            + ") {");
        lines.add("\t\t{");
      }
      // items
      String prefix = "\t\t\t";
      {
        // declare variables for nodes;
        for (int i = 0; i < maxLevel - 2; i++) {
          lines.add(prefix + "javax.swing.tree.DefaultMutableTreeNode node_" + (1 + i) + ";");
        }
        // add nodes
        appendSourceItems(javaInfo, maxLevel, lines, rootItem.getChildren());
      }
      // footer
      {
        lines.add("\t\t}");
        lines.add("\t}");
        lines.add(")");
      }
      // set source as single String
      String source = StringUtils.join(lines.iterator(), "\n");
      genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
    }
  }

  private static void appendSourceItems(JavaInfo javaInfo,
      int maxLevel,
      List<String> lines,
      List<ItemInformation> items) throws Exception {
    for (ItemInformation item : items) {
      int level = item.getLevel();
      String text = item.getText();
      String prefix = "\t\t\t" + StringUtils.repeat("\t", level - 1);
      // prepare node source
      String nodeCreation =
          "new javax.swing.tree.DefaultMutableTreeNode("
              + StringConverter.INSTANCE.toJavaSource(javaInfo, text)
              + ")";
      // prepare parent access
      String parentAccess;
      if (level == 1) {
        parentAccess = "";
      } else {
        parentAccess = "node_" + (level - 1) + ".";
      }
      // add node
      if (item.getChildren().isEmpty()) {
        lines.add(prefix + parentAccess + "add(" + nodeCreation + ");");
      } else {
        // assign node
        lines.add(prefix + "node_" + level + " = " + nodeCreation + ";");
        // append children
        appendSourceItems(javaInfo, maxLevel, lines, item.getChildren());
        // add node
        lines.add(prefix + parentAccess + "add(node_" + level + ");");
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected PropertyTooltipProvider createPropertyTooltipProvider() {
    return new PropertyTooltipTextProvider() {
      @Override
      protected String getText(Property property) throws Exception {
        return getTooltip(property);
      }

      @Override
      public int getTooltipPosition() {
        return BELOW;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Appends given {@link TreeNode} and its children to the {@link StringBuffer}.
   */
  private static void getText_appendNodes(StringBuffer buffer, TreeNode node, String prefix) {
    // append separator
    if (buffer.length() != 0) {
      buffer.append(", ");
    }
    // append current node
    buffer.append(prefix);
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
      buffer.append(mutableTreeNode.getUserObject());
    }
    // append children
    String newPrefix = prefix + "+";
    for (int i = 0; i < node.getChildCount(); i++) {
      getText_appendNodes(buffer, node.getChildAt(i), newPrefix);
    }
  }

  /**
   * @return the text to display as tooltip, or <code>null</code> if {@link Property} has invalid
   *         value.
   */
  private String getTooltip(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof TreeModel) {
      TreeModel model = (TreeModel) value;
      TreeNode rootNode = (TreeNode) model.getRoot();
      //
      StringBuffer buffer = new StringBuffer();
      getTooltip_appendNodes(buffer, rootNode, "");
      return buffer.toString();
    }
    // no tooltip
    return null;
  }

  /**
   * Appends given {@link TreeNode} and its children to the {@link StringBuffer}.
   */
  private static void getTooltip_appendNodes(StringBuffer buffer, TreeNode node, String prefix) {
    // append separator
    if (buffer.length() != 0) {
      buffer.append("\n");
    }
    // append current node
    buffer.append(prefix);
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) node;
      buffer.append(mutableTreeNode.getUserObject());
    }
    // append children
    String newPrefix = prefix + "    ";
    for (int i = 0; i < node.getChildCount(); i++) {
      getTooltip_appendNodes(buffer, node.getChildAt(i), newPrefix);
    }
  }
}
