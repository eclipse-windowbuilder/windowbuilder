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
package org.eclipse.wb.internal.swing.model.property.editor.models.combo;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import javax.swing.ComboBoxModel;

/**
 * The dialog for editing {@link ComboBoxModel}.
 * 
 * @author sablin_aa
 * @coverage swing.property.editor
 */
public class ComboBoxModelDialog extends ResizableDialog {
  final IJavaProject javaProject;
  final String title;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComboBoxModelDialog(Shell parentShell,
      AbstractUIPlugin plugin,
      IJavaProject javaProject,
      String title) {
    super(parentShell, plugin);
    this.javaProject = javaProject;
    this.title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int COLUMNS = 3;
  private Button enumRadio;
  private Text enumText;
  private Button enumButton;
  private Button itemsRadio;
  private Text itemsText;

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(area).columns(COLUMNS);
    // enum selection
    {
      enumRadio = new Button(area, SWT.RADIO);
      GridDataFactory.create(enumRadio).spanH(COLUMNS);
      enumRadio.setText(ModelMessages.ComboBoxModelDialog_enumRadio);
      new Label(area, SWT.NONE).setText("   "); // filler
      enumText = new Text(area, SWT.BORDER);
      GridDataFactory.create(enumText).grabH().fillH().hintHC(60);
      // handle Ctrl+Enter as OK
      enumText.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          setEnumSelected(true);
          if (e.stateMask == SWT.CTRL && e.keyCode == SWT.CR) {
            okPressed();
          }
        }
      });
      enumButton = new Button(area, SWT.NONE);
      enumButton.setText(ModelMessages.ComboBoxModelDialog_enumBrowse);
      enumButton.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent event) {
          setEnumSelected(true);
          selectEnumType();
        }

        public void widgetDefaultSelected(SelectionEvent e) {
        }
      });
    }
    // direct text items
    {
      itemsRadio = new Button(area, SWT.RADIO);
      itemsRadio.setText(ModelMessages.ComboBoxModelDialog_itemsRadio);
      GridDataFactory.create(itemsRadio).spanH(COLUMNS);
      new Label(area, SWT.NONE).setText("   "); // filler
      itemsText = new Text(area, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(itemsText).spanH(COLUMNS - 1).grab().fill().hintVC(10);
      // handle Ctrl+Enter as OK
      itemsText.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          setEnumSelected(false);
          if (e.stateMask == SWT.CTRL && e.keyCode == SWT.CR) {
            okPressed();
          }
        }
      });
    }
    // footer
    Label footerLabel = new Label(area, SWT.NONE);
    GridDataFactory.create(footerLabel).spanH(COLUMNS).alignH(SWT.RIGHT);
    footerLabel.setText(ModelMessages.ComboBoxModelDialog_footerLabel);
    // initialize
    if (StringUtils.isEmpty(enumTypeName)) {
      setEnumTypeName("");
      setItems(StringUtils.isEmpty(stringItems) ? "" : stringItems);
    } else {
      setItems("");
      setEnumTypeName(enumTypeName);
    }
    //
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }

  @Override
  protected void okPressed() {
    isEnumSelected = enumRadio.getSelection();
    enumTypeName = enumText.getText();
    stringItems = itemsText.getText();
    super.okPressed();
  }

  private void selectEnumType() {
    try {
      Shell shell = DesignerPlugin.getShell();
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{javaProject});
      SelectionDialog dialog =
          JavaUI.createTypeDialog(
              shell,
              new ProgressMonitorDialog(shell),
              scope,
              IJavaElementSearchConstants.CONSIDER_ENUMS,
              false,
              enumText.getText());
      dialog.setTitle(ModelMessages.ComboBoxModelDialog_typeDialogTitle);
      dialog.setMessage(ModelMessages.ComboBoxModelDialog_typeDialogMessage);
      // open dialog
      if (dialog.open() == Window.OK) {
        IType selectedEnumType = (IType) dialog.getResult()[0];
        setEnumTypeName(selectedEnumType.getFullyQualifiedName());
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isEnumSelected;
  private String enumTypeName;
  private String stringItems;

  /**
   * Switch the configuration mode.
   */
  private void setEnumSelected(boolean selected) {
    isEnumSelected = selected;
    if (enumRadio != null && !enumRadio.isDisposed()) {
      enumRadio.setSelection(selected);
    }
    if (itemsRadio != null && !itemsRadio.isDisposed()) {
      itemsRadio.setSelection(!selected);
    }
  }

  /**
   * @return <code>true</code> if configuration mode is enum-selection.
   */
  public boolean isEnumSelected() {
    return isEnumSelected;
  }

  /**
   * Sets the enum type name to edit.
   */
  public void setEnumTypeName(String enumTypeName) {
    this.enumTypeName = enumTypeName.replace('$', '.');;
    if (enumText != null && !enumText.isDisposed()) {
      enumText.setText(this.enumTypeName);
    }
    setEnumSelected(true);
  }

  /**
   * @return the enum type name.
   */
  public String getEnumTypeName() {
    if (enumText != null && !enumText.isDisposed()) {
      return enumText.getText();
    } else {
      return enumTypeName;
    }
  }

  /**
   * Sets the items to edit.
   */
  public void setItems(String stringItems) {
    this.stringItems = stringItems;
    if (itemsText != null && !itemsText.isDisposed()) {
      itemsText.setText(this.stringItems);
    }
    setEnumSelected(false);
  }

  public void setItems(String[] items) {
    setItems(StringUtils.join(items, "\n"));
  }

  /**
   * @return the edited items.
   */
  public String[] getItems() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<String[]>() {
      public String[] runObject() throws Exception {
        List<String> strings = Lists.newArrayList();
        String stringItems =
            itemsText != null && !itemsText.isDisposed()
                ? itemsText.getText()
                : ComboBoxModelDialog.this.stringItems;
        BufferedReader br = new BufferedReader(new StringReader(stringItems));
        while (true) {
          String s = br.readLine();
          if (s == null) {
            break;
          }
          strings.add(s);
        }
        return strings.toArray(new String[strings.size()]);
      }
    }, ArrayUtils.EMPTY_STRING_ARRAY);
  }
}
