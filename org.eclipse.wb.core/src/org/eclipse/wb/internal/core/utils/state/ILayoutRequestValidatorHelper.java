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
package org.eclipse.wb.internal.core.utils.state;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;

/**
 * Helper for {@link ILayoutRequestValidator}s.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ILayoutRequestValidatorHelper {
  /**
   * @return <code>true</code> if given object is component model, so for example
   *         {@link #getDescription(Object)} can be used for it.
   */
  boolean isComponent(Object object);
  /**
   * @return the {@link IComponentDescription} for given memento.
   */
  IComponentDescription getPasteComponentDescription(Object memento) throws Exception;
  /**
   * @return the component for given memento.
   */
  Object getPasteComponent(Object memento) throws Exception;
  /**
   * @return <code>true</code> if "child" can be created on given "parent".
   */
  boolean canUseParentForChild(Object parent, Object child) throws Exception;
  /**
   * @return <code>true</code> if given {@link Object} is component and can be used as reference.
   */
  boolean canReference(Object object);
  /**
   * @return <code>true</code> if given {@link ObjectInfo} can be moved inside of its parent.
   */
  boolean canReorder(ObjectInfo component);
  /**
   * @return <code>true</code> if given {@link ObjectInfo} can be moved on different parent.
   */
  boolean canReparent(ObjectInfo component);
}
