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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;

/**
 * Such components as {@link Dialog}, {@link ApplicationWindow}, etc are not visual itself, however
 * have special method that initiates GUI creation, for example {@link Window#create()}.
 * <p>
 * Steps for full rendering are following:
 * <ol>
 * <li>Create component instance using constructor.</li>
 * <li>Create component GUI using special method invoked from {@link #render()}.</li>
 * </ol>
 * <p>
 * Interface {@link IJavaInfoRendering} is general presentation for rendering such components.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public interface IJavaInfoRendering {
  /**
   * Renders {@link JavaInfo} GUI.
   */
  void render() throws Exception;
}
