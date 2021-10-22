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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.utils.MorphingSupport;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Widget;

import java.util.List;

/**
 * Model for any {@link Widget} in XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class WidgetInfo extends AbstractComponentInfo {
  private final WidgetInfo m_this = this;

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
    // contribute context menu
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object == m_this) {
          MorphingSupport.contribute("org.eclipse.swt.widgets.Widget", m_this, manager);
        }
      }
    });
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
