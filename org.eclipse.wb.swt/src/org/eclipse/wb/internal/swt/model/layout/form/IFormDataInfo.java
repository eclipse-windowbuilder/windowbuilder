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

import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

/**
 * Interface for SWT {@link FormData} model. This is related to {@link FormLayout}.
 * 
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public interface IFormDataInfo<C extends IControlInfo> extends ILayoutDataInfo {
  /**
   * @return the IFormAttachmentInfo instance according given <code>side</code> parameter. May not
   *         return <code>null</code>, in case of non-existent attachment the returned
   *         IFormAttachmentInfo instance is virtual.
   */
  IFormAttachmentInfo<C> getAttachment(int side) throws Exception;

  /**
   * Reflects 'width' property of the FormData.
   */
  void setWidth(int value) throws Exception;

  /**
   * Reflects 'height' property of the FormData.
   */
  void setHeight(int value) throws Exception;
}
