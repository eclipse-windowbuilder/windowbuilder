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
package org.eclipse.wb.internal.core.editor.structure.property;

import org.eclipse.wb.internal.core.model.property.Property;

import org.eclipse.jface.action.IMenuManager;

/**
 * Contributes entries into {@link Property} context menu.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public interface IPropertiesMenuContributor {
  String GROUP_BASE = "org.eclipse.wb.component-properties.group.";
  String GROUP_TOP = GROUP_BASE + "top";
  String GROUP_EDIT = GROUP_BASE + "edit";
  String GROUP_PRIORITY = GROUP_BASE + "priority";
  String GROUP_ADDITIONAL = GROUP_BASE + "additional";

  void contributeMenu(IMenuManager manager, Property property) throws Exception;
}
