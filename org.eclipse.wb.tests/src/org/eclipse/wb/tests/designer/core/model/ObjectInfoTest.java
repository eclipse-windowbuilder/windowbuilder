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
package org.eclipse.wb.tests.designer.core.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;
import org.eclipse.wb.tests.designer.tests.common.PropertyWithTitle;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test for {@link ObjectInfo}.
 * 
 * @author scheglov_ke
 */
public class ObjectInfoTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ObjectInfo#getProperties()}.
   */
  public void test_getProperties() throws Exception {
    ObjectInfo object = new TestObjectInfo();
    Property[] properties = object.getProperties();
    assertThat(properties).isEmpty();
  }

  /**
   * {@link ObjectInfo#getProperties()} should use broadcast
   * {@link ObjectInfoAllProperties#invoke(ObjectInfo, List)}.
   */
  public void test_getProperties_allProperties() throws Exception {
    final Property someProperty = new PropertyWithTitle("some");
    ObjectInfo object = new TestObjectInfo() {
      @Override
      protected List<Property> getPropertyList() throws Exception {
        List<Property> properties = super.getPropertyList();
        properties.add(someProperty);
        return properties;
      }
    };
    // initially has property
    assertThat(object.getProperties()).containsOnly(someProperty);
    // add broadcast to remove all properties
    object.addBroadcastListener(new ObjectInfoAllProperties() {
      public void invoke(ObjectInfo o, List<Property> properties) throws Exception {
        properties.clear();
      }
    });
    assertThat(object.getProperties()).isEmpty();
  }

  /**
   * Test that {@link ObjectInfo#getProperties()} sorts properties by title by default.
   */
  public void test_getProperties_sorted() throws Exception {
    final Property aProperty = new PropertyWithTitle("a");
    final Property bProperty = new PropertyWithTitle("b");
    final Property cProperty = new PropertyWithTitle("c");
    ObjectInfo object = new TestObjectInfo() {
      @Override
      protected List<Property> getPropertyList() throws Exception {
        List<Property> properties = super.getPropertyList();
        properties.add(bProperty);
        properties.add(aProperty);
        properties.add(cProperty);
        return properties;
      }
    };
    //
    Property[] properties = object.getProperties();
    assertThat(properties).hasSize(3);
    assertSame(aProperty, properties[0]);
    assertSame(bProperty, properties[1]);
    assertSame(cProperty, properties[2]);
  }

  /**
   * Test for {@link ObjectInfo#getPropertyList()}.
   */
  @SuppressWarnings("unchecked")
  public void test_getPropertyList() throws Exception {
    ObjectInfo object = new TestObjectInfo();
    List<Property> properties =
        (List<Property>) ReflectionUtils.invokeMethod(object, "getPropertyList()");
    assertThat(properties).isEmpty();
  }

  /**
   * Test for {@link ObjectInfo#getPropertyByTitle(String)}.
   */
  public void test_getPropertyByTitle() throws Exception {
    final Property firstProperty = new PropertyWithTitle("first");
    final Property secondProperty = new PropertyWithTitle("second");
    ObjectInfo object = new TestObjectInfo() {
      @Override
      protected List<Property> getPropertyList() throws Exception {
        List<Property> properties = super.getPropertyList();
        properties.add(firstProperty);
        properties.add(secondProperty);
        return properties;
      }
    };
    assertSame(firstProperty, object.getPropertyByTitle("first"));
    assertSame(secondProperty, object.getPropertyByTitle("second"));
    assertSame(null, object.getPropertyByTitle("noSuchProperty"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary map
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_map() throws Exception {
    ObjectInfo object = new TestObjectInfo();
    // no value initially
    assertNull(object.getArbitraryValue(object));
    // empty arbitrary map
    assertThat(object.getArbitraries()).isEmpty();
    // put value and check
    object.putArbitraryValue(object, this);
    assertSame(this, object.getArbitraryValue(object));
    {
      // arbitrary map contains only 1 item
      Map<Object, Object> arbitraries = object.getArbitraries();
      assertThat(arbitraries).isNotEmpty();
      Set<Entry<Object, Object>> arbitrariesSet = arbitraries.entrySet();
      assertThat(arbitrariesSet).hasSize(1);
      Entry<Object, Object> entry = arbitrariesSet.iterator().next();
      assertSame(object, entry.getKey());
      assertSame(this, entry.getValue());
    }
    // remove value and check
    object.removeArbitraryValue(object);
    assertNull(object.getArbitraryValue(object));
    assertThat(object.getArbitraries()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getUnderlyingModel() throws Exception {
    ObjectInfo object = new TestObjectInfo();
    assertSame(object, object.getUnderlyingModel());
  }

  public void test_setParent() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child = new TestObjectInfo("child");
    child.setParent(parent);
    // parent was set
    assertSame(parent, child.getParent());
    // but child is not in parent
    assertThat(parent.getChildren()).isEmpty();
  }

  public void test_isRoot() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    // build hierarchy
    parent.addChild(child_1);
    // check getRoot()/isRoot()
    assertSame(parent, child_1.getRoot());
    assertTrue(parent.isRoot());
    assertFalse(child_1.isRoot());
  }

  public void test_isDeleted() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child = new TestObjectInfo("child");
    // "child" never was in "parent", so is not deleted
    assertFalse(child.isDeleted());
    // build hierarchy
    parent.addChild(child);
    // "child" is in "parent", so again is not deleted
    assertFalse(child.isDeleted());
    // delete
    parent.removeChild(child);
    assertTrue(child.isDeleted());
  }

  /**
   * Test for {@link ObjectInfo#isParentOf(ObjectInfo)}.
   */
  public void test_isParentOf() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    ObjectInfo child_1_1 = new TestObjectInfo("child_1_1");
    // build hierarchy
    parent.addChild(child_1);
    parent.addChild(child_2);
    child_1.addChild(child_1_1);
    // check isParentOf()
    assertFalse(parent.isParentOf(null));
    assertFalse(parent.isParentOf(parent));
    assertTrue(parent.isParentOf(child_1));
    assertTrue(parent.isParentOf(child_2));
    assertTrue(child_1.isParentOf(child_1_1));
    assertTrue(parent.isParentOf(child_1_1));
    assertFalse(child_2.isParentOf(child_1_1));
  }

  /**
   * Test for {@link ObjectInfo#isParentOf(ObjectInfo)}.
   */
  public void test_isParentOf_whenRemove_1() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_1_1 = new TestObjectInfo("child_1_1");
    // build hierarchy
    parent.addChild(child_1);
    child_1.addChild(child_1_1);
    // initially children bound to "parent"
    assertTrue(parent.isParentOf(child_1));
    assertTrue(parent.isParentOf(child_1_1));
    // remove "child", so not both children are not it "parent"
    parent.removeChild(child_1);
    assertFalse(parent.isParentOf(child_1));
    assertFalse(parent.isParentOf(child_1_1));
  }

  /**
   * Test for {@link ObjectInfo#isParentOf(ObjectInfo)}.
   */
  public void test_isParentOf_whenRemove_2() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_1_1 = new TestObjectInfo("child_1_1");
    // build hierarchy
    parent.addChild(child_1);
    child_1.addChild(child_1_1);
    // initially children bound to "parent"
    assertTrue(parent.isParentOf(child_1));
    assertTrue(parent.isParentOf(child_1_1));
    // remove "child", so not both children are not it "parent"
    child_1.removeChild(child_1_1);
    assertTrue(parent.isParentOf(child_1));
    assertFalse(parent.isParentOf(child_1_1));
  }

  /**
   * Test for {@link ObjectInfo#isItOrParentOf(ObjectInfo)}.
   */
  public void test_isItOrParentOf() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    // build hierarchy
    parent.addChild(child_1);
    parent.addChild(child_2);
    // check isParentOf()
    assertFalse(parent.isItOrParentOf(null));
    assertTrue(parent.isItOrParentOf(parent));
    assertTrue(parent.isItOrParentOf(child_1));
    assertTrue(parent.isItOrParentOf(child_2));
    assertFalse(child_1.isItOrParentOf(child_2));
  }

  public void test_getParent() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    ObjectInfo child_1_1 = new TestObjectInfo("child_1_1");
    // build hierarchy
    parent.addChild(child_1);
    parent.addChild(child_2);
    child_1.addChild(child_1_1);
    // direct parent
    {
      List<ObjectInfo> parents = Lists.newArrayList();
      parents.add(parent);
      parents.add(child_1);
      assertSame(child_1, child_1_1.getParent(parents));
    }
    // direct parent 2
    {
      List<ObjectInfo> parents = Lists.newArrayList();
      parents.add(parent);
      parents.add(child_1);
      assertSame(parent, child_2.getParent(parents));
    }
    // indirect parent
    {
      List<ObjectInfo> parents = Lists.newArrayList();
      parents.add(parent);
      assertSame(parent, child_1_1.getParent(parents));
    }
    // no parent
    {
      List<ObjectInfo> parents = Lists.newArrayList();
      parents.add(child_1);
      assertNull(child_2.getParent(parents));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addChild
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parent/children and hierarchy events.
   */
  public void test_addChild_1() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    // add listener
    final StringBuffer buffer = new StringBuffer();
    parent.addBroadcastListener(new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child, ObjectInfo[] nextChild) {
        buffer.append("childAddBefore " + _parent + " " + _child + "\n");
      }
    });
    parent.addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child) {
        buffer.append("childAddAfter " + _parent + " " + _child + "\n");
      }
    });
    // build hierarchy
    parent.addChild(child_1);
    parent.addChild(child_2);
    // test children and parent
    assertTrue(ArrayUtils.isEquals(new Object[]{child_1, child_2}, parent.getChildren().toArray()));
    assertSame(parent, child_1.getParent());
    assertSame(parent, child_2.getParent());
    // check getChildren(Class)
    {
      assertEquals(0, parent.getChildren(JavaInfo.class).size());
      assertEquals(2, parent.getChildren(TestObjectInfo.class).size());
      assertEquals(2, parent.getChildren(ObjectInfo.class).size());
    }
    //
    assertEquals(
        getSourceDQ(
            "childAddBefore parent child_1",
            "childAddAfter parent child_1",
            "childAddBefore parent child_2",
            "childAddAfter parent child_2"),
        buffer.toString());
  }

  /**
   * Test that we can add before some other child.
   */
  public void test_addChild_2() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child_1 = new TestObjectInfo();
    ObjectInfo child_2 = new TestObjectInfo();
    ObjectInfo child_3 = new TestObjectInfo();
    //
    parent.addChild(child_1);
    parent.addChild(child_2);
    parent.addChild(child_3, child_2);
    assertTrue(ArrayUtils.isEquals(
        new Object[]{child_1, child_3, child_2},
        parent.getChildren().toArray()));
    assertSame(parent, child_1.getParent());
    assertSame(parent, child_2.getParent());
    assertSame(parent, child_3.getParent());
  }

  /**
   * Test for {@link ObjectInfo#addChildFirst(ObjectInfo)}.
   */
  public void test_addChildFirst() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child_1 = new TestObjectInfo();
    ObjectInfo child_2 = new TestObjectInfo();
    ObjectInfo child_3 = new TestObjectInfo();
    //
    parent.addChild(child_1);
    parent.addChild(child_2);
    parent.addChildFirst(child_3);
    assertThat(parent.getChildren()).containsExactly(child_3, child_1, child_2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // moveChild
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_moveChild() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    ObjectInfo child_3 = new TestObjectInfo("child_3");
    //
    parent.addChild(child_1);
    parent.addChild(child_2);
    parent.addChild(child_3);
    // add listener
    final StringBuffer buffer = new StringBuffer();
    parent.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void childMoveBefore(ObjectInfo _parent, ObjectInfo _child, ObjectInfo _nextChild) {
        buffer.append("childMoveBefore " + _parent + " " + _child + " " + _nextChild + "\n");
      }

      @Override
      public void childMoveAfter(ObjectInfo _parent,
          ObjectInfo _child,
          ObjectInfo _nextChild,
          int oldIndex,
          int newIndex) {
        buffer.append("childMoveAfter " + _parent + " " + _child + " " + _nextChild + "\n");
      }
    });
    // move with reference
    {
      buffer.setLength(0);
      parent.moveChild(child_3, child_1);
      assertTrue(ArrayUtils.isEquals(
          new Object[]{child_3, child_1, child_2},
          parent.getChildren().toArray()));
      assertEquals(
          getSourceDQ(
              "childMoveBefore parent child_3 child_1",
              "childMoveAfter parent child_3 child_1"),
          buffer.toString());
    }
    // move as last
    {
      buffer.setLength(0);
      parent.moveChild(child_1, null);
      assertTrue(ArrayUtils.isEquals(
          new Object[]{child_3, child_2, child_1},
          parent.getChildren().toArray()));
      assertEquals(
          getSourceDQ("childMoveBefore parent child_1 null", "childMoveAfter parent child_1 null"),
          buffer.toString());
    }
  }

  /**
   * Test for {@link ObjectInfo#moveChild(ObjectInfo, ObjectInfo)}.<br>
   * Move before itself.
   */
  public void test_moveChild_beforeItself() throws Exception {
    ObjectInfo parent = new TestObjectInfo("parent");
    ObjectInfo child_1 = new TestObjectInfo("child_1");
    ObjectInfo child_2 = new TestObjectInfo("child_2");
    //
    parent.addChild(child_1);
    parent.addChild(child_2);
    // do move
    parent.moveChild(child_2, child_2);
    assertTrue(ArrayUtils.isEquals(new Object[]{child_1, child_2}, parent.getChildren().toArray()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // removeChild
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_removeChild() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child = new TestObjectInfo();
    assertTrue(parent.getChildren().isEmpty());
    // add
    parent.addChild(child);
    assertEquals(1, parent.getChildren().size());
    assertSame(child, parent.getChildren().get(0));
    assertSame(parent, child.getParent());
    // remove
    parent.removeChild(child);
    assertTrue(parent.getChildren().isEmpty());
    assertSame(parent, child.getParent()); // yes, we should keep old parent, not "null"
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replaceChild
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_replaceChild() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child1 = new TestObjectInfo();
    ObjectInfo child2 = new TestObjectInfo();
    ObjectInfo newChild = new TestObjectInfo();
    // add
    parent.addChild(child1);
    parent.addChild(child2);
    assertEquals(2, parent.getChildren().size());
    assertSame(child1, parent.getChildren().get(0));
    assertSame(child2, parent.getChildren().get(1));
    // replace
    parent.replaceChild(child1, newChild);
    assertEquals(2, parent.getChildren().size());
    assertSame(newChild, parent.getChildren().get(0));
    assertSame(child2, parent.getChildren().get(1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener.refresh*
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for all "refresh" notifications in {@link ObjectEventListener}.
   */
  public void test_broadcast_ObjectEventListener_refresh() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child = new TestObjectInfo();
    parent.addChild(child);
    // prepare mock
    ObjectEventListener listener = createStrictMock(ObjectEventListener.class);
    // add listener
    parent.addBroadcastListener(listener);
    // case 1: only root can be refreshed
    {
      reset(listener);
      replay(listener);
      try {
        child.refresh();
        fail();
      } catch (IllegalArgumentException e) {
      }
      verify(listener);
    }
    // case 2: refresh root
    {
      reset(listener);
      listener.refreshDispose();
      listener.refreshBeforeCreate();
      listener.refreshAfterCreate0();
      listener.refreshAfterCreate();
      listener.refreshAfterCreate2();
      listener.refreshFinallyRefresh();
      listener.refreshed();
      listener.refreshed2();
      replay(listener);
      // do refresh
      parent.refresh();
      verify(listener);
    }
    // case 3: use start/endEdit for root
    {
      reset(listener);
      listener.endEdit_aboutToRefresh();
      listener.refreshDispose();
      listener.refreshBeforeCreate();
      listener.refreshAfterCreate0();
      listener.refreshAfterCreate();
      listener.refreshAfterCreate2();
      listener.refreshFinallyRefresh();
      listener.refreshed();
      listener.refreshed2();
      replay(listener);
      // do edit operation
      try {
        parent.startEdit();
      } finally {
        parent.endEdit();
      }
      verify(listener);
    }
    // case 4: use start/endEdit for child
    {
      reset(listener);
      listener.endEdit_aboutToRefresh();
      listener.refreshDispose();
      listener.refreshBeforeCreate();
      listener.refreshAfterCreate0();
      listener.refreshAfterCreate();
      listener.refreshAfterCreate2();
      listener.refreshFinallyRefresh();
      listener.refreshed();
      listener.refreshed2();
      replay(listener);
      // do edit operation
      try {
        child.startEdit();
      } finally {
        child.endEdit();
      }
      verify(listener);
    }
    // case 5: disable refresh
    {
      reset(listener);
      listener.refreshDispose();
      listener.refreshBeforeCreate();
      listener.refreshAfterCreate0();
      listener.refreshAfterCreate();
      listener.refreshAfterCreate2();
      listener.refreshFinallyRefresh();
      replay(listener);
      // do refresh
      parent.putArbitraryValue(ObjectInfo.KEY_NO_REFRESHED_BROADCAST, Boolean.FALSE);
      try {
        parent.refresh();
      } finally {
        parent.removeArbitraryValue(ObjectInfo.KEY_NO_REFRESHED_BROADCAST);
      }
      // no refreshed() notifications expected
      verify(listener);
    }
    // case 6: remove listener, we should not receive refresh notifications
    {
      reset(listener);
      replay(listener);
      // do refresh
      parent.removeBroadcastListener(listener);
      parent.refresh();
      verify(listener);
    }
  }

  /**
   * Kick {@link ObjectInfo#refreshLight()}.
   */
  public void test_refreshLight() throws Exception {
    ObjectInfo object = new TestObjectInfo();
    object.refreshLight();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ObjectInfo#addChild(ObjectInfo)} broadcast.
   */
  public void test_broadcast() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child_1 = new TestObjectInfo("child_1");
    TestObjectInfo child_2 = new TestObjectInfo("child_2");
    // add listener
    final StringBuffer buffer = new StringBuffer();
    parent.addBroadcastListener(new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child, ObjectInfo[] nextChild) {
        buffer.append("childAddBefore " + _parent + " " + _child + "\n");
      }
    });
    parent.addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child) {
        buffer.append("childAddAfter " + _parent + " " + _child + "\n");
      }
    });
    ObjectEventListener listener = new ObjectEventListener() {
      @Override
      public void childRemoveBefore(ObjectInfo _parent, ObjectInfo _child) throws Exception {
        buffer.append("childRemoveBefore " + _parent + " " + _child + "\n");
      }
    };
    parent.addBroadcastListener(listener);
    // do operations
    parent.addChild(child_1);
    child_1.addChild(child_2);
    assertEquals("childAddBefore parent child_1\n"
        + "childAddAfter parent child_1\n"
        + "childAddBefore child_1 child_2\n"
        + "childAddAfter child_1 child_2\n", buffer.toString());
    // remove listener and do more operations
    {
      buffer.setLength(0);
      // remove listener
      parent.removeBroadcastListener(listener);
      // do operation
      assertEquals("", buffer.toString());
      parent.removeChild(child_1);
      // no log expected
      assertEquals("", buffer.toString());
    }
  }

  /**
   * When we attempt to add same listener several times, only one listener should be added.
   */
  public void test_broadcast_duplicate() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child = new TestObjectInfo("child");
    // add listener (2 times!)
    final StringBuffer buffer = new StringBuffer();
    ObjectInfoChildAddBefore listener = new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child, ObjectInfo[] nextChild) {
        buffer.append("childAddBefore " + _parent + " " + _child + "\n");
      }
    };
    parent.addBroadcastListener(listener);
    parent.addBroadcastListener(listener);
    // do operations, only one record expected
    parent.addChild(child);
    assertEquals("childAddBefore parent child\n", buffer.toString());
  }

  /**
   * Test that listener automatically removed after {@link ObjectInfo#refresh()} when its
   * {@link ObjectInfo} is not linked to its parent.
   */
  public void test_broadcast_autoRemove_whenRemoveObject() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child = new TestObjectInfo("child");
    // add child and listener (bound to child)
    parent.addChild(child);
    final StringBuffer buffer = new StringBuffer();
    ObjectEventListener listener = new ObjectEventListener() {
      @Override
      public void dispose() throws Exception {
        buffer.append("dispose");
      }
    };
    child.addBroadcastListener(listener);
    // listener works
    {
      child.getBroadcastObject().dispose();
      assertEquals("dispose", buffer.toString());
      buffer.setLength(0);
    }
    // remove "child", do refresh()
    parent.removeChild(child);
    parent.refresh();
    // listener was removed
    {
      child.getBroadcastObject().dispose();
      assertEquals("", buffer.toString());
      buffer.setLength(0);
    }
  }

  /**
   * Test that listener automatically removed after {@link ObjectInfo#refresh()} when its
   * {@link ObjectInfo} is not linked to root.
   */
  public void test_broadcast_autoRemove_whenRemoveParent() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child_1 = new TestObjectInfo("child");
    TestObjectInfo child_2 = new TestObjectInfo("child2");
    // build hierarchy
    parent.addChild(child_1);
    child_1.addChild(child_2);
    // add child and listener (bound to child)
    final StringBuffer buffer = new StringBuffer();
    ObjectEventListener listener = new ObjectEventListener() {
      @Override
      public void dispose() throws Exception {
        buffer.append("dispose");
      }
    };
    child_2.addBroadcastListener(listener);
    // listener works
    {
      child_2.getBroadcastObject().dispose();
      assertEquals("dispose", buffer.toString());
      buffer.setLength(0);
    }
    // remove "child_1", do refresh()
    parent.removeChild(child_1);
    parent.refresh();
    // "child_2" is linked to "child_1", which is not linked to root, so listener was removed
    {
      child_2.getBroadcastObject().dispose();
      assertEquals("", buffer.toString());
      buffer.setLength(0);
    }
  }

  /**
   * Test that we can use {@link ObjectEventListener#invoke(ObjectInfo, ObjectInfo, ObjectInfo[])}
   * from {@link ObjectInfo#addChild(ObjectInfo)} to set alternative <code>nextChild</code>.
   */
  public void test_addChild_otherNext() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    final TestObjectInfo child_1 = new TestObjectInfo("child_1");
    final TestObjectInfo child_2 = new TestObjectInfo("child_2");
    final TestObjectInfo child_3 = new TestObjectInfo("child_3");
    // add child_1/child_2
    parent.addChild(child_1, null);
    parent.addChild(child_2, null);
    // add listener for re-targeting, instead of "last" add as "first"
    ObjectInfoChildAddBefore listener = new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child, ObjectInfo[] nextChild) {
        if (nextChild[0] == null) {
          nextChild[0] = child_1;
        }
      }
    };
    parent.addBroadcastListener(listener);
    // do add
    parent.addChild(child_3, null);
    // check "parent" children
    {
      List<ObjectInfo> children = parent.getChildren();
      assertEquals(3, children.size());
      assertSame(child_3, children.get(0));
      assertSame(child_1, children.get(1));
      assertSame(child_2, children.get(2));
    }
  }

  /**
   * Test for {@link ObjectInfo#targetBroadcastListener(ObjectInfo)}.
   */
  public void test_broadcast_targetBroadcastListener() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child = new TestObjectInfo("child");
    // add child and listener (bound to child)
    parent.addChild(child);
    final StringBuffer buffer = new StringBuffer();
    ObjectInfoChildAddBefore listener = new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo _parent, ObjectInfo _child, ObjectInfo[] nextChild) {
        buffer.append("childAddBefore " + _parent + " " + _child + "\n");
      }
    };
    child.addBroadcastListener(listener);
    // re-target listener to "parent"
    child.targetBroadcastListener(parent);
    // remove "child" and refresh, without re-target we would not receive events
    parent.removeChild(child);
    parent.refresh();
    // we did re-target, so event expected
    TestObjectInfo child2;
    {
      child2 = new TestObjectInfo("child2");
      assertEquals(0, buffer.length());
      parent.addChild(child2);
      assertEquals("childAddBefore parent child2\n", buffer.toString());
    }
  }

  public interface BroadcastTestInterface {
    void invoke();
  }

  /**
   * Test for using interfaces as broadcast classes.
   */
  public void test_broadcast_interface() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    // add listener
    final StringBuffer buffer = new StringBuffer();
    BroadcastTestInterface listener = new BroadcastTestInterface() {
      public void invoke() {
        buffer.append("invoke");
      }
    };
    parent.addBroadcastListener(listener);
    // send broadcast
    parent.getBroadcast(BroadcastTestInterface.class).invoke();
    assertEquals("invoke", buffer.toString());
    // remove listener and check again
    {
      buffer.setLength(0);
      parent.removeBroadcastListener(listener);
      // send broadcast
      parent.getBroadcast(BroadcastTestInterface.class).invoke();
      assertEquals("", buffer.toString());
    }
  }

  /**
   * There are rare cases when we want to create sub-class of broadcast implementation.
   */
  public void test_broadcast_deepHierarchy() throws Exception {
    TestObjectInfo object = new TestObjectInfo("object");
    // add listener
    final StringBuffer buffer = new StringBuffer();
    class Listener_1 extends ObjectEventListener {
    }
    class Listener_2 extends Listener_1 {
      public void dispose() throws Exception {
        buffer.append("invoke");
      }
    }
    Object listener = new Listener_2();
    object.addBroadcastListener(listener);
    // send broadcast
    object.getBroadcast(ObjectEventListener.class).dispose();
    assertEquals("invoke", buffer.toString());
    // remove listener and check again
    {
      buffer.setLength(0);
      object.removeBroadcastListener(listener);
      // send broadcast
      object.getBroadcast(ObjectEventListener.class).dispose();
      assertEquals("", buffer.toString());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // start/commit/endEdit
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_editOperation() throws Exception {
    final StringBuffer buffer = new StringBuffer();
    ObjectInfo root = new TestObjectInfo() {
      @Override
      protected void saveEdit() throws Exception {
        buffer.append("saveEdit of root");
      }
    };
    ObjectInfo object = new TestObjectInfo() {
      @Override
      protected void saveEdit() throws Exception {
        buffer.append("saveEdit of object");
      }
    };
    root.addChild(object);
    // do edit operation
    object.startEdit();
    try {
    } finally {
      object.endEdit();
    }
    // saveEdit() of "root" should be used
    assertEquals("saveEdit of root", buffer.toString());
  }

  public void test_endEdit_aboutToRefresh() throws Exception {
    final AtomicInteger saveCount = new AtomicInteger();
    final ObjectInfo object = new TestObjectInfo() {
      @Override
      protected void saveEdit() throws Exception {
        saveCount.getAndIncrement();
      }
    };
    //
    final AtomicBoolean wasFired = new AtomicBoolean();
    object.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void endEdit_aboutToRefresh() throws Exception {
        wasFired.set(true);
        // perform inner edit operation
        object.startEdit();
        try {
        } finally {
          object.endEdit();
        }
      }
    });
    // do edit operation
    object.startEdit();
    try {
    } finally {
      object.endEdit();
    }
    // verify, only one saveEdit() should be done
    assertTrue(wasFired.get());
    assertEquals(1, saveCount.get());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ObjectInfo#delete()} does nothing.
   */
  public void test_delete() throws Exception {
    TestObjectInfo parent = new TestObjectInfo("parent");
    TestObjectInfo child = new TestObjectInfo("child");
    parent.addChild(child);
    // "child" is in "parent"
    assertTrue(parent.getChildren().contains(child));
    // do delete, nothing changed
    assertFalse(child.canDelete());
    child.delete();
    assertTrue(parent.getChildren().contains(child));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test visiting using {@link ObjectInfoVisitor}.
   */
  public void test_visiting() throws Exception {
    ObjectInfo parent = new TestObjectInfo();
    ObjectInfo child_1 = new TestObjectInfo();
    ObjectInfo child_2 = new TestObjectInfo();
    ObjectInfo child_3 = new TestObjectInfo();
    //
    parent.addChild(child_1);
    parent.addChild(child_2);
    child_2.addChild(child_3);
    //
    final ObjectInfo[] expectedSequence = {parent, child_1, child_2, child_3};
    parent.accept(new ObjectInfoVisitor() {
      private int m_index;

      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        assertSame(expectedSequence[m_index++], objectInfo);
        return true;
      }
    });
  }
}
