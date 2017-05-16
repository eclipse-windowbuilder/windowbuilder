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
package org.eclipse.wb.internal.core.utils.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.graphics.Image;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Helper for automatic disposing {@link Image}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ImageDisposer {
  private static boolean DEBUG = false;
  private static ReferenceQueue<Object> m_queue = new ReferenceQueue<Object>();
  private static final List<ImageHolder> m_references = Lists.newArrayList();
  private static Timer m_timer;
  static {
    // create Timer (its TimerThread) with empty set of ProtectionDomain's,
    // so prevent holding reference on stack ProtectionDomain's, which include ClassLoader's
    AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        m_timer = new Timer(true);
        return null;
      }
    }, new AccessControlContext(new ProtectionDomain[]{}));
    // schedule time
    m_timer.schedule(new TimerTask() {
      @Override
      public void run() {
        disposeImages();
      }
    }, 1000, 1000);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ImageDisposer() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Registers {@link Image} that should be disposed when its key is garbage collected.
   */
  public static synchronized void add(Object key, String name, Image image) {
    if (image != null) {
      m_references.add(new ImageHolder(key, m_queue, name, image));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link WeakReference} that holds reference on {@link Image}.
   */
  private static class ImageHolder extends WeakReference<Object> {
    private final String m_name;
    private final Image m_image;

    public ImageHolder(Object key, ReferenceQueue<Object> queue, String name, Image image) {
      super(key, queue);
      m_name = name;
      m_image = image;
    }
  }

  /**
   * Disposes {@link Image}-s for which keys are garbage collected.
   */
  private static synchronized void disposeImages() {
    int removed = 0;
    while (true) {
      ImageHolder reference = (ImageHolder) m_queue.poll();
      if (reference == null) {
        break;
      }
      // remove reference
      {
        m_references.remove(reference);
        removed++;
      }
      // dispose Image
      final Image image = reference.m_image;
      ExecutionUtils.runAsync(new RunnableEx() {
        public void run() throws Exception {
          if (image != null && !image.isDisposed()) {
            image.dispose();
          }
        }
      });
    }
    if (DEBUG && removed != 0) {
      printReferences();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Debug
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void printReferences() {
    Debug.println("references: " + m_references.size());
    try {
      for (ImageHolder reference : m_references) {
        Debug.print("\t");
        Debug.print(reference.m_name);
        Debug.println();
      }
    } catch (Throwable e) {
    }
  }
}
