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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.jface.text.IDocument;

/**
 * Strategy for refresh after {@link IDocument} change in {@link UndoManager}.
 * 
 * @author scheglov_ke
 * @coverage XML.editor
 */
public interface IRefreshStrategy {
  /**
   * @return <code>true</code> if UI should be refreshed immediately after change.
   */
  boolean shouldImmediately();

  /**
   * @return <code>true</code> if UI should be refreshed after {@link #getDelay()} ms.
   */
  boolean shouldWithDelay();

  /**
   * @return <code>true</code> if UI should be refreshed on editor save.
   */
  boolean shouldOnSave();

  /**
   * @return the delay in milliseconds for refreshing UI.
   */
  int getDelay();

  IRefreshStrategy IMMEDIATELY = new IRefreshStrategy() {
    public boolean shouldImmediately() {
      return true;
    }

    public boolean shouldWithDelay() {
      return false;
    }

    public boolean shouldOnSave() {
      return false;
    }

    public int getDelay() {
      return 0;
    }
  };
}