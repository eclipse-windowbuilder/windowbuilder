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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;
import org.eclipse.wb.internal.swing.MigLayout.Activator;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dialog for editing {@link List} of {@link MigDimensionInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
abstract class DimensionsDialog<T extends MigDimensionInfo> extends ResizableTitleAreaDialog {
  protected final MigLayoutInfo m_layout;
  private final List<T> m_dimensions2;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionsDialog(Shell parentShell, MigLayoutInfo layout, List<T> dimensions) {
    super(parentShell, Activator.getDefault());
    setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
    //
    m_layout = layout;
    m_dimensions2 = dimensions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    GridLayoutFactory.create(container).columns(2);
    // title
    {
      Label label = new Label(container, SWT.NONE);
      GridDataFactory.create(label).spanH(2);
      label.setText(getViewerTitle());
    }
    // create viewer
    createViewer(container);
    // buttons
    {
      createButtonsComposite(container);
      updateButtons();
    }
    // configure title area
    setTitle(getDialogTitle());
    setMessage(getDialogMessage());
    return area;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(getDialogTitle());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  private TableViewer m_viewer;

  /**
   * Creates {@link TableViewer} for {@link MigDimensionInfo}'s.
   */
  private void createViewer(Composite container) {
    m_viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    // configure table
    Table table = m_viewer.getTable();
    GridDataFactory.create(table).hintC(60, 15).grab().fill();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    createColumn("#", 5);
    createColumn("Specification", 65);
    // configure viewer
    m_viewer.setContentProvider(new ArrayContentProvider());
    m_viewer.setLabelProvider(new DimensionsLabelProvider());
    m_viewer.setInput(m_dimensions2);
    // add listeners
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        editSelectedDimension();
      }
    });
    // select first
    if (!m_dimensions2.isEmpty()) {
      m_viewer.getTable().setSelection(0);
    }
  }

  /**
   * Creates {@link TableColumn} with given parameters.
   */
  private void createColumn(String text, int widthInChars) {
    TableColumn column = new TableColumn(m_viewer.getTable(), SWT.NONE);
    column.setText(text);
    column.setWidth(convertWidthInCharsToPixels(widthInChars));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private Button m_editButton;
  private Button m_removeButton;
  private Button m_moveUpButton;
  private Button m_moveDownButton;

  /**
   * Creates {@link Composite} with {@link Button}'s.
   */
  private void createButtonsComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(composite).fill();
    GridLayoutFactory.create(composite).marginsV(0);
    //
    createButton(composite, "&Insert...", new Listener() {
      public void handleEvent(Event event) {
        addNewDimension(0);
      }
    });
    createButton(composite, "&Append...", new Listener() {
      public void handleEvent(Event event) {
        addNewDimension(1);
      }
    });
    m_editButton = createButton(composite, "&Edit...", new Listener() {
      public void handleEvent(Event event) {
        editSelectedDimension();
      }
    });
    m_removeButton = createButton(composite, "&Remove", new Listener() {
      public void handleEvent(Event event) {
        final AtomicInteger lastIndex = new AtomicInteger();
        applyChanges(new RunnableEx() {
          public void run() throws Exception {
            Iterable<T> selectedDimensions = GenericsUtils.<T>iterable(m_viewer.getSelection());
            for (T dimension : selectedDimensions) {
              lastIndex.set(dimension.getIndex());
              dimension.delete();
            }
          }
        });
        // set selection
        int index = lastIndex.get();
        index = Math.min(index, m_dimensions2.size() - 1);
        m_viewer.getTable().select(index);
        // validate
        updateButtons();
      }
    });
    //
    new Label(composite, SWT.NONE);
    m_moveUpButton = createButton(composite, "Move &Up", new Listener() {
      public void handleEvent(Event event) {
        applyChanges(new RunnableEx() {
          public void run() throws Exception {
            Iterable<T> selectedDimensions = GenericsUtils.<T>iterable(m_viewer.getSelection());
            moveDimensionsUp(selectedDimensions);
          }
        });
        updateButtons();
      }
    });
    m_moveDownButton = createButton(composite, "Move &Down", new Listener() {
      public void handleEvent(Event event) {
        applyChanges(new RunnableEx() {
          public void run() throws Exception {
            Iterable<T> selectedDimensions = GenericsUtils.<T>iterable(m_viewer.getSelection());
            moveDimensionsDown(selectedDimensions);
          }
        });
        updateButtons();
      }
    });
  }

  /**
   * @return the new {@link Button} with given text and {@link SWT#Selection} {@link Listener}.
   */
  private static Button createButton(Composite parent, String text, Listener listener) {
    Button button = new Button(parent, SWT.NONE);
    GridDataFactory.create(button).grabH().fillH();
    button.setText(text);
    button.addListener(SWT.Selection, listener);
    return button;
  }

  /**
   * Updates buttons according to the selection in viewer.
   */
  private void updateButtons() {
    IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
    boolean empty = selection.isEmpty();
    //
    m_editButton.setEnabled(selection.size() == 1);
    m_removeButton.setEnabled(!empty);
    // up/down buttons
    {
      m_moveUpButton.setEnabled(!empty);
      m_moveDownButton.setEnabled(!empty);
      for (T dimension : GenericsUtils.<T>iterable(selection)) {
        int index = dimension.getIndex();
        if (index == 0) {
          m_moveUpButton.setEnabled(false);
        }
        if (index == m_dimensions2.size() - 1) {
          m_moveDownButton.setEnabled(false);
        }
      }
    }
  }

  /**
   * Adds new {@link MigDimensionInfo}.
   * 
   * @param indexOffset
   *          the offset to add to the current selection index, <code>0</code> to implement insert
   *          and <code>1</code> for append.
   */
  private void addNewDimension(int indexOffset) {
    // add new dimension
    final int index = addNewDimension_getIndex(indexOffset);
    applyChanges(new RunnableEx() {
      public void run() throws Exception {
        createNewDimension(index);
      }
    });
    m_viewer.getTable().setSelection(index);
    // edit it
    editSelectedDimension();
  }

  private int addNewDimension_getIndex(int indexOffset) {
    int index = m_viewer.getTable().getSelectionIndex();
    if (index == -1) {
      return m_dimensions2.size();
    } else {
      return index + indexOffset;
    }
  }

  /**
   * Edits the selected {@link MigDimensionInfo}.
   */
  private void editSelectedDimension() {
    T dimension = GenericsUtils.<T>first(m_viewer.getSelection());
    editSelectedDimension(dimension);
    applyChanges(new RunnableEx() {
      public void run() throws Exception {
      }
    });
  }

  /**
   * Applies changes in {@link MigDimensionInfo}-s.
   */
  private void applyChanges(final RunnableEx runnable) {
    ExecutionUtils.run(m_layout, new RunnableEx() {
      public void run() throws Exception {
        runnable.run();
        m_layout.writeDimensions();
      }
    });
    m_viewer.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods to implement: strings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title for dialog title area.
   */
  protected abstract String getDialogTitle();

  /**
   * @return the description for dialog title area.
   */
  protected abstract String getDialogMessage();

  /**
   * @return the title for dimensions viewer.
   */
  protected abstract String getViewerTitle();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Methods to implement: dimensions
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void moveDimensionsUp(Iterable<T> dimensions) throws Exception;

  protected abstract void moveDimensionsDown(Iterable<T> dimensions) throws Exception;

  /**
   * Edits given {@link MigDimensionInfo}.
   * 
   * @return <code>true</code> if edit was successful.
   */
  protected abstract boolean editSelectedDimension(T dimension);

  /**
   * @return the new {@link MigDimensionInfo} instance to append/insert.
   */
  protected abstract T createNewDimension(int targetIndex) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dimensions providers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ITableLabelProvider} for {@link MigDimensionInfo}.
   */
  private class DimensionsLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object element, int columnIndex) {
      MigDimensionInfo dimension = (MigDimensionInfo) element;
      if (columnIndex == 0) {
        return "" + dimension.getIndex();
      }
      if (columnIndex == 1) {
        return dimension.getString(false);
      }
      return element.toString();
    }

    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }
}
