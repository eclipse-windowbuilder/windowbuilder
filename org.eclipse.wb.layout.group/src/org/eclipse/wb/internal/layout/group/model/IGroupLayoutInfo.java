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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Insets;

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
