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
package org.eclipse.wb.internal.core.xml.model.clipboard;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Extension for {@link Property} that can provide object for copy/paste operation.
 * 
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public interface IClipboardObjectProperty {
  /**
   * @return the {@link Object} that can be used later to set same value, or
   *         {@link Property#UNKNOWN_VALUE} if no such object can be provided.
   */
  Object getClipboardObject() throws Exception;

  /**
   * Applies {@link Object} during paste and attempt to set {@link Property} corresponding value.
   */
  void setClipboardObject(Object value) throws Exception;
}
