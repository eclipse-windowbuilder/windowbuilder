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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

/**
 * Standard "New Java Class" wizard page.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class AutomaticDatabindingFirstPage
    extends
      org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingFirstPage(IAutomaticDatabindingProvider databindingProvider,
      String initialBeanClassName) {
    super(databindingProvider, initialBeanClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Substitution support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String performSubstitutions(String code, ImportsManager imports) {
    code = super.performSubstitutions(code, imports);
    code = SwingWizardPage.doPerformSubstitutions(this, code, imports);
    return code;
  }

  @Override
  protected ToolkitDescriptionJava getToolkitDescription() {
    return ToolkitProvider.DESCRIPTION;
  }
}