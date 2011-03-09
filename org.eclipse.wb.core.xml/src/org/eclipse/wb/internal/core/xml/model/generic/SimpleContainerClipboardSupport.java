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
package org.eclipse.wb.internal.core.xml.model.generic;

import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;

import java.util.List;

/**
 * Support for copying {@link SimpleContainer} child.
 * 
 * @author scheglov_ke
 * @coverage XML.model.generic
 */
public final class SimpleContainerClipboardSupport implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new SimpleContainerClipboardSupport();

  private SimpleContainerClipboardSupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final XmlObjectInfo root) throws Exception {
    root.addBroadcastListener(new XmlObjectClipboardCopy() {
      public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
        List<SimpleContainer> containers = getSimpleContainers(object);
        for (int i = 0; i < containers.size(); i++) {
          SimpleContainer container = containers.get(i);
          Object child = container.getChild();
          if (child instanceof XmlObjectInfo) {
            ClipboardCommand command = createCommand(i, (XmlObjectInfo) child);
            commands.add(command);
          }
        }
      }
    });
  }

  private static List<SimpleContainer> getSimpleContainers(XmlObjectInfo object) {
    return new SimpleContainerFactory(object, true).get();
  }

  /**
   * @return the {@link ClipboardCommand} for creating given child on simple container, during
   *         paste.
   */
  private static ClipboardCommand createCommand(final int containerIndex, XmlObjectInfo child)
      throws Exception {
    final XmlObjectMemento memento = XmlObjectMemento.createMemento(child);
    ClipboardCommand command = new ClipboardCommand() {
      private static final long serialVersionUID = 0L;

      @Override
      public void execute(XmlObjectInfo newContainer) throws Exception {
        XmlObjectInfo newChild = memento.create(newContainer);
        getSimpleContainers(newContainer).get(containerIndex).command_CREATE(newChild);
        memento.apply();
      }
    };
    return command;
  }
}
