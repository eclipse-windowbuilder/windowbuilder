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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;

import org.eclipse.swt.widgets.Widget;

/**
 * Model for any {@link Widget} in XWT.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class WidgetInfo extends AbstractComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    StylePropertyEditor.addStyleProperty(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Widget} object.
   */
  public final Widget getWidget() {
    return (Widget) getObject();
  }

  /**
   * @return the style of {@link Widget} object.
   */
  public final int getStyle() {
    if (getWidget() != null) {
      return getWidget().getStyle();
    } else {
      return getLiveComponentsManager().getStyle();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Live" support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instance of {@link XwtLiveManager} to fetch "live" data.
   */
  protected XwtLiveManager getLiveComponentsManager() {
    return new XwtLiveManager(this);
  }
}
