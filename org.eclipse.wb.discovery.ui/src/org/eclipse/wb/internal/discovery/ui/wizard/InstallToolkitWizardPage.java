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
package org.eclipse.wb.internal.discovery.ui.wizard;

import org.eclipse.wb.internal.discovery.core.WBToolkit;
import org.eclipse.wb.internal.discovery.ui.Messages;
import org.eclipse.wb.internal.discovery.ui.WBDiscoveryUiPlugin;
import org.eclipse.wb.internal.discovery.ui.util.BrowserHelper;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import java.text.MessageFormat;

/**
 * The wizard page used by the InstallToolkitWizard wizard.
 */
class InstallToolkitWizardPage extends WizardPage {
  private WBToolkit toolkit;

  /**
   * Create a new instance of a InstallToolkitWizardPage.
   *
   * @param toolkit
   *          the WindowBuilder toolkit to install
   */
  protected InstallToolkitWizardPage(WBToolkit toolkit) {
    super(Messages.InstallToolkitWizardPage_name);
    this.toolkit = toolkit;
    setTitle(MessageFormat.format(Messages.InstallToolkitWizardPage_title, toolkit.getName()));
    setMessage(WizardToolkitUtils.getDescription(toolkit));
    setImageDescriptor(WBDiscoveryUiPlugin.getBundledImageDescriptor("icons/new_wiz.png"));
  }

  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).extendedMargins(0, 0, 30, 0).applyTo(composite);
    Label iconLabel = new Label(composite, SWT.NULL);
    GridDataFactory.swtDefaults().span(1, 4).align(SWT.CENTER, SWT.BEGINNING).applyTo(iconLabel);
    if (toolkit.getIconURL() != null) {
      ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(toolkit.getIconURL());
      if (imageDescriptor != null) {
        iconLabel.setImage(WBDiscoveryUiPlugin.getImage(imageDescriptor));
      }
    }
    Label summaryLabel = new Label(composite, SWT.WRAP);
    String description = toolkit.getDescription();
    description = description.replaceAll("(\\r\\n)|\\n|\\r", " ");
    summaryLabel.setText(description);
    GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(summaryLabel);
    if (toolkit.getMoreInfoURL() != null) {
      Link moreInfoLink = new Link(composite, SWT.NONE);
      moreInfoLink.setText(Messages.InstallToolkitWizardPage_moreInfoLink);
      moreInfoLink.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          BrowserHelper.openUrl(getShell(), toolkit.getMoreInfoURL());
        }
      });
      GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(moreInfoLink);
    } else {
      // spacer
      new Label(composite, SWT.NONE);
    }
    Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
    GridDataFactory.fillDefaults().grab(true, false).hint(100, 20).applyTo(separator);
    Label label = new Label(composite, SWT.CENTER);
    label.setText(Messages.InstallToolkitWizardPage_finishLabel);
    GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(label);
    setControl(composite);
  }
}
