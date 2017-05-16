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
package org.eclipse.wb.internal.core.editor.structure;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Adapter for {@link IPartListener}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class PartListenerAdapter implements IPartListener {
  public void partActivated(IWorkbenchPart part) {
  }

  public void partBroughtToTop(IWorkbenchPart part) {
  }

  public void partClosed(IWorkbenchPart part) {
  }

  public void partDeactivated(IWorkbenchPart part) {
  }

  public void partOpened(IWorkbenchPart part) {
  }
}
