/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.gefTree;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.gefTree.part.ArrayObjectEditPart;
import org.eclipse.wb.internal.core.gefTree.part.FlowContainerGroupEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.FlowContainerGroupInfo;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.gef.EditPart;

/**
 * Generic implementation of {@link IEditPartFactory} for {@link TreeViewer} that redirects
 * {@link EditPart}'s creation to {@link IEditPartFactory}'s from extension point.
 *
 * @author scheglov_ke
 * @coverage core.gefTree
 */
public final class EditPartFactory2 implements IEditPartFactory {
	public static final IEditPartFactory INSTANCE = new EditPartFactory2();

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
	@Override
	public org.eclipse.wb.gef.core.EditPart createEditPart(EditPart context, Object model) {
		if (model == null) {
			return null;
		}
		// flow container group
		if (model instanceof FlowContainerGroupInfo groupInfo) {
			return new FlowContainerGroupEditPart(groupInfo);
		}
		// child array
		if (model instanceof AbstractArrayObjectInfo arrayInfo) {
			return new ArrayObjectEditPart(arrayInfo);
		}
		// use default EditPart for JavaInfo and ObjectInfo
		if (model instanceof JavaInfo) {
			return new JavaEditPart((JavaInfo) model);
		}
		// no EditPart found
		return null;
	}

	private EditPart createEditPartPure(EditPart context, Object model) {
		// menu
		//    if (model instanceof ObjectInfo) {
		//      ObjectInfo objectInfo = (ObjectInfo) model;
		//      {
		//        IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(model);
		//        if (menuObject != null) {
		//          return new MenuEditPart(objectInfo, menuObject);
		//        }
		//      }
		//      {
		//        IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(model);
		//        if (itemObject != null) {
		//          return new MenuItemEditPart(objectInfo, itemObject);
		//        }
		//      }
		//    }
		// check each external factory
		//    for (IEditPartFactory factory : getFactories()) {
		//      EditPart editPart = factory.createEditPart(context, model);
		//      if (editPart != null) {
		//        return editPart;
		//      }
		//    }
		// flow container group
		if (model instanceof FlowContainerGroupInfo groupInfo) {
			return new FlowContainerGroupEditPart(groupInfo);
		}
		// child array
		if (model instanceof AbstractArrayObjectInfo arrayInfo) {
			return new ArrayObjectEditPart(arrayInfo);
		}
		// use default EditPart for JavaInfo and ObjectInfo
		if (model instanceof JavaInfo) {
			return new JavaEditPart((JavaInfo) model);
		}
		//    if (model instanceof ObjectInfo) {
		//      return new ObjectEditPart((ObjectInfo) model);
		//    }
		// no EditPart found
		return null;
	}
	//  ////////////////////////////////////////////////////////////////////////////
	//  //
	//  // Extensions
	//  //
	//  ////////////////////////////////////////////////////////////////////////////
	//  /**
	//   * @return the instances of {@link IEditPartFactory}.
	//   */
	//  private static List<IEditPartFactory> getFactories() {
	//    return ExternalFactoriesHelper.getElementsInstances(
	//        IEditPartFactory.class,
	//        "org.eclipse.wb.core.treeEditPartFactories",
	//        "factory");
	//  }
	//
	//  /**
	//   * Configures given {@link EditPart} using externally contributed {@link IEditPartConfigurator}'s.
	//   */
	//  private static void configureEditPart(EditPart context, EditPart editPart) {
	//    List<IEditPartConfigurator> configurators =
	//        ExternalFactoriesHelper.getElementsInstances(
	//            IEditPartConfigurator.class,
	//            "org.eclipse.wb.core.treeEditPartConfigurators",
	//            "configurator");
	//    for (IEditPartConfigurator configurator : configurators) {
	//      configurator.configure(context, editPart);
	//    }
	//  }
}
