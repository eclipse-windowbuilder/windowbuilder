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
package org.eclipse.wb.core.model;

import com.google.common.collect.MapMaker;

import org.eclipse.wb.internal.core.utils.check.Assert;

import java.util.Map;

/**
 * Utilities for {@link ObjectInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model
 */
public final class ObjectInfoUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Private Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ObjectInfoUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ID
  //
  ////////////////////////////////////////////////////////////////////////////
  private static long m_lastObjectInfoID = 0;
  private static Map<String, ObjectInfo> m_idToObjectInfo = new MapMaker().weakValues().makeMap();
  private static Map<ObjectInfo, String> m_objectInfoToId = new MapMaker().weakKeys().makeMap();

  /**
   * @return the {@link ObjectInfo} with corresponding ID.
   */
  public static ObjectInfo getById(String id) {
    ObjectInfo result = m_idToObjectInfo.get(id);
    Assert.isNotNull(result, "Can not find ObjectInfo for %s", id);
    return result;
  }

  /**
   * @return the unique {@link ObjectInfo} id.
   */
  public static String getId(ObjectInfo objectInfo) {
    String id = getId0(objectInfo);
    Assert.isNotNull(id, "No ID for (%s) %s", objectInfo.getClass().getName(), objectInfo);
    return id;
  }

  /**
   * Sets the unique {@link ObjectInfo} id (should be called only once).
   */
  public static void setNewId(ObjectInfo objectInfo) {
    {
      String existingID = getId0(objectInfo);
      Assert.isNull(existingID, "%s already has ID %s", objectInfo, existingID);
    }
    setId0(objectInfo, Long.toString(m_lastObjectInfoID++));
  }

  private static String getId0(ObjectInfo objectInfo) {
    return m_objectInfoToId.get(objectInfo);
  }

  private static void setId0(ObjectInfo objectInfo, String id) {
    m_objectInfoToId.put(objectInfo, id);
    m_idToObjectInfo.put(id, objectInfo);
  }
}
