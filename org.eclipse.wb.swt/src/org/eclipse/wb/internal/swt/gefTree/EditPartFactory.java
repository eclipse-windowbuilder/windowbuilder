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
package org.eclipse.wb.internal.swt.gefTree;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.gef.MatchingEditPartFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;

/**
 * Implementation of {@link IEditPartFactory} for SWT.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
  private final static IEditPartFactory MATCHING_FACTORY =
      new MatchingEditPartFactory(ImmutableList.of("org.eclipse.wb.internal.swt.model.widgets"),
          ImmutableList.of("org.eclipse.wb.internal.swt.gefTree.part"));

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // most EditPart's can be created using matching
    return MATCHING_FACTORY.createEditPart(context, model);
  }
}