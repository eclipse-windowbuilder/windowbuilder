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
package org.eclipse.wb.internal.core.nls.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSourceListener;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.PixelConverter;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils.ITableTooltipProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link Composite}e for editing single {@link IEditableSource}.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class SourceComposite extends Composite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IEditableSource m_source;
  private LocaleInfo[] m_locales;
  private final List<String> m_keys = Lists.newArrayList();
  ////////////////////////////////////////////////////////////////////////////
  //
  // UI objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PixelConverter m_pixelConverter;
  private final TableViewer m_viewer;
  private final Table m_table;
  private final Button m_currentStringsButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SourceComposite(Composite parent, int style, IEditableSource source) throws Exception {
    super(parent, style);
    m_pixelConverter = new PixelConverter(this);
    // prepare model
    {
      // subscribe on events for this source
      m_source = source;
      m_source.addListener(new IEditableSourceListener() {
        public void keyAdded(String key, Object o) {
          m_keys.add(key);
          m_viewer.add(key);
        }

        public void keyRemoved(String key) {
          m_keys.remove(key);
          m_viewer.remove(key);
        }

        public void keyRenamed(String oldKey, String newKey) {
          if (m_keys.contains(newKey)) {
            m_viewer.remove(oldKey);
          } else {
            int index = m_keys.indexOf(oldKey);
            m_keys.set(index, newKey);
            m_viewer.remove(oldKey);
            m_viewer.insert(newKey, index);
          }
          m_viewer.setSelection(new StructuredSelection(newKey));
        }
      });
      // prepare initial keys
      {
        m_keys.addAll(m_source.getKeys());
        Collections.sort(m_keys);
      }
    }
    // create GUI
    GridLayoutFactory.create(this).columns(2);
    // create "Strings:" label
    {
      Label stringsLabel = new Label(this, SWT.NONE);
      GridDataFactory.create(stringsLabel).spanH(2).fillH();
      stringsLabel.setText(Messages.SourceComposite_stringsLabel);
    }
    // create viewer
    {
      m_viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
      m_viewer.setContentProvider(new StringsContentProvider());
      m_viewer.setLabelProvider(new StringsLabelProvider());
      m_viewer.setCellModifier(new StringsCellModifier());
      //
      m_table = m_viewer.getTable();
      m_table.setHeaderVisible(true);
      m_table.setLinesVisible(true);
      GridDataFactory.create(m_table).spanH(2).grab().fill();
      //
      setViewerMenu();
      setTooltipProvider();
      // create columns
      {
        // create 'key' column
        {
          TableColumn keyColumn = new TableColumn(m_table, SWT.NONE);
          keyColumn.setText(Messages.SourceComposite_keyColumn);
          keyColumn.setWidth(m_pixelConverter.convertWidthInCharsToPixels(60));
        }
        // create locale columns
        createLocaleColumns();
      }
    }
    // create edit hint label
    {
      Label hintLabel = new Label(this, SWT.WRAP);
      GridDataFactory.create(hintLabel).spanH(2);
      hintLabel.setText(Messages.SourceComposite_hint);
    }
    // create "Show strings only for current form" check box
    {
      m_currentStringsButton = new Button(this, SWT.CHECK);
      m_currentStringsButton.setText(Messages.SourceComposite_onlyCurrentFormFlag);
      m_currentStringsButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_viewer.refresh();
        }
      });
    }
    // create "New locale" button
    {
      Button newLocaleButton = new Button(this, SWT.NONE);
      GridDataFactory.create(newLocaleButton).alignHR();
      newLocaleButton.setText(Messages.SourceComposite_newLocaleButton);
      newLocaleButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          addNewLocale();
        }
      });
    }
    // fill viewer
    m_viewer.setInput(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Columns and cell editors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes all existing columns (except first - for key) and create separate column for each
   * locale. Sets columns properties and cell editors.
   */
  private void createLocaleColumns() {
    m_locales = m_source.getLocales();
    LocaleUtils.sortByTitle(m_locales);
    // dispose existing columns
    {
      TableColumn[] columns = m_table.getColumns();
      for (int i = 1; i < columns.length; i++) {
        TableColumn column = columns[i];
        column.dispose();
      }
    }
    // create new columns
    {
      for (int i = 0; i < m_locales.length; i++) {
        LocaleInfo localeInfo = m_locales[i];
        TableColumn column = new TableColumn(m_table, SWT.NONE);
        column.setText(localeInfo.getTitle());
        column.setImage(LocaleUtils.getImage(localeInfo));
        column.setWidth(m_pixelConverter.convertWidthInCharsToPixels(30));
      }
    }
    // configure viewer
    {
      // set column properties
      {
        String columnProperties[] = new String[1 + m_locales.length];
        columnProperties[0] = "key";
        for (int i = 0; i < m_locales.length; i++) {
          columnProperties[1 + i] = Integer.toString(i);
        }
        m_viewer.setColumnProperties(columnProperties);
      }
      // set property editors
      {
        CellEditor columnEditors[] = new CellEditor[1 + m_locales.length];
        for (int i = 0; i < columnEditors.length; i++) {
          TableTextCellEditor editor = new TableTextCellEditor(m_viewer, i);
          columnEditors[i] = editor;
          // support tabbing between columns while editing
          final int column = i;
          editor.getText().addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
              switch (e.detail) {
                case SWT.TRAVERSE_TAB_NEXT :
                  editColumnOrNextPossible(column);
                  e.detail = SWT.TRAVERSE_NONE;
                  break;
                case SWT.TRAVERSE_TAB_PREVIOUS :
                  editColumnOrPrevPossible(column);
                  e.detail = SWT.TRAVERSE_NONE;
                  break;
              }
            }
          });
        }
        m_viewer.setCellEditors(columnEditors);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editors navigation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the key selected in viewer.
   */
  private String getSelectedKey() {
    return (String) ((IStructuredSelection) m_viewer.getSelection()).getFirstElement();
  }

  /**
   * Activates {@link CellEditor} in next column.
   */
  private void editColumnOrNextPossible(int column) {
    String selectedKey = getSelectedKey();
    int nextColumn = getNextColumn(column);
    m_viewer.editElement(selectedKey, nextColumn);
  }

  /**
   * Activates {@link CellEditor} in previous column.
   */
  private void editColumnOrPrevPossible(int column) {
    String selectedKey = getSelectedKey();
    int prevColumn = getPrevColumn(column);
    m_viewer.editElement(selectedKey, prevColumn);
  }

  /**
   * @return the index of next column (or first, if given is the last one).
   */
  private int getNextColumn(int column) {
    return column >= m_table.getColumnCount() - 1 ? 0 : column + 1;
  }

  /**
   * @return the index of previous column (or last, if given is the first one).
   */
  private int getPrevColumn(int column) {
    return column <= 0 ? m_table.getColumnCount() - 1 : column - 1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handlers for actions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds key/value into all locales.
   */
  private void addKeyValue() {
    AddKeyValueDialog newLocaleDialog = new AddKeyValueDialog(getShell());
    if (newLocaleDialog.open() == Window.OK) {
      String key = newLocaleDialog.getKey();
      String value = newLocaleDialog.getValue();
      m_source.addKey(key, value);
      // update UI
      m_viewer.refresh();
    }
  }

  /**
   * Adds new {@link LocaleInfo} using {@link NewLocaleDialog}.
   */
  private void addNewLocale() {
    NewLocaleDialog newLocaleDialog = new NewLocaleDialog(getShell(), m_source.getLocales());
    if (newLocaleDialog.open() == Window.OK) {
      // prepare locales information
      LocaleInfo localeInfo = newLocaleDialog.getSelectedLocale();
      LocaleInfo baseLocaleInfo = newLocaleDialog.getBaseLocale();
      // add locale
      m_source.addLocale(localeInfo, baseLocaleInfo);
      // update UI
      createLocaleColumns();
      m_viewer.refresh();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setViewerMenu() {
    // prepare manager
    MenuManager menuManager = new MenuManager(null);
    menuManager.setRemoveAllWhenShown(true);
    menuManager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager) {
        IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
        // "Internalize key" action
        if (!selection.isEmpty()) {
          final String key = (String) selection.getFirstElement();
          // only keys for current form can be internalized
          if (m_source.getFormKeys().contains(key)) {
            manager.add(new Action(Messages.SourceComposite_internalizeKeyAction) {
              @Override
              public void run() {
                // ask confirmation
                if (!MessageDialog.openConfirm(
                    getShell(),
                    Messages.SourceComposite_internalizeTitle,
                    MessageFormat.format(Messages.SourceComposite_internalizeMessage, key))) {
                  return;
                }
                // do internalize
                ExecutionUtils.runLog(new RunnableEx() {
                  public void run() throws Exception {
                    m_source.internalizeKey(key);
                  }
                });
              }
            });
          }
        }
        // "Add key/value" action
        manager.add(new Action(Messages.SourceComposite_addKeyValueAction) {
          @Override
          public void run() {
            addKeyValue();
          }
        });
        // "Add locale" action
        manager.add(new Action(Messages.SourceComposite_addLocaleAction) {
          @Override
          public void run() {
            addNewLocale();
          }
        });
        // "Remove locale" action
        final int column = UiUtils.getColumnUnderCursor(m_table);
        {
          Action action = new Action(Messages.SourceComposite_removeLocaleAction) {
            @Override
            public void run() {
              final LocaleInfo locale = m_locales[column - 1];
              // ask confirmation
              if (!MessageDialog.openConfirm(
                  getShell(),
                  Messages.SourceComposite_removeLocaleTitle,
                  MessageFormat.format(
                      Messages.SourceComposite_removeLocaleMessage,
                      locale.getTitle()))) {
                return;
              }
              // do remove
              ExecutionUtils.runLog(new RunnableEx() {
                public void run() throws Exception {
                  m_source.removeLocale(locale);
                  createLocaleColumns();
                  m_viewer.refresh();
                }
              });
            }
          };
          action.setEnabled(column > 1 && column < m_table.getColumnCount());
          manager.add(action);
        }
      }
    });
    // create menu
    Menu menu = menuManager.createContextMenu(m_table);
    m_table.setMenu(menu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Installs {@link ITableTooltipProvider} for table.
   */
  private void setTooltipProvider() {
    UiUtils.installTableTooltipProvider(m_table, createTooltipProvider());
  }

  /**
   * @return the instance of {@link ITableTooltipProvider}.
   */
  private ITableTooltipProvider createTooltipProvider() {
    return new ITableTooltipProvider() {
      public Control createTooltipControl(TableItem item, Composite parent, int column) {
        if (column == 0) {
          // prepare components
          String key = (String) item.getData();
          Set<JavaInfo> components = m_source.getComponentsByKey(key);
          // if we have components, we can create tooltip
          if (!components.isEmpty()) {
            // prepare container
            Composite composite = new Composite(parent, SWT.NONE);
            setColors(composite);
            composite.setLayout(new FillLayout(SWT.VERTICAL));
            // add label for each component
            for (Iterator<JavaInfo> I = components.iterator(); I.hasNext();) {
              final JavaInfo component = I.next();
              final CLabel label = new CLabel(composite, SWT.NONE);
              setColors(label);
              ExecutionUtils.runLog(new RunnableEx() {
                public void run() throws Exception {
                  IObjectPresentation presentation = component.getPresentation();
                  label.setImage(presentation.getIcon());
                  label.setText(presentation.getText());
                }
              });
            }
            //
            return composite;
          }
        }
        //
        return null;
      }

      private void setColors(Control control) {
        control.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        control.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content provider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Content provider that returns keys in initial order that keeps same even after renaming. We
   * don't install any sorter because it is not convenient for user when rows are moved
   * "under hands".
   */
  private class StringsContentProvider implements IStructuredContentProvider {
    public Object[] getElements(Object inputElement) {
      if (m_currentStringsButton.getSelection()) {
        List<String> elements = Lists.newArrayList();
        Set<String> formKeys = m_source.getFormKeys();
        for (String key : m_keys) {
          if (formKeys.contains(key)) {
            elements.add(key);
          }
        }
        return elements.toArray();
      } else {
        return m_keys.toArray();
      }
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Label provider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ITableLabelProvider} using values from {@link IEditableSource}.
   */
  private class StringsLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object element, int columnIndex) {
      String key = (String) element;
      if (columnIndex == 0) {
        return key;
      } else {
        LocaleInfo locale = m_locales[columnIndex - 1];
        return m_source.getValue(locale, key);
      }
    }

    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        // use icon of component that has externalize properties with this key
        String key = (String) element;
        Set<JavaInfo> components = m_source.getComponentsByKey(key);
        if (!components.isEmpty()) {
          final JavaInfo component = components.iterator().next();
          return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
            public Image runObject() throws Exception {
              return component.getPresentation().getIcon();
            }
          }, null);
        }
      }
      return null;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Modifier
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ICellModifier} for modifying values and renaming keys.
   */
  private class StringsCellModifier implements ICellModifier {
    public boolean canModify(Object element, String property) {
      return true;
    }

    public Object getValue(Object element, String property) {
      String key = (String) element;
      // check for key property
      if (property.equals("key")) {
        return key;
      }
      // get value for key and locale
      LocaleInfo locale = getLocaleForProperty(property);
      String value = m_source.getValue(locale, key);
      // return value
      if (value == null) {
        return "";
      }
      return value;
    }

    public void modify(Object element, String property, Object value) {
      final String stringValue = (String) value;
      // prepare key
      final String key;
      {
        if (element instanceof Item) {
          element = ((Item) element).getData();
        }
        key = (String) element;
      }
      // change key or value
      if (property.equals("key")) {
        // key change
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            m_source.renameKey(key, stringValue);
          }
        });
      } else {
        // value change
        LocaleInfo locale = getLocaleForProperty(property);
        m_source.setValue(locale, key, stringValue);
        m_viewer.update(key, new String[]{property});
      }
    }

    private LocaleInfo getLocaleForProperty(String property) {
      int localeIndex = Integer.parseInt(property);
      return m_locales[localeIndex];
    }
  }
}
