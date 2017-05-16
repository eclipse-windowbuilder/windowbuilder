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
package org.eclipse.wb.internal.core.model.property.order;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.gef.tree.dnd.TreeTransfer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

/**
 * Dialog for editing components order.
 *
 * @author lobas_av
 * @coverage core.model.property.order
 */
final class ReorderDialog extends ResizableDialog {
  private final TabOrderInfo m_orderInfo;
  private final List<AbstractComponentInfo> m_allInfos;
  private CheckboxTableViewer m_viewer;
  private Button m_upButton;
  private Button m_downButton;
  private Button m_unSelectAllButton;
  private Button m_selectAllButton;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ReorderDialog(Shell parentShell, TabOrderInfo orderInfo) {
    super(parentShell, DesignerPlugin.getDefault());
    m_orderInfo = orderInfo;
    m_allInfos = m_orderInfo.getInfos();
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    // container
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(container).columns(2);
    // title
    Label title = new Label(container, SWT.NONE);
    title.setText(ModelMessages.ReorderDialog_childrenLabel);
    GridDataFactory.create(title).spanH(2);
    // reorder viewer
    m_viewer = CheckboxTableViewer.newCheckList(container, SWT.FULL_SELECTION | SWT.BORDER);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent e) {
        do_viewer_selectionChanged();
      }
    });
    m_viewer.setContentProvider(new ArrayContentProvider());
    m_viewer.setLabelProvider(ObjectsLabelProvider.INSTANCE);
    //
    TableFactory.modify(m_viewer).headerVisible(true).linesVisible(true);
    GridDataFactory.create(m_viewer.getControl()).fill().grab().hintC(60, 15);
    TableFactory.modify(m_viewer).newColumn().width(convertWidthInCharsToPixels(60)).text(
        ModelMessages.ReorderDialog_childColumn);
    // show all components and select first
    m_viewer.setInput(m_allInfos);
    m_viewer.getTable().select(0);
    // check all components that are already in tab order, or all, if there is not components
    m_viewer.setCheckedElements(m_orderInfo.getOrderedInfos().toArray());
    setupDragAndDrop();
    // buttons bar
    Composite buttonsComposite = new Composite(container, SWT.NONE);
    GridLayoutFactory.create(buttonsComposite).noMargins();
    GridDataFactory.create(buttonsComposite).fill().hintHC(18);
    // up
    m_upButton = new Button(buttonsComposite, SWT.NONE);
    m_upButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        do_upButton_widgetSelected(e);
      }
    });
    m_upButton.setText(ModelMessages.ReorderDialog_upButton);
    GridDataFactory.create(m_upButton).fillH().grabH();
    // down
    m_downButton = new Button(buttonsComposite, SWT.NONE);
    m_downButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        do_downButton_widgetSelected(e);
      }
    });
    m_downButton.setText(ModelMessages.ReorderDialog_downButton);
    GridDataFactory.create(m_downButton).fillH().grabH();
    // filler
    GridDataFactory.create(new Label(buttonsComposite, SWT.NONE)).fillH().grabH();
    // select all
    m_selectAllButton = new Button(buttonsComposite, SWT.NONE);
    m_selectAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(true);
      }
    });
    m_selectAllButton.setText(ModelMessages.ReorderDialog_selectAllButton);
    GridDataFactory.create(m_selectAllButton).fillH().grabH();
    // unselect all
    m_unSelectAllButton = new Button(buttonsComposite, SWT.NONE);
    m_unSelectAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_viewer.setAllChecked(false);
      }
    });
    m_unSelectAllButton.setText(ModelMessages.ReorderDialog_deselectAllButton);
    GridDataFactory.create(m_unSelectAllButton).fillH().grabH();
    // calculate start state
    do_viewer_selectionChanged();
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.ReorderDialog_title);
  }

  @Override
  ////////////////////////////////////////////////////////////////////////////
  //
  // Events handling
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void okPressed() {
    List<AbstractComponentInfo> orderedInfos = m_orderInfo.getOrderedInfos();
    orderedInfos.clear();
    for (Object element : m_viewer.getCheckedElements()) {
      orderedInfos.add((AbstractComponentInfo) element);
    }
    super.okPressed();
  }

  protected void do_viewer_selectionChanged() {
    IStructuredSelection selection = getSelection();
    if (selection.isEmpty()) {
      m_upButton.setEnabled(false);
      m_downButton.setEnabled(false);
    } else {
      JavaInfo child = (JavaInfo) selection.getFirstElement();
      m_upButton.setEnabled(m_allInfos.indexOf(child) > 0);
      m_downButton.setEnabled(m_allInfos.indexOf(child) < m_allInfos.size() - 1);
    }
  }

  protected void do_upButton_widgetSelected(SelectionEvent e) {
    AbstractComponentInfo child = getSelectionElement();
    int index = m_allInfos.indexOf(child);
    m_allInfos.remove(index);
    m_allInfos.add(index - 1, child);
    m_viewer.refresh();
    do_viewer_selectionChanged();
  }

  protected void do_downButton_widgetSelected(SelectionEvent e) {
    AbstractComponentInfo child = getSelectionElement();
    int index = m_allInfos.indexOf(child);
    m_allInfos.remove(child);
    m_allInfos.add(index + 1, child);
    m_viewer.refresh();
    do_viewer_selectionChanged();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private IStructuredSelection getSelection() {
    return (IStructuredSelection) m_viewer.getSelection();
  }

  private AbstractComponentInfo getSelectionElement() {
    return (AbstractComponentInfo) getSelection().getFirstElement();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drag and drop
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setupDragAndDrop() {
    int ops = DND.DROP_MOVE;
    Transfer[] transfers = new Transfer[]{TreeTransfer.INSTANCE};
    //
    final AbstractComponentInfo dragWidget[] = new AbstractComponentInfo[1];
    m_viewer.addDragSupport(ops, transfers, new DragSourceAdapter() {
      @Override
      public void dragStart(DragSourceEvent event) {
        dragWidget[0] = getSelectionElement();
      }
    });
    ViewerDropAdapter adapter = new ViewerDropAdapter(m_viewer) {
      private AbstractComponentInfo m_targetWidget;

      @Override
      protected int determineLocation(DropTargetEvent event) {
        int location = super.determineLocation(event);
        if (location == LOCATION_BEFORE || location == LOCATION_AFTER) {
          location = LOCATION_ON;
        }
        return location;
      }

      @Override
      public boolean performDrop(Object data) {
        int sourceIndex = m_allInfos.indexOf(dragWidget[0]);
        int targetIndex = m_allInfos.indexOf(m_targetWidget);
        m_allInfos.remove(sourceIndex);
        if (m_targetWidget == null) {
          m_allInfos.add(dragWidget[0]);
        } else if (sourceIndex < targetIndex) {
          m_allInfos.add(targetIndex - 1, dragWidget[0]);
        } else {
          m_allInfos.add(targetIndex, dragWidget[0]);
        }
        m_viewer.refresh();
        return true;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        m_targetWidget = (AbstractComponentInfo) target;
        return true;
      }
    };
    adapter.setFeedbackEnabled(true);
    m_viewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);
  }
}