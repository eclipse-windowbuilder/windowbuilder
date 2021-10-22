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

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * {@link ICompletionProposal} for display {@code Java Bean} properties.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class CompletionProposal implements ICompletionProposal {
  private final String m_displayText;
  private final Image m_image;
  private final int m_offset;
  private final String m_data;
  private final Point m_selection;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompletionProposal(String displayText, Image image, int offset, String data, int selection) {
    m_displayText = displayText;
    m_image = image;
    m_offset = offset;
    m_data = data;
    m_selection = new Point(selection, 0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICompletionProposal
  //
  ////////////////////////////////////////////////////////////////////////////
  public void apply(IDocument document) {
    try {
      document.replace(m_offset, 0, m_data);
    } catch (BadLocationException e) {
      DesignerPlugin.log(e);
    }
  }

  public String getDisplayString() {
    return m_displayText;
  }

  public Image getImage() {
    return m_image;
  }

  public Point getSelection(IDocument document) {
    return m_selection;
  }

  public String getAdditionalProposalInfo() {
    return null;
  }

  public IContextInformation getContextInformation() {
    return null;
  }
}