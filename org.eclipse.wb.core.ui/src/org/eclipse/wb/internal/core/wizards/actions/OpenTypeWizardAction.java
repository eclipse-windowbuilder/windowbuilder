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
package org.eclipse.wb.internal.core.wizards.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

/**
 * {@link Action} for for running {@link Wizard}
 *
 * @author Dan Rubel
 * @coverage core.wizards.ui
 */
public class OpenTypeWizardAction extends AbstractOpenWizardAction {
  private static final String ATT_NAME = "name";
  private static final String ATT_ICON = "icon";
  private static final String ATT_CLASS = "class";
  private static final String TAG_DESCRIPTION = "description";
  private final IConfigurationElement fConfigurationElement;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public OpenTypeWizardAction(IConfigurationElement element) {
    fConfigurationElement = element;
    setText(element.getAttribute(ATT_NAME));
    setImageDescriptor(getIconFromConfig(fConfigurationElement));
    {
      String description = getDescriptionFromConfig(fConfigurationElement);
      setDescription(description);
      setToolTipText(description);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private String getDescriptionFromConfig(IConfigurationElement config) {
    IConfigurationElement[] children = config.getChildren(TAG_DESCRIPTION);
    if (children.length >= 1) {
      return children[0].getValue();
    }
    return "";
  }

  private ImageDescriptor getIconFromConfig(IConfigurationElement config) {
    try {
      return ExternalFactoriesHelper.getImageDescriptor(config, ATT_ICON);
    } catch (Throwable e) {
      DesignerPlugin.log("Unable to load wizard icon", e);
    }
    return ImageDescriptor.getMissingImageDescriptor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Wizard createWizard() {
    try {
      return (Wizard) fConfigurationElement.createExecutableExtension(ATT_CLASS);
    } catch (Exception e) {
      return null;
    }
  }
}
