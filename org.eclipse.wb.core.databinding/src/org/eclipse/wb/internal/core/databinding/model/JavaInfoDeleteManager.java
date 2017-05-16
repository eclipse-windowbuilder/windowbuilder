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
package org.eclipse.wb.internal.core.databinding.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;

import java.util.List;

/**
 * Observe {@link JavaInfo} events for delete bindings that have reference to deleted
 * {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class JavaInfoDeleteManager {
  protected final IDatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoDeleteManager(IDatabindingsProvider provider, ObjectInfo objectInfoRoot) {
    m_provider = provider;
    objectInfoRoot.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        deleteJavaInfo(child);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void deleteJavaInfo(ObjectInfo javaInfo) throws Exception {
    List<IBindingInfo> deleteList = Lists.newArrayList();
    List<IBindingInfo> bindings = m_provider.getBindings();
    //
    if (!m_provider.getBindings().isEmpty() && accept(javaInfo)) {
      String reference = getReference(javaInfo);
      //
      if (reference != null) {
        for (IBindingInfo binding : Lists.newArrayList(bindings)) {
          if (equals(javaInfo, reference, binding.getTarget())
              || equals(javaInfo, reference, binding.getModel())) {
            deleteList.add(binding);
            deleteBinding(binding, bindings);
          }
        }
      }
    }
    // commit
    if (!deleteList.isEmpty()) {
      bindings.removeAll(deleteList);
      m_provider.saveEdit();
    }
  }

  protected abstract void deleteBinding(IBindingInfo binding, List<IBindingInfo> bindings)
      throws Exception;

  /**
   * @return <code>true</code> if given {@link JavaInfo} can work with bindings.
   */
  protected abstract boolean accept(ObjectInfo javaInfo) throws Exception;

  /**
   * @return {@link String} reference that represented given {@link JavaInfo}.
   */
  protected abstract String getReference(ObjectInfo javaInfo) throws Exception;

  /**
   * @return <code>true</code> if given {@link JavaInfo} equal with given {@link IObserveInfo}.
   */
  protected abstract boolean equals(ObjectInfo javaInfo,
      String javaInfoReference,
      IObserveInfo iobserve) throws Exception;
}