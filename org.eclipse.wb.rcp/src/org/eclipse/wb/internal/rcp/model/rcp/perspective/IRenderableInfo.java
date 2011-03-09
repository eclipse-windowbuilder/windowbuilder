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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

/**
 * Model for object that can be rendered from {@link PageLayoutAddCreationSupport}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public interface IRenderableInfo {
  /**
   * Renders this {@link IRenderableInfo} by creating some {@link Object}.
   * 
   * @return the {@link Object} that represents this {@link IRenderableInfo}.
   */
  Object render() throws Exception;
}
