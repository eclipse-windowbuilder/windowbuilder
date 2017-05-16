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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;

/**
 * A {@link IRootContainer} is the <i>root</i> of an {@link IEditPartViewer}. It bridges the gap
 * between the {@link IEditPartViewer} and its contents. It does not correspond to anything in the
 * model, and typically can not be interacted with by the User. The Root provides a homogeneous
 * context for the applications "real" EditParts.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IRootContainer {
  /**
   * Returns the <i>content</i> {@link EditPart}.
   */
  EditPart getContent();

  /**
   * Sets the <i>content</i> {@link EditPart}. A IRootEditPart only has a single child, called its
   * <i>contents</i>.
   */
  void setContent(EditPart contentEditPart);
}