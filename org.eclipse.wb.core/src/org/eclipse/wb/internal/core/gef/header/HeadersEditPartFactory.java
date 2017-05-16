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
package org.eclipse.wb.internal.core.gef.header;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;

/**
 * Implementation of {@link IEditPartFactory} for headers.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class HeadersEditPartFactory implements IEditPartFactory {
  public EditPart createEditPart(EditPart context, Object model) {
    return (EditPart) model;
  }
}
