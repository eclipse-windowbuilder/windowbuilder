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
package org.eclipse.wb.internal.core.model.property.editor.string;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.nls.ui.LocaleUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;

/**
 * {@link Dialog} for editing key in some NLS source for {@link StringPropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
final class StringPropertyKeyDialog extends ResizableTitleAreaDialog {
  private final IEditableSupport m_editableSupport;
  private final LocaleInfo m_locale;
  private IEditableSource m_selectedSource;
  private String m_selectedKey;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringPropertyKeyDialog(Shell parentShell,
      IEditableSupport editableSupport,
      IEditableSource source,
      String key,
      LocaleInfo locale) {
    super(parentShell, DesignerPlugin.getDefault());
    m_editableSupport = editableSupport;
    m_selectedSource = source;
    m_selectedKey = key;
    m_locale = locale;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link IEditableSource}.
   */
  public IEditableSource getSelectedSource() {
    return m_selectedSource;
  }

  /**
   * @return the selected key.
   */
  public String getSelectedKey() {
    return m_selectedKey;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private ListViewer m_sourcesViewer;
  private TableViewer m_valuesViewer;

  @Override
  public void create() {
    super.create();
    // title/message
    {
      setTitle(ModelMessages.StringPropertyKeyDialog_title);
      setMessage(ModelMessages.StringPropertyKeyDialog_message);
    }
    // select key
    {
      Button okButton = getButton(IDialogConstants.OK_ID);
      if (m_selectedKey != null) {
        m_valuesViewer.setSelection(new StructuredSelection(m_selectedKey));
        okButton.setEnabled(true);
      } else {
        okButton.setEnabled(false);
      }
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    // prepare container for all dialog controls
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    GridLayoutFactory.create(container);
    // groups
    createSourcesGroup(container);
    createValuesGroup(container);
    // select source
    {
      if (m_selectedSource == null) {
        m_selectedSource = (IEditableSource) m_sourcesViewer.getElementAt(0);
      }
      m_sourcesViewer.setSelection(new StructuredSelection(m_selectedSource));
    }
    //
    return area;
  }

  private void createSourcesGroup(Composite parent) {
    Group sourcesGroup = new Group(parent, SWT.NONE);
    GridDataFactory.create(sourcesGroup).hintC(105, 5).grabH().fill();
    GridLayoutFactory.create(sourcesGroup);
    sourcesGroup.setText(ModelMessages.StringPropertyKeyDialog_sourcesGroup);
    //
    m_sourcesViewer = new ListViewer(sourcesGroup, SWT.BORDER);
    GridDataFactory.create(m_sourcesViewer.getList()).grab().fill();
    // content
    m_sourcesViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        IEditableSource source = (IEditableSource) element;
        return source.getLongTitle();
      }
    });
    m_sourcesViewer.setContentProvider(new ArrayContentProvider());
    m_sourcesViewer.setSorter(new ViewerSorter());
    m_sourcesViewer.setInput(m_editableSupport.getEditableSources());
    // selection listener
    m_sourcesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          m_selectedSource = (IEditableSource) selection.getFirstElement();
          m_valuesViewer.setInput(m_selectedSource.getKeys());
        } else {
          m_valuesViewer.setInput(null);
        }
        updateOkButton();
      }
    });
  }

  private void createValuesGroup(Composite parent) {
    Group valuesGroup = new Group(parent, SWT.NONE);
    GridDataFactory.create(valuesGroup).hintC(105, 20).grab().fill();
    GridLayoutFactory.create(valuesGroup);
    valuesGroup.setText(ModelMessages.StringPropertyKeyDialog_valuesGroup);
    // pattern
    {
      new Label(valuesGroup, SWT.NONE).setText(ModelMessages.StringPropertyKeyDialog_valuesFilterLabel);
      //
      m_filterPatternText = new Text(valuesGroup, SWT.BORDER);
      GridDataFactory.create(m_filterPatternText).grabH().fill();
      m_filterPatternText.addListener(SWT.Modify, new Listener() {
        public void handleEvent(Event event) {
          refreshValuesViewer();
        }
      });
    }
    // viewer
    {
      new Label(valuesGroup, SWT.NONE).setText(ModelMessages.StringPropertyKeyDialog_matchedGroup);
      //
      m_valuesViewer = new TableViewer(valuesGroup, SWT.BORDER | SWT.FULL_SELECTION);
      GridDataFactory.create(m_valuesViewer.getTable()).grab().fill();
      // columns
      {
        TableFactory tableFactory = TableFactory.modify(m_valuesViewer).standard();
        tableFactory.newColumn().widthC(40).text(ModelMessages.StringPropertyKeyDialog_keyColumn);
        tableFactory.newColumn().widthC(57).image(LocaleUtils.getImage(m_locale)).text(
            ModelMessages.StringPropertyKeyDialog_valueColumn);
      }
      // content
      setValuesFilter();
      m_valuesViewer.setContentProvider(new ArrayContentProvider());
      m_valuesViewer.setLabelProvider(new ValuesLabelProvider());
      m_valuesViewer.setSorter(new ViewerSorter());
      // listeners
      m_valuesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          IStructuredSelection selection = (IStructuredSelection) event.getSelection();
          m_selectedKey = (String) selection.getFirstElement();
          getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
      });
      m_valuesViewer.addDoubleClickListener(new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
          okPressed();
        }
      });
    }
    // pattern options
    {
      Composite optionsComposite = new Composite(valuesGroup, SWT.NONE);
      GridDataFactory.create(optionsComposite).fill();
      GridLayoutFactory.create(optionsComposite).noMargins().columns(4);
      //
      new Label(optionsComposite, SWT.NONE).setText(ModelMessages.StringPropertyKeyDialog_filterLabel);
      // create buttons
      m_filterKeyButton =
          createFilterOptionButton(
              optionsComposite,
              ModelMessages.StringPropertyKeyDialog_filterKeyButton);
      m_filterValueButton =
          createFilterOptionButton(
              optionsComposite,
              ModelMessages.StringPropertyKeyDialog_filterValueButton);
      m_filterBothButton =
          createFilterOptionButton(
              optionsComposite,
              ModelMessages.StringPropertyKeyDialog_filterBothButton);
      // by default select "key"
      if (!m_filterKeyButton.getSelection()
          && !m_filterValueButton.getSelection()
          && !m_filterBothButton.getSelection()) {
        m_filterKeyButton.setSelection(true);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  private SearchPattern m_searchPattern;
  private Text m_filterPatternText;
  private Button m_filterKeyButton;
  private Button m_filterValueButton;
  private Button m_filterBothButton;

  /**
   * Creates {@link SWT#RADIO} {@link Button} that refreshes {@link #m_valuesViewer}.
   */
  private Button createFilterOptionButton(Composite parent, final String text) {
    final Button button = new Button(parent, SWT.RADIO);
    button.setText(text);
    button.setSelection(getDialogSettings().getBoolean(text));
    button.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        getDialogSettings().put(text, button.getSelection());
        refreshValuesViewer();
      }
    });
    return button;
  }

  /**
   * Sets the {@link ViewerFilter} that uses {@link #m_searchPattern}.
   */
  private void setValuesFilter() {
    m_valuesViewer.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (m_searchPattern == null) {
          return true;
        } else {
          String key = (String) element;
          String value = m_selectedSource.getValue(m_locale, key);
          if (m_filterKeyButton.getSelection()) {
            return m_searchPattern.matches(key);
          }
          if (m_filterValueButton.getSelection()) {
            return m_searchPattern.matches(value);
          }
          if (m_filterBothButton.getSelection()) {
            return m_searchPattern.matches(key) || m_searchPattern.matches(value);
          }
          return false;
        }
      }
    });
  }

  /**
   * Refreshes {@link #m_valuesViewer}.
   */
  private void refreshValuesViewer() {
    // create search pattern
    {
      m_searchPattern = new SearchPattern();
      m_searchPattern.setPattern("*" + m_filterPatternText.getText());
    }
    // do refresh
    m_valuesViewer.refresh();
    updateOkButton();
  }

  private void updateOkButton() {
    ISelection keySelection = m_valuesViewer.getSelection();
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {
      okButton.setEnabled(!keySelection.isEmpty());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.StringPropertyKeyDialog_shellTitle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values providers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ITableLabelProvider} for {@link #m_valuesViewer}.
   */
  private class ValuesLabelProvider extends LabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    public String getColumnText(Object element, int columnIndex) {
      String key = (String) element;
      if (columnIndex == 0) {
        return key;
      }
      return m_selectedSource.getValue(m_locale, key);
    }
  }
}
