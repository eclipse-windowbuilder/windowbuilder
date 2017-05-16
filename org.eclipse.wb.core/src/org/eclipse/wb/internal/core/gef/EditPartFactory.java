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

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.gef.part.menu.MenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuReference;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import java.util.List;

/**
 * Generic implementation of {@link IEditPartFactory} that redirects {@link EditPart}'s creation to
 * {@link IEditPartFactory}'s from extension point.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class EditPartFactory implements IEditPartFactory {
  public static final EditPartFactory INSTANCE = new EditPartFactory();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditPartFactory() {
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
    // TODO(scheglov)
//    if (model instanceof DesignRootObject) {
//      DesignRootObject designRootObject = (DesignRootObject) model;
//      return new DesignRootEditPart(designRootObject);
//    }
    // menu
    if (model instanceof MenuReference) {
      MenuReference reference = (MenuReference) model;
      MenuEditPart editPart = new MenuEditPart(reference, reference.getMenu());
      configureEditPart(context, editPart);
      return editPart;
    }
    // child array
    // TODO(scheglov)
//    if (model instanceof AbstractArrayObjectInfo) {
//      AbstractArrayObjectInfo arrayInfo = (AbstractArrayObjectInfo) model;
//      ArrayObjectEditPart editPart = new ArrayObjectEditPart(arrayInfo);
//      configureEditPart(context, editPart);
//      return editPart;
//    }
    // check each external factory
    for (IEditPartFactory factory : getFactories()) {
      EditPart editPart = factory.createEditPart(context, model);
      if (editPart != null) {
        configureEditPart(context, editPart);
        return editPart;
      }
    }
    // IWrapperInfo
    // TODO(scheglov)
//    if (model instanceof IWrapperInfo) {
//      IWrapper wrapper = ((IWrapperInfo) model).getWrapper();
//      AbstractWrapperEditPart editPart = new AbstractWrapperEditPart(wrapper);
//      configureEditPart(context, editPart);
//      return editPart;
//    }
    // no EditPart found
    return null;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Extensions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instances of {@link IEditPartFactory}.
   */
  private static List<IEditPartFactory> getFactories() {
    return ExternalFactoriesHelper.getElementsInstances(
        IEditPartFactory.class,
        "org.eclipse.wb.core.editPartFactories",
        "factory");
  }
  /**
   * Configures given {@link EditPart} using externally contributed {@link IEditPartConfigurator}'s.
   */
  public static void configureEditPart(EditPart context, EditPart editPart) {
    List<IEditPartConfigurator> configurators =
        ExternalFactoriesHelper.getElementsInstances(
            IEditPartConfigurator.class,
            "org.eclipse.wb.core.editPartConfigurators",
            "configurator");
    for (IEditPartConfigurator configurator : configurators) {
      configurator.configure(context, editPart);
    }
  }
}
