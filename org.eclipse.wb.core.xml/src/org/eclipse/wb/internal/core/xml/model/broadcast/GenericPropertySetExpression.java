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
package org.eclipse.wb.internal.core.xml.model.broadcast;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;

/**
 * Listener for {@link GenericProperty} events.
 * <p>
 * Subscribers can use this method to update/validate expression during
 * {@link GenericPropertyImpl#setExpression(String, Object)}.
 * 
 * @author scheglov_ke
 * @coverage XML.model
 */
public interface GenericPropertySetExpression {
  /**
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   * @param expression
   *          the single element array with new expression.
   * @param value
   *          the single element array with new value, may be {@link Property#UNKNOWN_VALUE}.
   * @param shouldSet
   *          the single element array that specifies if expression can be set, subscriber may set
   *          it to <code>false</code> if it did required modification itself, or thinks that value
   *          of this property should not be modified at all.
   */
  void invoke(GenericPropertyImpl property, String[] expression, Object[] value, boolean[] shouldSet)
      throws Exception;
}
