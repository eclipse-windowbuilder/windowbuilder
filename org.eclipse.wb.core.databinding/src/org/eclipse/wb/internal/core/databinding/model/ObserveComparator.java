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

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import java.util.Comparator;

/**
 * A comparison function for {@link IObserveInfo} objects.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class ObserveComparator implements Comparator<IObserveInfo> {
  public static final Comparator<IObserveInfo> INSTANCE = new ObserveComparator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comparator
  //
  ////////////////////////////////////////////////////////////////////////////
  public int compare(final IObserveInfo observe1, final IObserveInfo observe2) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Integer>() {
      public Integer runObject() throws Exception {
        String text1 = observe1.getPresentation().getText();
        String text2 = observe2.getPresentation().getText();
        return text1.compareTo(text2);
      }
    }, 0);
  }
}