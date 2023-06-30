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
package org.eclipse.wb.internal.core.gefTree;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.core.gefTree.part.ArrayObjectEditPart;
import org.eclipse.wb.internal.core.gefTree.part.FlowContainerGroupEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.FlowContainerGroupInfo;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

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
	public EditPart createEditPart(EditPart context, Object model) {
		if (model == null) {
			return null;
		}
		// flow container group
		if (model instanceof FlowContainerGroupInfo) {
			FlowContainerGroupInfo groupInfo = (FlowContainerGroupInfo) model;
			return new FlowContainerGroupEditPart(groupInfo);
		}
		// child array
		if (model instanceof AbstractArrayObjectInfo) {
			AbstractArrayObjectInfo arrayInfo = (AbstractArrayObjectInfo) model;
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
		if (model instanceof FlowContainerGroupInfo) {
			FlowContainerGroupInfo groupInfo = (FlowContainerGroupInfo) model;
			return new FlowContainerGroupEditPart(groupInfo);
		}
		// child array
		if (model instanceof AbstractArrayObjectInfo) {
			AbstractArrayObjectInfo arrayInfo = (AbstractArrayObjectInfo) model;
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
