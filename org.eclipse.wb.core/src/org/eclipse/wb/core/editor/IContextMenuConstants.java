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
package org.eclipse.wb.core.editor;

/**
 * Constants for context groups.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IContextMenuConstants {
  String GROUP_BASE = "org.eclipse.wb.popup.group.";
  String GROUP_TOP = GROUP_BASE + "top";
  String GROUP_EDIT = GROUP_BASE + "edit";
  String GROUP_EDIT2 = GROUP_BASE + "edit2";
  String GROUP_EVENTS = GROUP_BASE + "events";
  String GROUP_EVENTS2 = GROUP_BASE + "events2";
  String GROUP_LAYOUT = GROUP_BASE + "layout";
  String GROUP_CONSTRAINTS = GROUP_BASE + "constraints";
  String GROUP_INHERITANCE = GROUP_BASE + "inheritance";
  String GROUP_ADDITIONAL = GROUP_BASE + "additional";
}
