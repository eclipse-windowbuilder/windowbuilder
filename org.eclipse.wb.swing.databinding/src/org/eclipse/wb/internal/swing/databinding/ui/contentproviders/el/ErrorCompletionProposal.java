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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.wb.internal.swing.databinding.Activator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * {@link ICompletionProposal} for display errors.
 * 
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ErrorCompletionProposal implements ICompletionProposal {
  private final String m_message;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ErrorCompletionProposal(Throwable e) {
    m_message = e.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICompletionProposal
  //
  ////////////////////////////////////////////////////////////////////////////
  public void apply(IDocument document) {
  }

  public String getDisplayString() {
    return m_message;
  }

  public Image getImage() {
    return Activator.getImage("errors.gif");
  }

  public Point getSelection(IDocument document) {
    return null;
  }

  public String getAdditionalProposalInfo() {
    return null;
  }

  public IContextInformation getContextInformation() {
    return null;
  }
}