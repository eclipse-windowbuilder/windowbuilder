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
package org.eclipse.wb.internal.core.utils.dialogfields;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Interface for displaying messages.
 *
 * We use it to separate GUI that edits anything and needs to display messages from concrete message
 * containers, such as {@link TitleAreaDialog} or {@link WizardPage}.
 *
 * @author scheglov_ke
 */
public interface IMessageContainer {
  void setErrorMessage(String message);

  /**
   * Helper class for creating {@link IMessageContainer} for standard GUI objects.
   */
  public static class Util {
    /**
     * Creates {@link IMessageContainer} for {@link WizardPage}.
     */
    public static IMessageContainer forWizardPage(final WizardPage wizardPage) {
      return new IMessageContainer() {
        public void setErrorMessage(String message) {
          wizardPage.setErrorMessage(message);
        }
      };
    }

    /**
     * Creates {@link IMessageContainer} for {@link TitleAreaDialog}.
     */
    public static IMessageContainer forTitleAreaDialog(final TitleAreaDialog titleAreaDialog) {
      return new IMessageContainer() {
        public void setErrorMessage(String message) {
          titleAreaDialog.setErrorMessage(message);
        }
      };
    }
  }
}
