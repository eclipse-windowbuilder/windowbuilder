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
package org.eclipse.wb.core.controls.test;

import org.eclipse.wb.core.controls.flyout.FlyoutControlComposite;
import org.eclipse.wb.core.controls.flyout.IFlyoutPreferences;
import org.eclipse.wb.core.controls.flyout.MemoryFlyoutPreferences;
import org.eclipse.wb.draw2d.IColorConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Application for testing {@link FlyoutControlComposite}.
 *
 * @author scheglov_ke
 * @coverage core.test
 */
public class FlyoutTest {
  private Shell m_shell;
  private CTabFolder m_tabFolder;

  public static void main(String[] args) {
    try {
      FlyoutTest window = new FlyoutTest();
      window.open();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void open() {
    final Display display = Display.getDefault();
    createContents();
    m_shell.open();
    m_shell.layout();
    while (!m_shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  protected void createContents() {
    m_shell = new Shell();
    m_shell.setBounds(600, 300, 800, 600);
    m_shell.setText("SWT Application");
    m_shell.setLayout(new FillLayout());
    //
    m_tabFolder = new CTabFolder(m_shell, SWT.NONE);
    m_tabFolder.setBackground(IColorConstants.button);
    m_tabFolder.setSelectionBackground(IColorConstants.button);
    {
      FlyoutControlComposite flyoutComposite = createTab("WEST", IFlyoutPreferences.DOCK_WEST);
      flyoutComposite.setValidDockLocations(IFlyoutPreferences.DOCK_WEST
          | IFlyoutPreferences.DOCK_EAST);
    }
    createTab("EAST", IFlyoutPreferences.DOCK_EAST);
    {
      FlyoutControlComposite flyoutComposite = createTab("NORTH", IFlyoutPreferences.DOCK_NORTH);
      flyoutComposite.setValidDockLocations(IFlyoutPreferences.DOCK_NORTH
          | IFlyoutPreferences.DOCK_SOUTH);
    }
    createTab("SOUTH", IFlyoutPreferences.DOCK_SOUTH);
  }

  private FlyoutControlComposite createTab(String title, int dockLocation) {
    CTabItem tabItem = new CTabItem(m_tabFolder, SWT.NONE);
    tabItem.setText(title);
    //
    IFlyoutPreferences preferences =
        new MemoryFlyoutPreferences(dockLocation, IFlyoutPreferences.STATE_OPEN, 200);
    FlyoutControlComposite flyoutControlComposite =
        new FlyoutControlComposite(m_tabFolder, SWT.NONE, preferences);
    flyoutControlComposite.setTitleText("Structure");
    {
      Text flyout = new Text(flyoutControlComposite.getFlyoutParent(), SWT.BORDER);
      flyout.setText("Flyout");
    }
    {
      Text client = new Text(flyoutControlComposite.getClientParent(), SWT.BORDER);
      client.setText("Client");
    }
    //
    tabItem.setControl(flyoutControlComposite);
    return flyoutControlComposite;
  }
}
