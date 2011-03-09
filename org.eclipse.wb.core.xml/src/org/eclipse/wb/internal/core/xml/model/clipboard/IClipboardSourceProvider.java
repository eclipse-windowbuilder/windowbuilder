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

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;

/**
 * Extension for {@link PropertyEditor} that can provide XML source for copy/paste operation.
 * 
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public interface IClipboardSourceProvider {
  /**
   * @return the Java source that that has same value as current value of given
   *         {@link GenericProperty}, or <code>null</code> if no such source can be provided.
   */
  String getClipboardSource(GenericProperty property) throws Exception;
}
