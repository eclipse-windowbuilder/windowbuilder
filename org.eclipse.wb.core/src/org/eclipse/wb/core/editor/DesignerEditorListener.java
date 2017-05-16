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
package org.eclipse.wb.core.editor;

/**
 * Listener for {@link IDesignerEditor} events.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface DesignerEditorListener {
  /**
   * Notifies that reparse was performed in this {@link IDesignerEditor}.
   */
  void reparsed();
}