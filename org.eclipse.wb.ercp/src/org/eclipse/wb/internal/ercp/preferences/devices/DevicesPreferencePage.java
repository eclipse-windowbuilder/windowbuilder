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
package org.eclipse.wb.internal.ercp.preferences.devices;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.EmptyTransfer;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.devices.DeviceManager;
import org.eclipse.wb.internal.ercp.devices.command.CategoryAddCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryMoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryNameCommand;
import org.eclipse.wb.internal.ercp.devices.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.Command;
import org.eclipse.wb.internal.ercp.devices.command.DeviceMoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.DeviceRemoveCommand;
import org.eclipse.wb.internal.ercp.devices.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.ercp.devices.model.AbstractDeviceInfo;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * {@link PreferencePage} for configuring {@link DeviceManager}.
 * 
 * @author scheglov_ke
 * @coverage ercp.device.ui
 */
public final class DevicesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DevicesPreferencePage() {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image IMAGE_CATEGORY = Activator.getImage("devices/category.gif");
  private static final Image IMAGE_DEVICE = Activator.getImage("devices/device.png");
  private CheckboxTreeViewer m_viewer;

  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container).columns(3);
    //
    createViewer(container);
    createButtonsComposite(container);
    createPreviewComposite(container);
    //
    return container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates {@link CheckboxTreeViewer} for categories/devices.
   */
  private void createViewer(Composite parent) {
    m_viewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.MULTI);
    GridDataFactory.create(m_viewer.getTree()).grab().fill().hintC(50, 20);
    // content provider
    m_viewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getElements(Object inputElement) {
        return DeviceManager.getCategories().toArray();
      }

      public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof CategoryInfo) {
          return ((CategoryInfo) parentElement).getDevices().toArray();
        }
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
      }

      public boolean hasChildren(Object element) {
        return getChildren(element).length != 0;
      }

      public Object getParent(Object element) {
        if (element instanceof DeviceInfo) {
          return ((DeviceInfo) element).getCategory();
        }
        return null;
      }

      public void dispose() {
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      }
    });
    // label provider
    m_viewer.setLabelProvider(new LabelProvider() {
      @Override
      public Image getImage(Object element) {
        if (element instanceof CategoryInfo) {
          return IMAGE_CATEGORY;
        }
        return IMAGE_DEVICE;
      }

      @Override
      public String getText(Object element) {
        if (element instanceof CategoryInfo) {
          CategoryInfo category = (CategoryInfo) element;
          return category.getName();
        } else if (element instanceof DeviceInfo) {
          DeviceInfo device = (DeviceInfo) element;
          return device.getName()
              + "     -     "
              + device.getDisplayBounds().width
              + "x"
              + device.getDisplayBounds().height;
        }
        return null;
      }
    });
    // set input
    m_viewer.setInput(this);
    refreshViewer();
    refreshViewChecks();
    // listeners
    m_viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        AbstractDeviceInfo element = (AbstractDeviceInfo) event.getElement();
        commands_add(new ElementVisibilityCommand(element, event.getChecked()));
      }
    });
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
        updatePreview();
      }
    });
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        onEdit();
      }
    });
    // DND
    configureDND();
  }

  /**
   * Shows actual state of devices in {@link #m_viewer}.
   */
  private void refreshViewer() {
    m_viewer.refresh();
  }

  /**
   * Shows state of "visible" state. Note that this method is slow, in particular
   * {@link CheckboxTreeViewer#setCheckedElements(Object[])}, so should be used carefully.
   */
  private void refreshViewChecks() {
    List<Object> visibleElements = Lists.newArrayList();
    for (CategoryInfo category : DeviceManager.getCategories()) {
      if (category.isVisible()) {
        visibleElements.add(category);
      }
      for (DeviceInfo device : category.getDevices()) {
        if (device.isVisible()) {
          visibleElements.add(device);
        }
      }
    }
    // update viewer
    m_viewer.setCheckedElements(visibleElements.toArray());
  }

  /**
   * @return the {@link List} of selected elements.
   */
  @SuppressWarnings("unchecked")
  private List<Object> getSelectedElements() {
    IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
    return selection.toList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  private Button m_addEntryButton;
  private Button m_editButton;
  private Button m_removeButton;
  private Button m_moveUpButton;
  private Button m_moveDownButton;

  /**
   * Creates {@link Composite} with buttons to modify devices.
   */
  private void createButtonsComposite(Composite parent) {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(buttonsComposite).grabV().fill();
    GridLayoutFactory.create(buttonsComposite).noMargins();
    //
    createButton(buttonsComposite, "Add Category...", new Listener() {
      public void handleEvent(Event event) {
        onAddCategory();
      }
    });
    m_addEntryButton = createButton(buttonsComposite, "Add device...", new Listener() {
      public void handleEvent(Event event) {
        onAddDevice();
      }
    });
    //
    createButtonSeparator(buttonsComposite);
    m_editButton = createButton(buttonsComposite, "Edit...", new Listener() {
      public void handleEvent(Event event) {
        onEdit();
      }
    });
    m_removeButton = createButton(buttonsComposite, "Remove...", new Listener() {
      public void handleEvent(Event event) {
        onRemove();
      }
    });
    //
    createButtonSeparator(buttonsComposite);
    m_moveUpButton = createButton(buttonsComposite, "Up", new Listener() {
      public void handleEvent(Event event) {
        onMove(-1);
      }
    });
    m_moveDownButton = createButton(buttonsComposite, "Down", new Listener() {
      public void handleEvent(Event event) {
        onMove(+2);
      }
    });
    //
    createButtonSeparator(buttonsComposite);
    createButton(buttonsComposite, "Collapse All", new Listener() {
      public void handleEvent(Event event) {
        m_viewer.collapseAll();
      }
    });
    createButton(buttonsComposite, "Expand All", new Listener() {
      public void handleEvent(Event event) {
        m_viewer.expandAll();
      }
    });
    // update buttons first time
    updateButtons();
  }

  /**
   * Creates {@link Button} with given text and {@link SWT#Selection} listener.
   */
  private static Button createButton(Composite parent, String text, Listener selectionListener) {
    Button button = new Button(parent, SWT.NONE);
    GridDataFactory.create(button).grabH().fillH();
    button.setText(text);
    button.addListener(SWT.Selection, selectionListener);
    return button;
  }

  /**
   * Creates separator between buttons on vertical buttons bar.
   */
  private static void createButtonSeparator(Composite parent) {
    Label separator = new Label(parent, SWT.NONE);
    GridDataFactory.create(separator).hintV(7);
  }

  /**
   * Updates buttons according to selection in {@link #m_viewer}.
   */
  private void updateButtons() {
    List<Object> selection = getSelectedElements();
    m_addEntryButton.setEnabled(!selection.isEmpty());
    m_editButton.setEnabled(selection.size() == 1);
    m_removeButton.setEnabled(!selection.isEmpty());
    // move up/down
    List<CategoryInfo> categories = DeviceManager.getCategories();
    {
      boolean upEnabled = !selection.isEmpty();
      boolean downEnabled = !selection.isEmpty();
      for (Object element : selection) {
        if (element instanceof CategoryInfo) {
          upEnabled &= categories.indexOf(element) != 0;
          downEnabled &= categories.indexOf(element) != categories.size() - 1;
        } else if (element instanceof DeviceInfo) {
          DeviceInfo device = (DeviceInfo) element;
          List<DeviceInfo> devices = device.getCategory().getDevices();
          m_editButton.setEnabled(m_editButton.isEnabled() && !device.isContributed());
          upEnabled &= devices.indexOf(device) != 0;
          downEnabled &= devices.indexOf(device) != devices.size() - 1;
        }
      }
      m_moveUpButton.setEnabled(upEnabled);
      m_moveDownButton.setEnabled(downEnabled);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: preview
  //
  ////////////////////////////////////////////////////////////////////////////
  private DevicePreviewCanvas m_previewCanvas;

  /**
   * Creates {@link DevicePreviewCanvas} for preview selected {@link DeviceInfo}.
   */
  private void createPreviewComposite(Composite parent) {
    m_previewCanvas = new DevicePreviewCanvas(parent, SWT.NONE);
    GridDataFactory.create(m_previewCanvas).fill().hintH(150);
  }

  /**
   * Shows selected {@link DeviceInfo} in preview.
   */
  private void updatePreview() {
    List<Object> selection = getSelectedElements();
    if (selection.size() == 1 && selection.get(0) instanceof DeviceInfo) {
      DeviceInfo device = (DeviceInfo) selection.get(0);
      m_previewCanvas.setDevice(device);
    } else {
      m_previewCanvas.setDevice(null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Edits device element.
   */
  private void onEdit() {
    Object element = getSelectedElements().get(0);
    if (element instanceof CategoryInfo) {
      CategoryInfo category = (CategoryInfo) element;
      InputDialog inputDialog =
          new InputDialog(getShell(),
              "Category",
              "Enter new category name:",
              category.getName(),
              null);
      // execute dialog
      if (inputDialog.open() == Window.OK) {
        commands_add(new CategoryNameCommand(category, inputDialog.getValue()));
      }
    } else if (element instanceof DeviceInfo) {
      DeviceInfo device = (DeviceInfo) element;
      DeviceEditDialog dialog = new DeviceEditDialog(device);
      // execute dialog
      if (dialog.open() == Window.OK) {
        commands_add(dialog.getCommand());
      }
    }
  }

  /**
   * Removes selected {@link CategoryInfo} or {@link DeviceInfo}.
   */
  private void onRemove() {
    List<Object> selection = getSelectedElements();
    if (MessageDialog.openConfirm(getShell(), "Confirm", "Are you sure you want to remove "
        + selection.size()
        + " selected element(s)?")) {
      for (Object element : selection) {
        if (element instanceof CategoryInfo) {
          commands_add(new CategoryRemoveCommand((CategoryInfo) element));
        } else if (element instanceof DeviceInfo) {
          commands_add(new DeviceRemoveCommand((DeviceInfo) element));
        }
      }
      refreshViewer();
    }
  }

  /**
   * Adds new {@link CategoryInfo}.
   */
  private void onAddCategory() {
    InputDialog inputDialog =
        new InputDialog(getShell(), "New category", "Enter new category name:", "", null);
    if (inputDialog.open() == Window.OK) {
      commands_add(new CategoryAddCommand("category_" + System.currentTimeMillis(),
          inputDialog.getValue()));
    }
  }

  /**
   * Adds new {@link DeviceInfo}.
   */
  private void onAddDevice() {
    final CategoryInfo targetCategory;
    {
      Object element = getSelectedElements().get(0);
      if (element instanceof CategoryInfo) {
        targetCategory = (CategoryInfo) element;
      } else if (element instanceof DeviceInfo) {
        targetCategory = ((DeviceInfo) element).getCategory();
      } else {
        return;
      }
    }
    // open dialog
    DeviceAddDialog deviceAddDialog = new DeviceAddDialog();
    if (deviceAddDialog.open() == Window.OK) {
      commands_add(deviceAddDialog.getCommand(targetCategory));
    }
  }

  /**
   * Moves selected elements up/down.
   */
  private void onMove(int delta) {
    m_viewer.getTree().setRedraw(false);
    try {
      for (Object element : getSelectedElements()) {
        if (element instanceof CategoryInfo) {
          CategoryInfo category = (CategoryInfo) element;
          List<CategoryInfo> categories = DeviceManager.getCategories();
          int index = categories.indexOf(element);
          int targetIndex = index + delta;
          CategoryInfo nextCategory =
              targetIndex < categories.size() ? (CategoryInfo) categories.get(targetIndex) : null;
          commands_add(new CategoryMoveCommand(category, nextCategory));
        } else if (element instanceof DeviceInfo) {
          DeviceInfo device = (DeviceInfo) element;
          CategoryInfo category = device.getCategory();
          List<DeviceInfo> devices = category.getDevices();
          int index = devices.indexOf(device);
          if (index + delta < devices.size()) {
            commands_add(new DeviceMoveCommand(device, category, devices.get(index + delta)));
          } else {
            commands_add(new DeviceMoveCommand(device, category, null));
          }
        }
      }
      refreshViewer();
      updateButtons();
    } finally {
      m_viewer.getTree().setRedraw(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DND
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<Object> m_dragElements;
  private boolean m_dragCategory;

  /**
   * Configures DND in {@link #m_viewer}.
   */
  private void configureDND() {
    Transfer[] transfers = new Transfer[]{EmptyTransfer.INSTANCE};
    m_viewer.addDragSupport(DND.DROP_MOVE, transfers, new DragSourceListener() {
      public void dragStart(DragSourceEvent event) {
        m_dragElements = getSelectedElements();
        m_dragCategory = m_dragElements.get(0) instanceof CategoryInfo;
        // check that we drag only categories or only entries
        for (Object element : m_dragElements) {
          if (m_dragCategory != element instanceof CategoryInfo) {
            event.doit = false;
          }
        }
      }

      public void dragSetData(DragSourceEvent event) {
      }

      public void dragFinished(DragSourceEvent event) {
      }
    });
    ViewerDropAdapter dropAdapter = new ViewerDropAdapter(m_viewer) {
      @Override
      protected int determineLocation(DropTargetEvent event) {
        if (event.item instanceof Item) {
          Item item = (Item) event.item;
          Point coordinates = m_viewer.getControl().toControl(event.x, event.y);
          Rectangle bounds = getBounds(item);
          // when drag device, relation with category can be only ON
          if (!m_dragCategory && determineTarget(event) instanceof CategoryInfo) {
            return LOCATION_ON;
          }
          // in all other cases, drop before/after
          return coordinates.y < bounds.y + bounds.height / 2 ? LOCATION_BEFORE : LOCATION_AFTER;
        }
        return LOCATION_NONE;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        // category can be dragged only relative other category
        if (m_dragCategory) {
          return target instanceof CategoryInfo;
        }
        // all other cases are valid
        return true;
      }

      @Override
      public boolean performDrop(Object data) {
        Object target = getCurrentTarget();
        int location = getCurrentLocation();
        if (m_dragCategory) {
          Assert.instanceOf(CategoryInfo.class, target);
          Assert.isTrue(location == LOCATION_BEFORE || location == LOCATION_AFTER);
          // prepare next category
          CategoryInfo nextCategory;
          {
            List<CategoryInfo> categories = DeviceManager.getCategories();
            int index = categories.indexOf(target);
            if (location == LOCATION_BEFORE) {
              nextCategory = categories.get(index);
            } else {
              nextCategory = GenericsUtils.getNextOrNull(categories, index);
            }
          }
          // add commands
          for (Object element : m_dragElements) {
            CategoryInfo category = (CategoryInfo) element;
            commands_add(new CategoryMoveCommand(category, nextCategory));
          }
        } else if (target instanceof CategoryInfo) {
          Assert.isTrue(location == LOCATION_ON);
          CategoryInfo targetCategory = (CategoryInfo) target;
          for (Object element : m_dragElements) {
            DeviceInfo device = (DeviceInfo) element;
            commands_add(new DeviceMoveCommand(device, targetCategory, null));
          }
        } else {
          DeviceInfo targetDevice = (DeviceInfo) target;
          CategoryInfo targetCategory = targetDevice.getCategory();
          // prepare next device
          DeviceInfo nextDevice;
          {
            List<DeviceInfo> entries = targetCategory.getDevices();
            int index = entries.indexOf(targetDevice);
            if (location == LOCATION_BEFORE) {
              nextDevice = entries.get(index);
            } else {
              nextDevice = GenericsUtils.getNextOrNull(entries, index);
            }
          }
          // add commands
          for (Object element : m_dragElements) {
            DeviceInfo device = (DeviceInfo) element;
            commands_add(new DeviceMoveCommand(device, targetCategory, nextDevice));
          }
        }
        // refresh viewer to show result of applying commands
        refreshViewer();
        return true;
      }
    };
    dropAdapter.setScrollExpandEnabled(false);
    m_viewer.addDropSupport(DND.DROP_MOVE, transfers, dropAdapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<Command> m_commands = Lists.newArrayList();

  /**
   * Executes new {@link Command} and updates {@link #m_viewer} accordingly.
   */
  private void commands_add(Command command) {
    command.execute();
    command.addToCommandList(m_commands);
    refreshViewer();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performOk() {
    // write all executed commands
    {
      for (Command command : m_commands) {
        DeviceManager.commands_add(command);
      }
      DeviceManager.commands_write();
    }
    // success
    return true;
  }

  @Override
  public boolean performCancel() {
    // don't save commands, force reload to undo executed commands
    DeviceManager.forceReload();
    return super.performCancel();
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    // remove all commands
    DeviceManager.resetToDefaults();
    // show new state
    refreshViewer();
  }
}