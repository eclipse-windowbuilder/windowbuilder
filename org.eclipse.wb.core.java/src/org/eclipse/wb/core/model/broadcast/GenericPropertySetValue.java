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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;

/**
 * Listener for {@link GenericProperty} events.
 * <p>
 * Subscribers can use this method to update/validate value during
 * {@link GenericPropertyImpl#setValue(Object)}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface GenericPropertySetValue {
  /**
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   * @param value
   *          the single element array with new value.
   * @param shouldSetValue
   *          the single element array that specifies if value can be set, subscriber may set it to
   *          <code>false</code> if it did required modification itself, or thinks that value of
   *          this property should not be modified at all.
   */
  void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
      throws Exception;
}
