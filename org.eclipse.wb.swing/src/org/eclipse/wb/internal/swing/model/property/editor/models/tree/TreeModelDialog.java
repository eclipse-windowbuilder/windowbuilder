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

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import javax.swing.tree.TreeModel;

/**
 * The dialog for editing {@link TreeModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TreeModelDialog extends ResizableDialog {
  private final String m_titleText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeModelDialog(Shell parentShell, String titleText) {
    super(parentShell, Activator.getDefault());
    m_titleText = titleText;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_text;

  /**
   * Sets the text to edit.
   */
  public final void setText(String text) {
    m_text = text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_textWidget;
  private Tree m_treeWidget;

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(area).columns(2).equalColumns();
    // headers
    {
      new Label(area, SWT.NONE).setText(ModelMessages.TreeModelDialog_itemsLabel);
      new Label(area, SWT.NONE).setText(ModelMessages.TreeModelDialog_previewLabel);
    }
    // Text widget
    {
      m_textWidget = new Text(area, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_textWidget).grab().fill().hintC(40, 20);
      m_textWidget.setText(m_text);
      // listener
      m_textWidget.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          refreshTree();
        }
      });
      m_textWidget.addTraverseListener(new TraverseListener() {
        public void keyTraversed(TraverseEvent e) {
          if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
            e.doit = false;
          }
        }
      });
    }
    // Tree widget
    {
      m_treeWidget = new Tree(area, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_treeWidget).grab().fill().hintC(40, 20);
    }
    // footer
    {
      Label footerLabel = new Label(area, SWT.NONE);
      GridDataFactory.create(footerLabel).spanH(2);
      footerLabel.setText(ModelMessages.TreeModelDialog_hint);
    }
    //
    refreshTree();
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_titleText);
  }

  @Override
  protected void okPressed() {
    {
      m_resultItems = Lists.newArrayList();
      prepareResult(m_resultItems, 0, m_treeWidget.getItems());
    }
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Result
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_maxLevel;
  private List<ItemInformation> m_resultItems;

  /**
   * @return the maximal level of items.
   */
  public int getMaxLevel() {
    return m_maxLevel;
  }

  /**
   * @return the root level {@link ItemInformation}.
   */
  public List<ItemInformation> getResultItems() {
    return m_resultItems;
  }

  /**
   * Appends given {@link TreeItem}'s to the result {@link List}'s.
   */
  private void prepareResult(List<ItemInformation> resultItems, int level, TreeItem[] items) {
    m_maxLevel = Math.max(m_maxLevel, level);
    for (TreeItem treeItem : items) {
      // add current item
      ItemInformation resultItem = new ItemInformation(level, treeItem.getText());
      resultItems.add(resultItem);
      // add sub-items
      prepareResult(resultItem.getChildren(), level + 1, treeItem.getItems());
    }
  }

  /**
   * Information about resulting item.
   * 
   * @author scheglov_ke
   */
  static final class ItemInformation {
    private final int m_level;
    private final String m_text;
    private final List<ItemInformation> m_children = Lists.newArrayList();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ItemInformation(int level, String text) {
      m_level = level;
      m_text = text;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public int getLevel() {
      return m_level;
    }

    public String getText() {
      return m_text;
    }

    public List<ItemInformation> getChildren() {
      return m_children;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image IMAGE_FOLDER = new Image(null,
      TreeModelDialog.class.getResourceAsStream("icons/folder.gif"));
  private static final Image IMAGE_LEAF = new Image(null,
      TreeModelDialog.class.getResourceAsStream("icons/leaf.gif"));

  /**
   * Refreshes {@link #m_treeWidget} with current text.
   */
  private void refreshTree() {
    m_treeWidget.setRedraw(false);
    try {
      m_treeWidget.removeAll();
      TreeItem currentItem = null;
      int currentLevel = -1;
      // process lines
      String[] lines = StringUtils.split(m_textWidget.getText(), "\r\n");
      for (String line : lines) {
        ItemInformationImpl information = createItem(currentItem, currentLevel, line);
        currentItem = information.m_item;
        currentLevel = information.m_level;
      }
      // expand all
      expandItems(m_treeWidget.getItems());
    } finally {
      m_treeWidget.setRedraw(true);
    }
  }

  /**
   * @return the newly create {@link TreeItem} with given text/indentation.
   */
  private ItemInformationImpl createItem(TreeItem parent, int parentLevel, String line) {
    int level = StringUtils.indexOfAnyBut(line, "\t");
    if (level == -1) {
      level = line.length();
    }
    // update parent
    if (parent == null) {
      level = 0;
    } else {
      // create empty items
      for (int i = parentLevel; i < level - 1; i++) {
        parent = new TreeItem(parent, SWT.NONE);
        parent.setText("(empty)");
        parent.setImage(IMAGE_FOLDER);
      }
      // go up, if needed
      for (int i = level - 1; i < parentLevel; i++) {
        parent = parent.getParentItem();
      }
    }
    // create new item
    TreeItem item;
    if (parent == null) {
      item = new TreeItem(m_treeWidget, SWT.NONE);
    } else {
      parent.setImage(IMAGE_FOLDER);
      item = new TreeItem(parent, SWT.NONE);
    }
    item.setText(line.substring(level));
    item.setImage(IMAGE_LEAF);
    return new ItemInformationImpl(item, level);
  }

  /**
   * Expands given {@link TreeItem}'s and their sub-items.
   */
  private static void expandItems(TreeItem[] items) {
    for (TreeItem treeItem : items) {
      treeItem.setExpanded(true);
      expandItems(treeItem.getItems());
    }
  }

  /**
   * Information about {@link TreeItem}.
   * 
   * @author scheglov_ke
   */
  private static final class ItemInformationImpl {
    final TreeItem m_item;
    final int m_level;

    public ItemInformationImpl(TreeItem item, int level) {
      m_item = item;
      m_level = level;
    }
  }
}
