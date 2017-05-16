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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract JFace {@link Action} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public abstract class ObjectInfoAction extends Action {
  private final ObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectInfoAction(ObjectInfo object) {
    this(object, null);
  }

  public ObjectInfoAction(ObjectInfo object, String text) {
    this(object, text, (ImageDescriptor) null);
  }

  public ObjectInfoAction(ObjectInfo object, String text, ImageDescriptor image) {
    this(object, text, image, AS_PUSH_BUTTON);
  }

  public ObjectInfoAction(ObjectInfo object, String text, Image image) {
    this(object, text, new ImageImageDescriptor(image), AS_PUSH_BUTTON);
  }

  public ObjectInfoAction(ObjectInfo object, String text, int style) {
    this(object, text, null, style);
  }

  public ObjectInfoAction(ObjectInfo object, String text, ImageDescriptor image, int style) {
    super(text, style);
    setImageDescriptor(image);
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets {@link #setImageDescriptor(ImageDescriptor)} using given {@link Image} icon.
   */
  public void setIcon(Image icon) {
    setImageDescriptor(new ImageImageDescriptor(icon));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void run() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        if (shouldRun()) {
          ExecutionUtils.run(m_object, new RunnableEx() {
            public void run() throws Exception {
              runEx();
            }
          });
        }
      }
    });
  }

  /**
   * @return <code>true</code> if {@link #runEx()} should be run inside of edit operation.
   */
  protected boolean shouldRun() throws Exception {
    if (getStyle() == AS_RADIO_BUTTON) {
      return isChecked();
    }
    return true;
  }

  /**
   * Executes some {@link ObjectInfo} editing operation.
   */
  protected abstract void runEx() throws Exception;
}
