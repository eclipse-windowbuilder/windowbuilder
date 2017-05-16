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
package org.eclipse.wb.internal.core.databinding.ui.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;

import java.util.List;

/**
 * Complex property for observe properties.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public final class ObservePropertyEditor extends TextDialogPropertyEditor
    implements
      IComplexPropertyEditor {
  public static final ObservePropertyEditor EDITOR = new ObservePropertyEditor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<AbstractBindingProperty> updateProperties(Property property) throws Exception {
    AbstractObserveProperty observeProperty = (AbstractObserveProperty) property;
    List<AbstractBindingProperty> bindingProperties = observeProperty.getBindingProperties();
    //
    List<IBindingInfo> bindings = Lists.newArrayList();
    List<Boolean> isTargets = Lists.newArrayList();
    observeProperty.getBindings(bindings, isTargets);
    //
    int oldSize = bindingProperties.size();
    int newSize = bindings.size();
    int size = Math.min(oldSize, newSize);
    //
    for (int i = 0; i < size; i++) {
      bindingProperties.get(i).setBinding(bindings.get(i), isTargets.get(i));
    }
    if (oldSize > newSize) {
      for (int i = newSize; i < oldSize; i++) {
        bindingProperties.remove(newSize);
      }
    } else if (oldSize < newSize) {
      for (int i = oldSize; i < newSize; i++) {
        AbstractBindingProperty bindingProperty = observeProperty.createBindingProperty();
        bindingProperty.setBinding(bindings.get(i), isTargets.get(i));
        bindingProperties.add(bindingProperty);
      }
    }
    //
    return bindingProperties;
  }

  public Property[] getProperties(Property property) throws Exception {
    List<AbstractBindingProperty> properties = updateProperties(property);
    return properties.toArray(new Property[properties.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDisplayPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    StringBuffer text = new StringBuffer();
    for (AbstractBindingProperty bindingProperty : updateProperties(property)) {
      if (text.length() > 0) {
        text.append(", ");
      }
      text.append(bindingProperty.getText());
    }
    if (text.length() > 0) {
      text.insert(0, "[");
      text.append("]");
    }
    return text.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TextDialogPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    AbstractObserveProperty observeProperty = (AbstractObserveProperty) property;
    observeProperty.createBinding();
  }
}