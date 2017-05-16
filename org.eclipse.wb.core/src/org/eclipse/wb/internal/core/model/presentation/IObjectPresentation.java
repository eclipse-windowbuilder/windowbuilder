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
package org.eclipse.wb.internal.core.model.presentation;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Interface for visual presentation of {@link ObjectInfo} - title, icon, etc.
 *
 * @author scheglov_ke
 * @coverage core.model.presentation
 */
public interface IObjectPresentation {
  /**
   * @return <code>true</code> if object should be displayed for user. For example we can display
   *         some containers only if there is at least one child.
   */
  boolean isVisible() throws Exception;

  /**
   * @return the text to display for user. This can be some static text, variable name, etc.
   */
  String getText() throws Exception;

  /**
   * @return the icon to display for user.
   */
  Image getIcon() throws Exception;

  /**
   * @return the list of {@link ObjectInfo} children to display for user in components tree. This
   *         list can be different than {@link ObjectInfo#getChildren()}.
   */
  List<ObjectInfo> getChildrenTree() throws Exception;

  /**
   * @return the list of {@link ObjectInfo} children to display for user on design canvas. This list
   *         can be different than {@link ObjectInfo#getChildren()}.
   */
  List<ObjectInfo> getChildrenGraphical() throws Exception;
}
