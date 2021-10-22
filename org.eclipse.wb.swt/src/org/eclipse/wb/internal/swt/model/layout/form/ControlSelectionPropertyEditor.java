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
package org.eclipse.wb.internal.swt.model.layout.form;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Property editor allowing to select one control within list of controls on the same parent (except
 * itself).
 *
 * @author mitin_aa
 * @coverage swt.property.editor
 */
public final class ControlSelectionPropertyEditor extends AbstractComboPropertyEditor {
  private final List<ControlInfo> m_controls = Lists.newArrayList();

  @Override
  protected void addItems(Property property, CCombo3 combo) throws Exception {
    FormAttachmentInfo formAttachment = getAttachment(property);
    ControlInfo thisControl = (ControlInfo) formAttachment.getParent().getParent();
    CompositeInfo compositeInfo = (CompositeInfo) thisControl.getParent();
    List<ControlInfo> childrenControls = compositeInfo.getChildrenControls();
    for (ControlInfo controlInfo : childrenControls) {
      if (controlInfo != thisControl) {
        combo.add(controlInfo.getPresentation().getText());
        m_controls.add(controlInfo);
      }
    }
  }

  @Override
  protected void selectItem(Property property, CCombo3 combo) throws Exception {
    combo.setText(getText(property));
  }

  @Override
  protected void toPropertyEx(Property property, CCombo3 combo, int index) throws Exception {
    ControlInfo controlInfo = m_controls.get(index);
    // set expression would further be caught in FormAttachment
    ((GenericPropertyImpl) property).setExpression("", controlInfo);
  }

  @Override
  protected String getText(Property property) throws Exception {
    Object control = property.getValue();
    if (control != null) {
      FormAttachmentInfo attachment = getAttachment(property);
      JavaInfo controlInfo = attachment.getRootJava().getChildByObject(control);
      if (controlInfo != null) {
        return controlInfo.getPresentation().getText();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc/Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private FormAttachmentInfo getAttachment(Property property) {
    GenericProperty genericProperty = (GenericProperty) property;
    FormAttachmentInfo formAttachment = (FormAttachmentInfo) genericProperty.getJavaInfo();
    return formAttachment;
  }
}
