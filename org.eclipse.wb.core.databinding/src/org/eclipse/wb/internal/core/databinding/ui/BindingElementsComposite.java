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
package org.eclipse.wb.internal.core.databinding.ui;

import org.eclipse.wb.core.editor.IDesignPage;
import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.gef.tree.dnd.TreeTransfer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;

import java.text.MessageFormat;
import java.util.List;

/**
 * {@link Composite} for viewers {@link IBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class BindingElementsComposite extends Composite {
  private final IDatabindingsProvider m_databindingsProvider;
  private final TableViewer m_bindingViewer;
  private final ToolItem m_editAction;
  private final ToolItem m_deleteAction;
  private final ToolItem m_deleteAllAction;
  private final ToolItem m_upAction;
  private final ToolItem m_downAction;
  private final ToolItem m_gotoDefinitionAction;
  private final MenuItem m_editActionMenu;
  private final MenuItem m_deleteActionMenu;
  private final MenuItem m_deleteAllActionMenu;
  private final MenuItem m_upActionMenu;
  private final MenuItem m_downActionMenu;
  private final MenuItem m_gotoDefinitionActionMenu;
  private final SelectionListener m_deleteBindingListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      deleteBindind();
    }
  };
  private final SelectionListener m_deleteAllBindingsListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      deleteAllBindinds();
    }
  };
  private final SelectionListener m_moveUpListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      moveBinding(true);
    }
  };
  private final SelectionListener m_moveDownListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      moveBinding(false);
    }
  };
  private final SelectionListener m_gotoDefinitionListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      gotoDefinition();
    }
  };
  private boolean m_skipSelectionEvent;
  private IBindingSelectionListener m_listener;
  private SelectionListener m_editBindingListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingElementsComposite(Composite parent,
      int style,
      IDatabindingsProvider databindingsProvider,
      IDialogSettings settings) {
    super(parent, style);
    m_databindingsProvider = databindingsProvider;
    // configure container
    GridLayoutFactory.create(this).columns(2);
    // title
    Label titleLabel = new Label(this, SWT.NONE);
    titleLabel.setText(Messages.BindingElementsComposite_boundProperties);
    GridDataFactory.create(titleLabel).fillH().grabH();
    // buttons
    ToolBar toolBar = new ToolBar(this, SWT.FLAT);
    GridDataFactory.create(toolBar).alignHR().grabH();
    // viewer for bindings
    m_bindingViewer =
        new TableViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
    GridDataFactory.create(m_bindingViewer.getControl()).fill().grab().spanH(2);
    TableFactory.modify(m_bindingViewer).standard();
    m_bindingViewer.setContentProvider(new ArrayContentProvider());
    m_databindingsProvider.configureBindingViewer(settings, m_bindingViewer);
    // viewer events
    m_bindingViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleBindingSelection(selection);
      }
    });
    m_bindingViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        if (m_editBindingListener != null && !UiUtils.isEmpty(m_bindingViewer.getSelection())) {
          m_editBindingListener.widgetSelected(null);
        }
      }
    });
    m_bindingViewer.getControl().addListener(SWT.KeyDown, new Listener() {
      public void handleEvent(Event event) {
        if (event.character == SWT.DEL && !UiUtils.isEmpty(m_bindingViewer.getSelection())) {
          deleteBindind();
        }
      }
    });
    setupDragAndDrop();
    // create context menu
    Table bindingTable = m_bindingViewer.getTable();
    Menu contextMenu = new Menu(bindingTable);
    bindingTable.setMenu(contextMenu);
    // edit
    m_editActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_editActionMenu.setText(Messages.BindingElementsComposite_editAction);
    m_editActionMenu.setImage(Activator.getImage("link_edit_action.png"));
    m_editActionMenu.setEnabled(false);
    // separator
    new MenuItem(contextMenu, SWT.SEPARATOR);
    // delete
    m_deleteActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_deleteActionMenu.setText(Messages.BindingElementsComposite_deleteAction);
    m_deleteActionMenu.setImage(Activator.getImage("link_delete_action.png"));
    m_deleteActionMenu.setEnabled(false);
    m_deleteActionMenu.addSelectionListener(m_deleteBindingListener);
    // delete all
    m_deleteAllActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_deleteAllActionMenu.setText(Messages.BindingElementsComposite_deleteAllAction);
    m_deleteAllActionMenu.setImage(Activator.getImage("link_delete_all_action.png"));
    m_deleteAllActionMenu.setEnabled(false);
    m_deleteAllActionMenu.addSelectionListener(m_deleteAllBindingsListener);
    // separator
    new MenuItem(contextMenu, SWT.SEPARATOR);
    // move up
    m_upActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_upActionMenu.setText(Messages.BindingElementsComposite_moveUpAction);
    m_upActionMenu.setImage(Activator.getImage("up.png"));
    m_upActionMenu.setEnabled(false);
    m_upActionMenu.addSelectionListener(m_moveUpListener);
    // move down
    m_downActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_downActionMenu.setText(Messages.BindingElementsComposite_moveDownAction);
    m_downActionMenu.setImage(Activator.getImage("down.png"));
    m_downActionMenu.setEnabled(false);
    m_downActionMenu.addSelectionListener(m_moveDownListener);
    // separator
    new MenuItem(contextMenu, SWT.SEPARATOR);
    // goto definition
    m_gotoDefinitionActionMenu = new MenuItem(contextMenu, SWT.NONE);
    m_gotoDefinitionActionMenu.setText(Messages.BindingElementsComposite_gotoDefinitionAction);
    m_gotoDefinitionActionMenu.setImage(Activator.getImage("goto_definition.gif"));
    m_gotoDefinitionActionMenu.setEnabled(false);
    m_gotoDefinitionActionMenu.addSelectionListener(m_gotoDefinitionListener);
    // refresh
    ToolItem refreshItem = new ToolItem(toolBar, SWT.NONE);
    refreshItem.setToolTipText(Messages.BindingElementsComposite_reparseItem);
    refreshItem.setImage(DesignerPlugin.getImage("editor_refresh.png"));
    refreshItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        IEditorPart editor =
            DesignerPlugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof IDesignerEditor) {
          IDesignerEditor designerEditor = (IDesignerEditor) editor;
          IDesignPage designPage = designerEditor.getMultiMode().getDesignPage();
          designPage.refreshGEF();
        } else if (m_databindingsProvider != null) {
          m_databindingsProvider.refreshDesigner();
        }
      }
    });
    //
    new ToolItem(toolBar, SWT.SEPARATOR);
    // edit
    m_editAction = new ToolItem(toolBar, SWT.NONE);
    m_editAction.setToolTipText(Messages.BindingElementsComposite_editItem);
    m_editAction.setImage(Activator.getImage("link_edit_action.png"));
    m_editAction.setEnabled(false);
    //
    new ToolItem(toolBar, SWT.SEPARATOR);
    // delete
    m_deleteAction = new ToolItem(toolBar, SWT.NONE);
    m_deleteAction.setToolTipText(Messages.BindingElementsComposite_deleteItem);
    m_deleteAction.setImage(Activator.getImage("link_delete_action.png"));
    m_deleteAction.setEnabled(false);
    m_deleteAction.addSelectionListener(m_deleteBindingListener);
    // delete all
    m_deleteAllAction = new ToolItem(toolBar, SWT.NONE);
    m_deleteAllAction.setToolTipText(Messages.BindingElementsComposite_deleteAllItem);
    m_deleteAllAction.setImage(Activator.getImage("link_delete_all_action.png"));
    m_deleteAllAction.setEnabled(false);
    m_deleteAllAction.addSelectionListener(m_deleteAllBindingsListener);
    // separator
    new ToolItem(toolBar, SWT.SEPARATOR);
    // move up
    m_upAction = new ToolItem(toolBar, SWT.NONE);
    m_upAction.setToolTipText(Messages.BindingElementsComposite_moveUpItem);
    m_upAction.setImage(Activator.getImage("up.png"));
    m_upAction.setEnabled(false);
    m_upAction.addSelectionListener(m_moveUpListener);
    // move down
    m_downAction = new ToolItem(toolBar, SWT.NONE);
    m_downAction.setToolTipText(Messages.BindingElementsComposite_moveDownItem);
    m_downAction.setImage(Activator.getImage("down.png"));
    m_downAction.setEnabled(false);
    m_downAction.addSelectionListener(m_moveDownListener);
    // separator
    new ToolItem(toolBar, SWT.SEPARATOR);
    // goto definition
    m_gotoDefinitionAction = new ToolItem(toolBar, SWT.NONE);
    m_gotoDefinitionAction.setToolTipText(Messages.BindingElementsComposite_gotoDefinitionItem);
    m_gotoDefinitionAction.setImage(Activator.getImage("goto_definition.gif"));
    m_gotoDefinitionAction.setEnabled(false);
    m_gotoDefinitionAction.addSelectionListener(m_gotoDefinitionListener);
    // external actions
    m_databindingsProvider.fillExternalBindingActions(toolBar, contextMenu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drag and drop
  //
  ////////////////////////////////////////////////////////////////////////////
  private void setupDragAndDrop() {
    Transfer[] transfers = new Transfer[]{TreeTransfer.INSTANCE};
    final IBindingInfo[] binding = new IBindingInfo[1];
    m_bindingViewer.addDragSupport(DND.DROP_MOVE, transfers, new DragSourceAdapter() {
      @Override
      public void dragStart(DragSourceEvent event) {
        List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
        event.doit = bindings.size() > 1;
        binding[0] = getSelectionBinding();
      }
    });
    ViewerDropAdapter adapter = new ViewerDropAdapter(m_bindingViewer) {
      @Override
      protected int determineLocation(DropTargetEvent event) {
        int location = super.determineLocation(event);
        if (location == LOCATION_BEFORE || location == LOCATION_AFTER) {
          location = LOCATION_ON;
        }
        return location;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (binding[0] != target && target != null) {
          List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
          return m_databindingsProvider.canMoveBinding(binding[0], bindings.indexOf(target), false);
        }
        return false;
      }

      @Override
      public boolean performDrop(Object data) {
        List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
        int sourceIndex = bindings.indexOf(binding[0]);
        int targetIndex = bindings.indexOf(getCurrentTarget());
        //
        m_databindingsProvider.moveBinding(binding[0], sourceIndex, targetIndex, false);
        //
        m_bindingViewer.refresh();
        handleBindingSelection((IStructuredSelection) m_bindingViewer.getSelection());
        //
        return true;
      }
    };
    adapter.setFeedbackEnabled(true);
    m_bindingViewer.addDropSupport(DND.DROP_MOVE | DND.DROP_DEFAULT, transfers, adapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new content to this container if <code>refresh</code> is <code>false</code> or update
   * existing content.
   */
  public void setInput(boolean refresh) {
    List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
    if (refresh) {
      m_bindingViewer.refresh();
    } else {
      m_bindingViewer.setInput(bindings);
    }
    setDeleteAllActionEnabled(!bindings.isEmpty());
  }

  public TableViewer getViewer() {
    return m_bindingViewer;
  }

  public void setEditBindingListener(SelectionListener listener) {
    m_editBindingListener = listener;
    m_editAction.addSelectionListener(listener);
    m_editActionMenu.addSelectionListener(listener);
  }

  public void setDeleteAllActionEnabled(boolean enabled) {
    m_deleteAllAction.setEnabled(enabled);
    m_deleteAllActionMenu.setEnabled(enabled);
  }

  public IBindingInfo getSelectionBinding() {
    IStructuredSelection selection = (IStructuredSelection) m_bindingViewer.getSelection();
    return UiUtils.isEmpty(selection) ? null : (IBindingInfo) selection.getFirstElement();
  }

  public void setBindingSelectionListener(IBindingSelectionListener listener) {
    m_listener = listener;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void handleBindingSelection(IStructuredSelection selection) {
    // handle recursion
    if (m_skipSelectionEvent) {
      return;
    }
    m_skipSelectionEvent = true;
    //
    try {
      // configure actions
      boolean enabled = !UiUtils.isEmpty(selection);
      m_editAction.setEnabled(enabled);
      m_editActionMenu.setEnabled(enabled);
      m_deleteAction.setEnabled(enabled);
      m_deleteActionMenu.setEnabled(enabled);
      //
      IBindingInfo binding = enabled ? (IBindingInfo) selection.getFirstElement() : null;
      //
      boolean moveUpEnabled = false;
      boolean moveDownEnabled = false;
      if (enabled) {
        List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
        int size = bindings.size();
        if (size > 1) {
          int index = bindings.indexOf(binding);
          if (index > 0) {
            moveUpEnabled = m_databindingsProvider.canMoveBinding(binding, index - 1, true);
          }
          if (index < size - 1) {
            moveDownEnabled = m_databindingsProvider.canMoveBinding(binding, index + 1, true);
          }
        }
      }
      //
      m_upAction.setEnabled(moveUpEnabled);
      m_upActionMenu.setEnabled(moveUpEnabled);
      //
      m_downAction.setEnabled(moveDownEnabled);
      m_downActionMenu.setEnabled(moveDownEnabled);
      //
      m_gotoDefinitionAction.setEnabled(enabled);
      m_gotoDefinitionActionMenu.setEnabled(enabled);
      // handle selection
      if (enabled && m_listener != null) {
        m_listener.selectionChanged(binding);
      }
      // return focus to bounded table
      m_bindingViewer.getTable().setFocus();
    } finally {
      m_skipSelectionEvent = false;
    }
  }

  protected void deleteBindind() {
    // prepare selection
    IBindingInfo binding = getSelectionBinding();
    // open confirm
    boolean canDelete = canDeleteBinding(m_databindingsProvider, binding, getShell());
    // handle delete
    if (canDelete) {
      // delete
      List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
      int index = bindings.indexOf(binding);
      m_databindingsProvider.deleteBinding(binding);
      if (!isDisposed()) {
        m_bindingViewer.refresh();
        // invoke events
        if (m_listener != null) {
          m_listener.selectionChanged(null);
        }
        // update selection
        if (index == bindings.size()) {
          index--;
        }
        if (index != -1) {
          m_bindingViewer.setSelection(new StructuredSelection(bindings.get(index)), true);
        }
      }
    }
  }

  public static boolean canDeleteBinding(final IDatabindingsProvider databindingsProvider,
      final IBindingInfo binding,
      Shell shell) {
    // prepare message
    String message = ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        String message = databindingsProvider.getBindingPresentationText(binding);
        if (message == null) {
          message =
              "Binding["
                  + UiUtils.getPresentationText(binding.getTarget(), binding.getTargetProperty())
                  + " : "
                  + UiUtils.getPresentationText(binding.getModel(), binding.getModelProperty())
                  + "]";
        }
        return message;
      }
    },
        "<exception, see log>");
    // open confirm
    boolean canDelete =
        MessageDialog.openConfirm(
            shell,
            Messages.BindingElementsComposite_deleteTitle,
            MessageFormat.format(Messages.BindingElementsComposite_deleteMessage, message));
    return canDelete;
  }

  protected void deleteAllBindinds() {
    // open confirm
    boolean canDelete =
        MessageDialog.openConfirm(
            getShell(),
            Messages.BindingElementsComposite_deleteAllTitle,
            Messages.BindingElementsComposite_deleteAllMessage);
    // handle delete
    if (canDelete) {
      // delete
      m_databindingsProvider.deleteAllBindings();
      m_bindingViewer.refresh();
      // invoke events
      if (m_listener != null) {
        m_listener.selectionChanged(null);
      }
      // update buttons
      setDeleteAllActionEnabled(false);
    }
  }

  private void moveBinding(boolean up) {
    List<IBindingInfo> bindings = m_databindingsProvider.getBindings();
    IBindingInfo binding = getSelectionBinding();
    int sourceIndex = bindings.indexOf(binding);
    int targetIndex = sourceIndex + (up ? -1 : 1);
    //
    m_databindingsProvider.moveBinding(binding, sourceIndex, targetIndex, true);
    //
    m_bindingViewer.refresh();
    handleBindingSelection((IStructuredSelection) m_bindingViewer.getSelection());
  }

  private void gotoDefinition() {
    m_databindingsProvider.gotoDefinition(getSelectionBinding());
  }
}