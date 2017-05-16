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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.swt.graphics.Image;

/**
 * Interface for component description.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface IComponentDescription {
  /**
   * @return the {@link ToolkitDescription} for this component.
   */
  ToolkitDescription getToolkit();

  /**
   * @return the {@link Class} of component for which this description is.
   */
  Class<?> getComponentClass();

  /**
   * @return the {@link Class} of model that should be used for this component.
   */
  Class<?> getModelClass();

  /**
   * @return the icon for this component.
   */
  Image getIcon();
}
