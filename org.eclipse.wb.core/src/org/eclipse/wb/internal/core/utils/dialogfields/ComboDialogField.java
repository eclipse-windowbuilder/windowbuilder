/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Dialog field containing a label and a combo control.
 */
public class ComboDialogField extends DialogField {
  private String fText;
  private int fVisibleItemCount;
  private int fSelectionIndex;
  private List/*<String>*/fItems = new ArrayList();
  private Combo fComboControl;
  private ModifyListener fModifyListener;
  private final int fFlags;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboDialogField(int flags) {
    super();
    fText = ""; //$NON-NLS-1$
    fFlags = flags;
    fSelectionIndex = -1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Control[] doFillIntoGrid(Composite parent, int nColumns) {
    assertEnoughColumns(nColumns);
    //
    Label label = getLabelControl(parent);
    label.setLayoutData(gridDataForLabel(1));
    Combo combo = getComboControl(parent);
    combo.setLayoutData(gridDataForCombo(nColumns - 1));
    //
    return new Control[]{label, combo};
  }

  @Override
  public int getNumberOfControls() {
    return 2;
  }

  protected static GridData gridDataForCombo(int span) {
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalSpan = span;
    return gd;
  }

  // ------- focus methods
  ////////////////////////////////////////////////////////////////////////////
  //
  // Focus methods
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFocus() {
    if (isOkToUse(fComboControl)) {
      fComboControl.setFocus();
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates or returns the created combo control.
   *
   * @param parent
   *          The parent composite or <code>null</code> when the widget has already been created.
   */
  public Combo getComboControl(Composite parent) {
    if (fComboControl == null) {
      assertCompositeNotNull(parent);
      fModifyListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          doModifyText(e);
        }
      };
      SelectionListener selectionListener = new SelectionListener() {
        public void widgetSelected(SelectionEvent e) {
          doSelectionChanged(e);
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }
      };
      fComboControl = new Combo(parent, fFlags);
      doSetItems();
      //
      if (fSelectionIndex != -1) {
        fComboControl.select(fSelectionIndex);
      } else {
        fComboControl.setText(fText);
      }
      if (fVisibleItemCount != 0) {
        fComboControl.setVisibleItemCount(fVisibleItemCount);
      }
      fComboControl.setFont(parent.getFont());
      fComboControl.addModifyListener(fModifyListener);
      fComboControl.addSelectionListener(selectionListener);
      fComboControl.setEnabled(isEnabled());
    }
    return fComboControl;
  }

  private void doModifyText(ModifyEvent e) {
    if (isOkToUse(fComboControl)) {
      fText = fComboControl.getText();
      fSelectionIndex = fComboControl.getSelectionIndex();
    }
    dialogFieldChanged();
  }

  private void doSelectionChanged(SelectionEvent e) {
    if (isOkToUse(fComboControl)) {
      fItems = new ArrayList();
      CollectionUtils.addAll(fItems, fComboControl.getItems());
      //
      fText = fComboControl.getText();
      fSelectionIndex = fComboControl.getSelectionIndex();
    }
    dialogFieldChanged();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enable / disable management
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateEnableState() {
    super.updateEnableState();
    if (isOkToUse(fComboControl)) {
      fComboControl.setEnabled(isEnabled());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Text access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Gets the count of combo items.
   */
  public int getItemCount() {
    return fItems.size();
  }

  /**
   * Gets the combo items.
   */
  public List getListItems() {
    return fItems;
  }

  /**
   * Gets the combo items.
   */
  public String[] getItems() {
    return (String[]) fItems.toArray(new String[fItems.size()]);
  }

  /**
   * Add the combo item.
   */
  public void addItem(String newItem) {
    fItems.add(newItem);
    if (isOkToUse(fComboControl)) {
      fComboControl.add(newItem);
    }
    dialogFieldChanged();
  }

  /**
   * Remove the combo item.
   */
  public void removeItem(String newItem) {
    boolean remove = fItems.remove(newItem);
    if (remove && isOkToUse(fComboControl)) {
      fComboControl.remove(newItem);
    }
  }

  /**
   * Add the combo items.
   */
  public void addItems(Collection newItems) {
    for (Iterator I = newItems.iterator(); I.hasNext();) {
      String item = (String) I.next();
      addItem(item);
    }
  }

  private void doSetItems() {
    fComboControl.removeAll();
    for (Iterator I = fItems.iterator(); I.hasNext();) {
      String item = (String) I.next();
      fComboControl.add(item);
    }
  }

  /**
   * Sets the combo items. Triggers a dialog-changed event.
   */
  public void setItems(String[] items) {
    fItems = new ArrayList();
    CollectionUtils.addAll(fItems, items);
    //
    if (isOkToUse(fComboControl)) {
      doSetItems();
      fComboControl.setItems(items);
    }
    dialogFieldChanged();
  }

  /**
   * Gets the text.
   */
  public String getText() {
    return fText;
  }

  /**
   * Sets the text. Triggers a dialog-changed event.
   */
  public void setText(String text) {
    fText = text;
    if (isOkToUse(fComboControl)) {
      fComboControl.setText(text);
    } else {
      dialogFieldChanged();
    }
  }

  /**
   * Sets the count of visible items
   */
  public void setVisibleItemCount(int visibleItemCount) {
    fVisibleItemCount = visibleItemCount;
    if (isOkToUse(fComboControl)) {
      fComboControl.setVisibleItemCount(fVisibleItemCount);
    } else {
      dialogFieldChanged();
    }
  }

  /**
   * Selects an item.
   */
  public boolean selectItem(int index) {
    boolean success = false;
    if (isOkToUse(fComboControl)) {
      fComboControl.select(index);
      fText = fComboControl.getText();
      fSelectionIndex = fComboControl.getSelectionIndex();
      success = fSelectionIndex == index;
    } else {
      if (index >= 0 && index < fItems.size()) {
        fText = (String) fItems.get(index);
        fSelectionIndex = index;
        success = true;
      }
    }
    if (success) {
      dialogFieldChanged();
    }
    return success;
  }

  /**
   * Selects an item.
   */
  public boolean selectItem(String name) {
    for (int i = 0; i < fItems.size(); i++) {
      if (name.equals(fItems.get(i))) {
        return selectItem(i);
      }
    }
    return false;
  }

  public int getSelectionIndex() {
    return fSelectionIndex;
  }

  /**
   * Sets the text without triggering a dialog-changed event.
   */
  public void setTextWithoutUpdate(String text) {
    fText = text;
    if (isOkToUse(fComboControl)) {
      fComboControl.removeModifyListener(fModifyListener);
      fComboControl.setText(text);
      fComboControl.addModifyListener(fModifyListener);
    }
  }

  @Override
  public void refresh() {
    super.refresh();
    setTextWithoutUpdate(fText);
  }
}
