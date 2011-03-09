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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.IAdaptable;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

import java.util.List;

/**
 * Abstract interface for GroupLayout support.
 * 
 * @author mitin_aa
 */
public interface IGroupLayoutInfo extends IAdaptable {
  LayoutDesigner getLayoutDesigner();

  LayoutModel getLayoutModel();

  AbstractComponentInfo getLayoutContainer();

  Insets getContainerInsets();

  boolean isRelatedComponent(ObjectInfo component);

  LayoutComponent createLayoutComponent(AbstractComponentInfo model);

  void saveLayout() throws Exception;

  //
  void command_commit() throws Exception;

  void command_paste(List<JavaInfoMemento> mementos) throws Exception;

  void command_create(AbstractComponentInfo newObject) throws Exception;

  void command_add(List<AbstractComponentInfo> models) throws Exception;
}
