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
package org.eclipse.wb.internal.rcp.wizards.rcp.pagebook;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link WizardPage} that creates new RCP {@link PageBook}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class PageBookWizardPage extends RcpWizardPage {
  private final List<Button> m_buttons = new ArrayList<Button>();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageBookWizardPage() {
    setTitle(WizardsMessages.PageBookWizardPage_title);
    setImageDescriptor(Activator.getImageDescriptor("wizard/PageBook/banner.gif"));
    setDescription(WizardsMessages.PageBookWizardPage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    final String[] template = new String[1];
    getShell().getDisplay().syncExec(new Runnable() {
      public void run() {
        for (Button button : m_buttons) {
          if (button.getSelection()) {
            template[0] = (String) button.getData();
            break;
          }
        }
      }
    });
    InputStream file = Activator.getFile("templates/rcp/" + template[0]);
    fillTypeFromTemplate(newType, imports, monitor, file);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int columns) {
    // create main container
    Composite baseComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(baseComposite).noMargins();
    GridDataFactory.create(baseComposite).fillH().grabH().spanH(columns);
    // create message label
    Label label = new Label(baseComposite, SWT.NONE);
    label.setText(WizardsMessages.PageBookWizardPage_basePageSelection);
    // create all buttons
    createButton(
        m_buttons,
        baseComposite,
        "Page",
        "class MyPage extends org.eclipse.ui.part.Page",
        true,
        "org.eclipse.ui.part.Page",
        null,
        "Page.jvt");
    createButton(
        m_buttons,
        baseComposite,
        "Page with IContentOutlinePage",
        "class MyPage extends Page implements IContentOutlinePage",
        false,
        "org.eclipse.ui.part.Page",
        "org.eclipse.ui.views.contentoutline.IContentOutlinePage",
        "PageIContentOutlinePage.jvt");
    createButton(
        m_buttons,
        baseComposite,
        "Page with IPropertySheetPage",
        "class MyPage extends Page implements IPropertySheetPage",
        false,
        "org.eclipse.ui.part.Page",
        "org.eclipse.ui.views.properties.IPropertySheetPage",
        "PageIPropertySheetPage.jvt");
  }

  private void createButton(List<Button> buttons,
      Composite parent,
      String text,
      String tooltip,
      boolean selection,
      final String superClass,
      final String superInterface,
      String template) {
    // create button
    final Button button = new Button(parent, SWT.RADIO);
    GridDataFactory.create(button).indentH(24);
    button.setText(text);
    button.setToolTipText(tooltip);
    button.setSelection(selection);
    button.setData(template);
    // create listener
    SelectionListener listener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (button.getSelection()) {
          if (superClass == null) {
            setSuperClass("java.lang.Object", true);
          } else {
            setSuperClass(superClass, true);
          }
          if (superInterface == null) {
            setSuperInterfaces(Collections.EMPTY_LIST, false);
          } else {
            List<String> interfaces = new ArrayList<String>();
            interfaces.add(superInterface);
            setSuperInterfaces(interfaces, false);
          }
        }
      }
    };
    button.addSelectionListener(listener);
    if (selection) {
      listener.widgetSelected(null);
    }
    // add to buttons
    buttons.add(button);
  }
}