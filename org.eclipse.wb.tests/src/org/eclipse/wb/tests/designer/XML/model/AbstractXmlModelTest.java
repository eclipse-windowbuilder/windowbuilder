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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.xml.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.XML.AbstractXmlObjectTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.description.Description;

import java.util.List;

/**
 * Abstract superclass for any XML model test.
 * 
 * @author scheglov_ke
 */
public abstract class AbstractXmlModelTest extends AbstractXmlObjectTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    disposeLastModel();
    super.tearDown();
  }

  /**
   * Disposes last parsed {@link XmlObjectInfo}.
   */
  protected void disposeLastModel() throws Exception {
    if (m_lastObject != null) {
      m_lastObject.refresh_dispose();
      m_lastObject.getBroadcastObject().dispose();
      m_lastObject = null;
    }
    if (m_lastContext != null) {
      GlobalStateXml.deactivate();
      m_lastContext = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XML source
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link XmlObjectInfo} for parsed XML source.
   */
  protected abstract <T extends XmlObjectInfo> T parse(String... lines) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link ObjectInfo} is visible in both tree children of parent.
   */
  public static void assertVisibleInTree(final ObjectInfo child, boolean expected) throws Exception {
    List<ObjectInfo> children = child.getParent().getPresentation().getChildrenTree();
    if (expected) {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should be visible it tree: " + child;
        }
      }).contains(child);
    } else {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should not be visible it tree: " + child;
        }
      }).doesNotContain(child);
    }
  }

  /**
   * Asserts that given {@link ObjectInfo} is visible in both graphical children of parent.
   */
  public static void assertVisibleInGraphical(final ObjectInfo child, boolean expected)
      throws Exception {
    List<ObjectInfo> children = child.getParent().getPresentation().getChildrenGraphical();
    if (expected) {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should be visible on canvas: " + child;
        }
      }).contains(child);
    } else {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should not be visible on canvas: " + child;
        }
      }).doesNotContain(child);
    }
  }

  /**
   * Asserts that given {@link ObjectInfo} is visible in both tree and graphical children of parent.
   */
  public static void assertVisible(ObjectInfo child, boolean expected) throws Exception {
    assertVisibleInGraphical(child, expected);
    assertVisibleInTree(child, expected);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link XmlObjectInfo} with given component class, using
   * {@link ElementCreationSupport}.
   */
  protected static <T extends XmlObjectInfo> T createObject(String componentClassName)
      throws Exception {
    return createObject(componentClassName, null);
  }

  /**
   * Creates new {@link XmlObjectInfo} with given component class, using
   * {@link ElementCreationSupport}.
   */
  @SuppressWarnings("unchecked")
  protected static <T extends XmlObjectInfo> T createObject(String componentClassName,
      String creationId) throws Exception {
    EditorContext context = ((XmlObjectInfo) GlobalState.getActiveObject()).getContext();
    XmlObjectInfo object =
        XmlObjectUtils.createObject(
            context,
            componentClassName,
            new ElementCreationSupport(creationId));
    object.putArbitraryValue(XmlObjectInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
    return (T) object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Uses first {@link FlowContainer} to perform CREATE.
   */
  public static void flowContainer_CREATE(final XmlObjectInfo container,
      final Object object,
      final Object reference) throws Exception {
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        FlowContainer flowContainer = new FlowContainerFactory(container, false).get().get(0);
        flowContainer.command_CREATE(object, reference);
      }
    });
  }

  /**
   * Uses first {@link FlowContainer} to perform MOVE.
   */
  public static void flowContainer_MOVE(final XmlObjectInfo container,
      final Object object,
      final Object reference) throws Exception {
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        FlowContainer flowContainer = new FlowContainerFactory(container, false).get().get(0);
        flowContainer.command_MOVE(object, reference);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SimpleContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Uses first {@link SimpleContainer} to perform CREATE.
   */
  public static void simpleContainer_CREATE(XmlObjectInfo container, Object object)
      throws Exception {
    SimpleContainer simpleContainer = new SimpleContainerFactory(container, false).get().get(0);
    simpleContainer.command_CREATE(object);
  }

  /**
   * Uses first {@link SimpleContainer} to perform ADD.
   */
  public static void simpleContainer_ADD(XmlObjectInfo container, Object object) throws Exception {
    SimpleContainer simpleContainer = new SimpleContainerFactory(container, false).get().get(0);
    simpleContainer.command_ADD(object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs copy/paste of given {@link XmlObjectInfo}.
   */
  public static <T extends XmlObjectInfo> void doCopyPaste(final T source,
      final PasteProcedure<T> pasteProcedure) throws Exception {
    final XmlObjectMemento memento = XmlObjectMemento.createMemento(source);
    ExecutionUtils.run(source, new RunnableEx() {
      @SuppressWarnings("unchecked")
      public void run() throws Exception {
        T copy = (T) memento.create(source);
        pasteProcedure.run(copy);
        memento.apply();
      }
    });
  }

  public interface PasteProcedure<T> {
    void run(T copy) throws Exception;
  }
}