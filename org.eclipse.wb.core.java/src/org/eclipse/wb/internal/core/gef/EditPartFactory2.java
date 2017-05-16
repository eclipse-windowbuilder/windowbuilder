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
package org.eclipse.wb.internal.core.gef;

import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.gef.part.AbstractWrapperEditPart;
import org.eclipse.wb.internal.core.gef.part.DesignRootEditPart;
import org.eclipse.wb.internal.core.gef.part.nonvisual.ArrayObjectEditPart;
import org.eclipse.wb.internal.core.model.DesignRootObject;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

/**
 * Generic implementation of {@link IEditPartFactory} that redirects {@link EditPart}'s creation to
 * {@link IEditPartFactory}'s from extension point.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class EditPartFactory2 implements IEditPartFactory {
  public static final EditPartFactory2 INSTANCE = new EditPartFactory2();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditPartFactory2() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    if (model == null) {
      return null;
    }
    // designer root
    if (model instanceof DesignRootObject) {
      DesignRootObject designRootObject = (DesignRootObject) model;
      return new DesignRootEditPart(designRootObject);
    }
    // child array
    if (model instanceof AbstractArrayObjectInfo) {
      AbstractArrayObjectInfo arrayInfo = (AbstractArrayObjectInfo) model;
      ArrayObjectEditPart editPart = new ArrayObjectEditPart(arrayInfo);
      EditPartFactory.configureEditPart(context, editPart);
      return editPart;
    }
    // IWrapperInfo
    if (model instanceof IWrapperInfo) {
      IWrapper wrapper = ((IWrapperInfo) model).getWrapper();
      AbstractWrapperEditPart editPart = new AbstractWrapperEditPart(wrapper);
      EditPartFactory.configureEditPart(context, editPart);
      return editPart;
    }
    // no EditPart found
    return null;
  }
}
