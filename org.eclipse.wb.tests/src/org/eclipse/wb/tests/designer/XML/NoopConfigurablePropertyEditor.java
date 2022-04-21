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
package org.eclipse.wb.tests.designer.XML;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;

import java.util.Map;

/**
 * No-op configurable property editor. Does nothing :-)
 *
 * @author mitin_aa
 */
public class NoopConfigurablePropertyEditor extends TextDisplayPropertyEditor
    implements
      IConfigurablePropertyObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void configure(EditorContext context, Map<String, Object> parameters) throws Exception {
  }
}
