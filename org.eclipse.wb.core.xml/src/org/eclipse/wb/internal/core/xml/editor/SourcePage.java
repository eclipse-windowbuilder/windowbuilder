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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.wb.internal.core.xml.Activator;
import org.eclipse.wb.internal.core.xml.Messages;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * "Source" page of {@link AbstractXmlEditor}.
 * 
 * @author lobas_av
 * @coverage XML.editor
 */
public final class SourcePage extends XmlEditorPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public Control createControl(Composite parent) {
    return null;
  }

  public Control getControl() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return Messages.SourcePage_name;
  }

  public Image getImage() {
    return Activator.getImage("editor_page_xml.png");
  }
}