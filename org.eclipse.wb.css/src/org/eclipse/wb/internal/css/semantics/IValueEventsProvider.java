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
package org.eclipse.wb.internal.css.semantics;

/**
 * Implementations of this interface supports adding/removing {@link IValueListener}'s.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public interface IValueEventsProvider {
  void addListener(IValueListener listener);

  void removeListener(IValueListener listener);

  void clear();
}
