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
package org.eclipse.wb.internal.core.databinding.model.presentation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoPresentationDecorateIcon;
import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;

import org.eclipse.swt.graphics.Image;

import java.util.Iterator;

/**
 * Decorator for bindings {@link JavaInfo} models.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class JavaInfoDecorator {
  public static final Image IMAGE = Activator.getImage("decorator.gif");
  private final IDatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoDecorator(IDatabindingsProvider provider, ObjectInfo objectInfoRoot) {
    m_provider = provider;
    objectInfoRoot.addBroadcastListener(new ObjectInfoPresentationDecorateIcon() {
      public void invoke(ObjectInfo object, Image[] icon) throws Exception {
        if (hasDecorate(object)) {
          icon[0] = SwtResourceManager.decorateImage(icon[0], IMAGE, SwtResourceManager.TOP_RIGHT);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean hasDecorate(ObjectInfo object) throws Exception {
    if (!m_provider.getBindings().isEmpty() && accept(object)) {
      String objectReference = getReference(object);
      if (objectReference != null) {
        for (Iterator<IBindingInfo> I = m_provider.getBindings().iterator(); I.hasNext();) {
          IBindingInfo binding = I.next();
          if (equals(object, objectReference, binding.getTarget())
              || equals(object, objectReference, binding.getModel())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if given {@link ObjectInfo} can work with bindings.
   */
  protected abstract boolean accept(ObjectInfo object) throws Exception;

  /**
   * @return {@link String} reference that represented given {@link ObjectInfo}.
   */
  protected abstract String getReference(ObjectInfo object) throws Exception;

  /**
   * @return <code>true</code> if given {@link ObjectInfo} equal with given {@link IObserveInfo}.
   */
  protected abstract boolean equals(ObjectInfo object, String objectReference, IObserveInfo iobserve)
      throws Exception;
}