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
package org.eclipse.wb.internal.ercp.model.widgets.mobile;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;
import java.util.ListIterator;

/**
 * Model for eSWT {@link org.eclipse.ercp.swt.mobile.ListBoxItem}.
 * 
 * @author lobas_av
 * @coverage ercp.model.widgets.mobile
 */
public final class ListBoxItemInfo extends AbstractComponentInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListBoxItemInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    for (ListIterator<Property> I = properties.listIterator(); I.hasNext();) {
      Property property = I.next();
      // check "constructor" property
      if ("Constructor".equals(property.getTitle()) && property instanceof ComplexProperty) {
        ComplexProperty constructor = (ComplexProperty) property;
        Property[] subProperties = constructor.getProperties();
        // handle "detailIcon" property
        addPropertyToTopLevel(I, subProperties, 1);
        // handle "headingIcon" property
        addPropertyToTopLevel(I, subProperties, 3);
        break;
      }
    }
    return properties;
  }

  /**
   * Utils method create copy of <code>sub-property</code> with given <code>index</code> and add to
   * <code>properties</code>.
   */
  private static void addPropertyToTopLevel(ListIterator<Property> properties,
      Property[] subProperties,
      int index) throws Exception {
    GenericPropertyImpl subProperty = (GenericPropertyImpl) subProperties[index];
    GenericPropertyImpl topProperty = new GenericPropertyImpl(subProperty, subProperty.getTitle());
    topProperty.setCategory(PropertyCategory.NORMAL);
    properties.add(topProperty);
  }
}