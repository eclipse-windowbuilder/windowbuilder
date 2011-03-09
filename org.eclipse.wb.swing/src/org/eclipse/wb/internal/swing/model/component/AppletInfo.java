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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.IJavaInfoRendering;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.applet.Applet;

import javax.swing.JApplet;

/**
 * Model for {@link Applet} and {@link JApplet}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class AppletInfo extends ContainerInfo implements IJavaInfoRendering {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AppletInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    JavaInfoUtils.scheduleSpecialRendering(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoRendering
  //
  ////////////////////////////////////////////////////////////////////////////
  public void render() throws Exception {
    Object applet = getObject();
    ReflectionUtils.invokeMethod(applet, "init()");
  }
}
