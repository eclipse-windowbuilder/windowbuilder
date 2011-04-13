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
package org.eclipse.wb.internal.ercp.devices;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.internal.ercp.ToolkitProvider;
import org.eclipse.wb.internal.ercp.devices.model.CategoryInfo;
import org.eclipse.wb.internal.ercp.devices.model.DeviceInfo;
import org.eclipse.wb.internal.ercp.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;

/**
 * Implementation of {@link IJavaInfoInitializationParticipator} that provides device selection
 * action on editor toolbar.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public final class DeviceSelectionJavaInfoParticipator
    implements
      IJavaInfoInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo javaInfo) throws Exception {
    if (javaInfo instanceof ControlInfo
        && javaInfo.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION) {
      final ControlInfo control = (ControlInfo) javaInfo;
      javaInfo.addBroadcastListener(new ObjectEventListener() {
        private DeviceSelectionItem m_deviceSelectionAction;

        @Override
        public void addHierarchyActions(List<Object> actions) throws Exception {
          if (control.isRoot()) {
            if (m_deviceSelectionAction == null) {
              m_deviceSelectionAction = new DeviceSelectionItem(control);
            }
            actions.add(m_deviceSelectionAction);
            m_deviceSelectionAction.updateActions();
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DeviceSelectionItem
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link ContributionItem} with drop down menu of accessible devices.
   */
  private static final class DeviceSelectionItem extends ContributionItem {
    private final CompositeTopBoundsSupport m_boundsSupport;
    private ToolItem m_toolItem;
    private Menu m_menu;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceSelectionItem(ControlInfo control) {
      m_boundsSupport = (CompositeTopBoundsSupport) control.getTopBoundsSupport();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ContributionItem
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void fill(final ToolBar toolBar, int index) {
      m_toolItem = new ToolItem(toolBar, SWT.DROP_DOWN);
      m_toolItem.setImage(Activator.getImage("devices/device.png"));
      // bind menu
      createMenu(toolBar);
      m_toolItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          createMenu(toolBar);
          // prepare location
          Point menuLocation;
          {
            Rectangle bounds = m_toolItem.getBounds();
            menuLocation = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
          }
          // show device images
          new DeviceFloatingPreview(m_menu, menuLocation);
          // show menu
          m_menu.setLocation(menuLocation);
          m_menu.setVisible(true);
        }
      });
      // update now
      updateActions();
    }

    @Override
    public void dispose() {
      disposeMenu();
      super.dispose();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Actions
    //
    ////////////////////////////////////////////////////////////////////////////
    private final List<DeviceAction> m_deviceActions = Lists.newArrayList();

    /**
     * Disposes drop-down {@link Menu}.
     */
    private void disposeMenu() {
      if (m_menu != null) {
        m_menu.dispose();
        m_menu = null;
      }
    }

    /**
     * Creates drop down {@link Menu} with {@link Action}'s for device selection.
     */
    private void createMenu(Control parent) {
      disposeMenu();
      // create new menu
      m_menu = new Menu(parent);
      // no device
      {
        addDeviceAction(m_menu, new DeviceAction(m_boundsSupport, null, null));
        new Separator().fill(m_menu, -1);
      }
      // add categories
      for (CategoryInfo category : DeviceManager.getCategories()) {
        if (category.isVisible()) {
          MenuItem categoryItem = new MenuItem(m_menu, SWT.CASCADE);
          categoryItem.setText(category.getName());
          //
          Menu categoryMenu = new Menu(parent.getShell(), SWT.DROP_DOWN);
          categoryItem.setMenu(categoryMenu);
          // add devices
          for (DeviceInfo device : category.getDevices()) {
            if (device.isVisible()) {
              addDeviceAction(categoryMenu, new DeviceAction(m_boundsSupport, category, device));
            }
          }
        }
      }
    }

    /**
     * Adds single {@link DeviceAction} to the menu.
     */
    private void addDeviceAction(Menu menu, final DeviceAction deviceAction) {
      m_deviceActions.add(deviceAction);
      //
      MenuItem menuItem = new MenuItem(menu, SWT.NONE);
      menuItem.setText(deviceAction.getText());
      menuItem.setData(deviceAction.m_device);
      // add listeners
      menuItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          deviceAction.run();
        }
      });
    }

    /**
     * Updates this item and {@link DeviceAction}'s.
     */
    private void updateActions() {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          DeviceInfo currentDevice = m_boundsSupport.getDevice();
          for (DeviceAction deviceAction : m_deviceActions) {
            if (deviceAction.m_device == currentDevice) {
              String text;
              if (deviceAction.m_device != null) {
                text = deviceAction.m_category.getName() + " - " + deviceAction.m_device.getName();
              } else {
                text = deviceAction.getText();
              }
              m_toolItem.setText(text);
            }
          }
        }
      });
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DeviceAction
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link Action} for selection single {@link DeviceInfo} for current {@link ControlInfo}.
   */
  private static final class DeviceAction extends Action {
    private final CompositeTopBoundsSupport m_boundsSupport;
    private final CategoryInfo m_category;
    private final DeviceInfo m_device;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeviceAction(CompositeTopBoundsSupport boundsSupport,
        CategoryInfo category,
        DeviceInfo device) {
      m_boundsSupport = boundsSupport;
      m_category = category;
      m_device = device;
      setText(getDeviceTitle(device));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the title text for device action.
     */
    private static String getDeviceTitle(DeviceInfo device) {
      if (device != null) {
        return device.getName()
            + "\t"
            + device.getDisplayBounds().width
            + "x"
            + device.getDisplayBounds().height;
      } else {
        return DeviceMessages.DeviceSelectionJavaInfoParticipator_noDevice;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Action
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      m_boundsSupport.setDevice(m_device);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // DevicePreview
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper that display {@link DeviceInfo} image in floating window.
   */
  private static final class DeviceFloatingPreview {
    private final Shell m_shell;
    private DeviceInfo m_armedDevice;

    public DeviceFloatingPreview(Menu menu, final Point menuLocation) {
      // prepare preview Shell
      {
        m_shell = new Shell(SWT.SINGLE);
        m_shell.addListener(SWT.Paint, new Listener() {
          public void handleEvent(Event event) {
            if (m_armedDevice != null) {
              Rectangle clientArea = m_shell.getClientArea();
              Image image = m_armedDevice.getImage();
              Rectangle imageBounds = image.getBounds();
              event.gc.drawImage(
                  image,
                  0,
                  0,
                  imageBounds.width,
                  imageBounds.height,
                  clientArea.x,
                  clientArea.y,
                  clientArea.width,
                  clientArea.height);
            }
          }
        });
      }
      // listen for Menu hide
      {
        menu.addListener(SWT.Hide, new Listener() {
          public void handleEvent(Event event) {
            if (event.type == SWT.Hide) {
              m_shell.dispose();
            }
          }
        });
      }
      // listen for MenuItem's
      {
        Listener itemListener = new Listener() {
          public void handleEvent(Event event) {
            m_armedDevice = (DeviceInfo) event.widget.getData();
            if (m_armedDevice != null) {
              Rectangle imageBounds = m_armedDevice.getImage().getBounds();
              int width = imageBounds.width / 3;
              int height = imageBounds.height / 3;
              m_shell.setBounds(menuLocation.x - width - 5, menuLocation.y + 5, width, height);
              m_shell.setVisible(true);
            } else {
              m_shell.setVisible(false);
            }
            m_shell.redraw();
          }
        };
        addMenuItemListener(menu, SWT.Arm, itemListener);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Adds {@link Listener} with given types for all {@link MenuItem} in given {@link Menu} and
     * sub-menus.
     */
    private static void addMenuItemListener(Menu menu, int type, Listener listener) {
      for (MenuItem menuItem : menu.getItems()) {
        if (menuItem.getStyle() == SWT.CASCADE) {
          addMenuItemListener(menuItem.getMenu(), type, listener);
        } else {
          menuItem.addListener(type, listener);
        }
      }
    }
  }
}
