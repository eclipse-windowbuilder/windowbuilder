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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoDeactivePropertyEditor {
  /**
   * Specifies that if there is some {@link PropertyEditor} active, then it should be deactivated.
   */
  void invoke() throws Exception;
}
