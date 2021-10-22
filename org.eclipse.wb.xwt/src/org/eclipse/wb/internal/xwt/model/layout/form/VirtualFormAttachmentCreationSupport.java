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
package org.eclipse.wb.internal.xwt.model.layout.form;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.association.Associations;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;
import org.eclipse.wb.internal.swt.model.layout.form.FormSide;

/**
 * Special creation support for non-existent FormAttachment.
 *
 * @author mitin_aa
 * @coverage XWT.model.layout
 */
public class VirtualFormAttachmentCreationSupport extends CreationSupport
    implements
      IImplicitCreationSupport {
  private final FormDataInfo m_formDataInfo;
  private final Object m_attachmentObject;
  private final FormSide m_formSide;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualFormAttachmentCreationSupport(FormDataInfo formDataInfo,
      Object attachmentObject,
      FormSide formSide) {
    m_formDataInfo = formDataInfo;
    m_attachmentObject = attachmentObject;
    m_formSide = formSide;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "virtual-FormAttachment: " + m_object.getDescription().getComponentClass().getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(XmlObjectInfo object) throws Exception {
    super.setObject(object);
    m_object.setObject(m_attachmentObject);
    m_formDataInfo.addBroadcastListener(new XmlObjectSetObjectAfter() {
      public void invoke(XmlObjectInfo target, Object object) throws Exception {
        // check for this creation support to be active
        if (m_object.getCreationSupport() != VirtualFormAttachmentCreationSupport.this) {
          m_formDataInfo.removeBroadcastListener(this);
          return;
        }
        // this object is the target
        if (target == m_formDataInfo) {
          m_object.setObject(m_attachmentObject);
        }
      }
    });
  }

  @Override
  public String getTitle() {
    return toString();
  }

  @Override
  public DocumentElement getElement() {
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        materialize();
      }
    });
    return m_object.getCreationSupport().getElement();
  }

  private void materialize() throws Exception {
    CreationSupport elementCreationSupport = new ElementCreationSupport();
    m_object.setCreationSupport(elementCreationSupport);
    // add element
    DocumentElement controlElement = m_formDataInfo.getCreationSupport().getElement();
    Association association = Associations.property(m_formSide.getField());
    association.add(m_object, new ElementTarget(controlElement, 0));
  }
}
