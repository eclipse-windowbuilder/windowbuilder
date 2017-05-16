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
package org.eclipse.wb.internal.core.databinding.ui.editor;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;

/**
 * Interface for displaying messages.
 *
 * We use it to separate GUI that edits anything and needs to display messages from concrete message
 * containers, such as {@link TitleAreaDialog} or {@link WizardPage}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public interface IPageListener {
  /**
   * Sets this dialog/wizard page's title.
   */
  void setTitle(String title);

  /**
   * Sets this dialog/wizard page's {@link Image}.
   */
  void setTitleImage(Image image);

  /**
   * Sets or clears the message for this page.
   */
  void setMessage(String newMessage);

  /**
   * Sets or clears the error message for this page.
   */
  void setErrorMessage(String newMessage);

  /**
   * Sets whether this page is complete.
   */
  void setPageComplete(boolean complete);
}