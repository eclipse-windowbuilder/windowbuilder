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
package org.eclipse.wb.internal.core.utils.state;

import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.PasteRequest;

import java.util.List;

/**
 * Helper for paste operation.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.model
 */
public interface IPasteRequestProcessor {
  /**
   * This method creates the components with their mementos in given <code>pasteRequest</code> then
   * returns the {@link Command} which applies the mementos in it's execute method.
   *
   * @param pasteRequest
   *          the {@link PasteRequest} instance given by policy.
   * @param componentProcessor
   *          provides the ability to do a special processing of the components while applying the
   *          mementos.
   *
   * @return the {@link Command} which applies the mementos in it's execute method or
   *         <code>null</code> if no components can be pasted or error occurred.
   */
  Command getPasteCommand(PasteRequest pasteRequest, IPasteComponentProcessor componentProcessor);

  /**
   * This method creates the components with their mementos in given <code>pasteRequest</code> and
   * then returns them as list. Did not do any memento applying.
   *
   * @param pasteRequest
   *          the {@link PasteRequest} instance given by policy.
   *
   * @return the {@link List} of the created components or empty list if no components to paste or
   *         error occurred.
   */
  List<IObjectInfo> getPastingComponents(PasteRequest pasteRequest);
}
