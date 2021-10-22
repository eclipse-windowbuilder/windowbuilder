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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Model for any XWT "LayoutData".
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public class LayoutDataInfo extends XmlObjectInfo implements ILayoutDataInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutDataInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
    contributeLayoutDataProperty_toControl();
    deleteIfDefault();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isVirtual() {
    return getCreationSupport() instanceof VirtualLayoutDataCreationSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IObjectPresentation getPresentation() {
    return new LayoutDataPresentation(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contribute "LayoutData" complex property to parent {@link ControlInfo}.
   */
  private void contributeLayoutDataProperty_toControl() {
    addBroadcastListener(new XmlObjectAddProperties() {
      public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
        if (isActiveForControl(object)) {
          addLayoutDataProperty(properties);
        }
      }

      private boolean isActiveForControl(XmlObjectInfo control) {
        return control.getChildren().contains(LayoutDataInfo.this);
      }
    });
  }

  private void deleteIfDefault() {
    addBroadcastListener(new ObjectEventListener() {
      @Override
      public void endEdit_aboutToRefresh() throws Exception {
        if (!isDeleted()
            && getCreationSupport() instanceof ElementCreationSupport
            && getElement().getDocumentAttributes().isEmpty()
            && getElement().getChildren().isEmpty()) {
          delete();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "LayoutData" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_property;

  /**
   * Adds properties of this {@link LayoutDataInfo} to the properties of its {@link ControlInfo}.
   */
  private void addLayoutDataProperty(List<Property> properties) throws Exception {
    // prepare complex property
    if (m_property == null) {
      String text;
      {
        Class<?> componentClass = getDescription().getComponentClass();
        text = "(" + componentClass.getName() + ")";
      }
      // prepare ComplexProperty
      m_property = new ComplexProperty("LayoutData", text) {
        @Override
        public boolean isModified() throws Exception {
          return !isVirtual();
        }

        @Override
        public void setValue(Object value) throws Exception {
          if (value == UNKNOWN_VALUE) {
            delete();
          }
        }
      };
      m_property.setCategory(PropertyCategory.system(5));
      // set sub-properties
      m_property.setProperties(getProperties());
    }
    // add property
    properties.add(m_property);
  }
}
