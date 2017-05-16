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
package org.eclipse.wb.internal.core.utils;

/**
 * Version identifier for bundles and packages.
 *
 * @author sablin_aa
 * @coverage core.util
 */
public final class Version {
  private static final String SEPARATOR = ".";
  private final int m_major;
  private final int m_minor;
  private final int m_micro;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Version(int major, int minor) {
    this(major, minor, 0);
  }

  public Version(int major, int minor, int micro) {
    m_major = major;
    m_minor = minor;
    m_micro = micro;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof Version)) {
      return false;
    }
    Version other = (Version) object;
    return m_major == other.m_major && m_minor == other.m_minor && m_micro == other.m_micro;
  }

  @Override
  public int hashCode() {
    return m_minor + 37 * m_major + 37 * m_micro;
  }

  @Override
  public String toString() {
    return getStringMajorMinorMicro();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the major component of this {@link Version}.
   */
  public int getMajor() {
    return m_major;
  }

  /**
   * @return the minor component of this {@link Version}.
   */
  public int getMinor() {
    return m_minor;
  }

  /**
   * @return the micro component of this {@link Version}.
   */
  public int getMicro() {
    return m_micro;
  }

  /**
   * @return the "major.minor" string.
   */
  public String getStringMajorMinor() {
    return m_major + SEPARATOR + m_minor;
  }

  /**
   * @return the "major.minor.micro" string.
   */
  public String getStringMajorMinorMicro() {
    return m_major + SEPARATOR + m_minor + SEPARATOR + m_micro;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compare
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link Version} is exactly same as given.
   */
  public boolean isSame(Version that) {
    return equals(that);
  }

  /**
   * @return <code>true</code> if components of this {@link Version} is are greater than given.
   */
  public boolean isHigher(Version that) {
    if (m_major > that.m_major) {
      return true;
    }
    if (m_major == that.m_major) {
      if (m_minor > that.m_minor) {
        return true;
      }
      if (m_minor == that.m_minor) {
        return m_micro > that.m_micro;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if {@link #isHigher(Version)} or {@link #isSame(Version)}.
   */
  public boolean isHigherOrSame(Version that) {
    return isSame(that) || isHigher(that);
  }

  /**
   * @return <code>true</code> if components of this {@link Version} is are less than given.
   */
  public boolean isLower(Version that) {
    if (m_major < that.m_major) {
      return true;
    }
    if (m_major == that.m_major) {
      if (m_minor < that.m_minor) {
        return true;
      }
      if (m_minor == that.m_minor) {
        return m_micro < that.m_micro;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code> if {@link #isLower(Version)} or {@link #isSame(Version)}.
   */
  public boolean isLowerOrSame(Version that) {
    return isSame(that) || isLower(that);
  }
}