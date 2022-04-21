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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetObjectAfter;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.swt.layout.FormAttachment;

/**
 * Implementation of {@link CreationSupport} for virtual {@link FormAttachment}.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class VirtualFormAttachmentCreationSupport extends CreationSupport {
  private final FormDataInfo m_formDataInfo;
  private final Object m_attachmentObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualFormAttachmentCreationSupport(FormDataInfo formDataInfo, Object attachmentObject) {
    m_formDataInfo = formDataInfo;
    m_attachmentObject = attachmentObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return null;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return false;
  }

  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    m_javaInfo.setObject(m_attachmentObject);
    m_formDataInfo.addBroadcastListener(new JavaInfoSetObjectAfter() {
      @Override
      public void invoke(JavaInfo target, Object object) throws Exception {
        // check if this CreationSupport no more needed
        if (m_javaInfo.getCreationSupport() != VirtualFormAttachmentCreationSupport.this) {
          m_formDataInfo.removeBroadcastListener(this);
          return;
        }
        if (target == m_formDataInfo) {
          m_javaInfo.setObject(m_attachmentObject);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, false);
  }
}
