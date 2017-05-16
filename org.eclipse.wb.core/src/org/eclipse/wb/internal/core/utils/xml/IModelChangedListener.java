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
package org.eclipse.wb.internal.core.utils.xml;

/**
 * Classes that need to be notified on model changes should implement this interface and add
 * themselves as listeners to the model they want to listen to.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public interface IModelChangedListener {
  /**
   * Called when there is a change in the model this listener is registered with.
   *
   * @param event
   *          a change event that describes the kind of the model change
   */
  void modelChanged(ModelChangedEvent event);
}
