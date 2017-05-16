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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.lang.NotImplementedException;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JPanel;

/**
 * {@link Association} is presentation of parent/child link. It can be wrapper for
 * {@link ClassInstanceCreation} (for example in SWT all widgets accept parent in constructor
 * argument), or {@link MethodInvocation} (for example
 * {@link Container#add(java.awt.Component, Object) in Swing, or factory method invocation in SWT).
 * <p> But in theory association can be something more complex, than just single
 *
 * @link ClassInstanceCreation} or {@link MethodInvocation}, for example some object that should be
 *       created and configured initially, and only as one of the invocation parent/child link is
 *       established. For this reason and just to separate various variants of associations (so be
 *       more OOP), we introduce {@link Association} as class.
 *
 *
 *       <p>
 *       Here is list of possible associations and their implementations:
 *       <ol>
 *
 *       <li>{@link ConstructorParentAssociation} - {@link ClassInstanceCreation} with
 *       <em>parent</em> argument. Association established on child creation. Child created after
 *       parent. This is usual association in SWT.</li>
 *
 *       <li>{@link ConstructorChildAssociation} - {@link ClassInstanceCreation} with <em>child</em>
 *       argument. Association established on parent creation. Child created before parent. This is
 *       rarely used association, for example {@link JPanel} accepts {@link LayoutManager} in
 *       constructor.</li>
 *
 *       <li>{@link FactoryParentAssociation} - factory {@link MethodInvocation} with
 *       <em>parent</em> argument. Used for factories, i.e. this is association on creation, it is
 *       different than separate association using for example invocation of child method with
 *       parent argument (like {@link Control#setParent(org.eclipse.swt.widgets.Composite)}).</li>
 *
 *       <li>{@link InvocationChildAssociation} - {@link MethodInvocation} of parent with
 *       <em>child</em> argument. Often used in Swing -
 *       {@link Container#add(java.awt.Component, Object)}.</li>
 *
 *       <li>{@link InvocationSecondaryAssociation} - {@link MethodInvocation} with both,
 *       <em>parent and child</em> arguments, marked as secondary. Can be used to establish some
 *       special parent/child link, used method that does not belong to parent or child, but just
 *       separate method (sometimes used for {@link GridBagLayout}).</li> </ul>
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public abstract class Association {
  protected JavaInfo m_javaInfo;
  protected AstEditor m_editor;
  protected IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link JavaInfo} that is associated using this {@link Association}.<br>
   * This method can be used only once and only for not-null {@link JavaInfo}.
   */
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    Assert.isNull(m_javaInfo);
    Assert.isNotNull(javaInfo);
    m_javaInfo = javaInfo;
    m_editor = m_javaInfo.getEditor();
    m_javaProject = m_editor.getJavaProject();
  }

  /**
   * @return the {@link JavaInfo} for which this {@link Association} is installed.
   */
  public final JavaInfo getJavaInfo() {
    return m_javaInfo;
  }

  /**
   * @return <code>true</code> if {@link #remove()} can be used, so this {@link JavaInfo} can be
   *         deleted.
   */
  public boolean canDelete() {
    return true;
  }

  /**
   * @return the {@link Statement} that contains this {@link Association}, or <code>null</code> if
   *         this {@link Association} is not established by any {@link Statement}.
   */
  public Statement getStatement() {
    return null;
  }

  /**
   * This method is used in tests to simplify checking of {@link Association}'s.
   *
   * @return the source code of this {@link Association}.
   */
  public String getSource() {
    throw new NotImplementedException(getClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Allows {@link Association} add zero or more {@link Property}'s, for example for method
   * invocation we can add complex property with sub-property for each argument (except child
   * argument).
   */
  public void addProperties(List<Property> properties) throws Exception {
    // don't add any property by default
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Applies this {@link Association} into source code, for example generates
   * {@link MethodInvocation}.
   * <p>
   * Not all {@link Association} implementations do something on this operation, for example in SWT
   * component associated with its parent on creation, so no separate association required.
   *
   * @param javaInfo
   *          the {@link JavaInfo} that should associated.
   * @param target
   *          the {@link StatementTarget} for association.
   * @param leadingComments
   *          the optional (can be <code>null</code>) array of lines that should be added before
   *          first statement
   */
  public void add(JavaInfo javaInfo, StatementTarget target, String[] leadingComments)
      throws Exception {
    setInModelNoCompound(javaInfo);
  }

  /**
   * Moves this {@link Association} to specified {@link NodeTarget} during moving component inside
   * of same container.
   * <p>
   * Not all {@link Association} implementations support this operation, for example in SWT
   * component associated with its parent on creation, so association can not be moved separately.
   * May be {@link LazyVariableSupport} is only user of this operation.
   */
  public void move(StatementTarget target) throws Exception {
    throw new NotImplementedException(getClass());
  }

  /**
   * Notifies this {@link Association} that {@link JavaInfo} was moved from one parent to other.
   * {@link Association} should update source code to reflect fact of association with new parent,
   * for example update reference on parent in constructor, or in invocation.
   * <p>
   * Sequence of operations during is following:
   *
   * <pre>
	 * 	oldAssociation.remove();
	 * 	if (oldAssociation.isStillActive()) {
	 * 		oldAssociation.setParent(newParent);
	 * 	} else {
	 * 		newAssociation.add(...);
	 * 	}
	 * </pre>
   *
   * @param parent
   *          the new parent. We could use parent from {@link JavaInfo#getParentJava()}, but in SWT
   *          Viewer's Control association is delegated to Viewer that is implemented as
   *          <em>child</em> of Control, so we in this case we should use parent-parent.
   */
  public void setParent(JavaInfo parent) throws Exception {
    throw new NotImplementedException(getClass());
  }

  /**
   * Deletes this {@link Association} from source code and from {@link JavaInfo#getAssociation()}.
   * We do this when delete component or move it on new container.
   * <p>
   * Not all {@link Association}'s can be removed. For example {@link ConstructorParentAssociation}
   * creates association on creation, so can not be removed (so {@link JavaInfo#getAssociation()}
   * will still return it even after "remove"). During {@link JavaInfo} "move" we check that if
   * {@link Association} is not removed, we ask it to move on new parent using
   * {@link #setParent(JavaInfo)}.
   *
   * @return <code>true</code> if {@link Association} was really removed.
   */
  public boolean remove() throws Exception {
    removeFromModelIfPrimary();
    return true;
  }

  /**
   * This method is used during morphing to transform existing association for source component into
   * association for target component. Here we create corresponding new instance of
   * {@link Association} for new component.
   *
   * @throws NotImplementedException
   *           if this {@link Association} does not supports morphing.
   */
  public Association getCopy() {
    throw new NotImplementedException(getClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If {@link JavaInfo} has no {@link CompoundAssociation}, then set this {@link Association}.
   * <p>
   * When we add new {@link Association} using
   * {@link Association#add(JavaInfo, StatementTarget, String[])}, we may be first and only
   * {@link Association}, but may be there is already existing {@link CompoundAssociation}.
   */
  protected final void setInModelNoCompound(JavaInfo javaInfo) throws Exception {
    if (javaInfo.getAssociation() instanceof CompoundAssociation) {
      setJavaInfo(javaInfo);
    } else {
      javaInfo.setAssociation(this);
    }
  }

  /**
   * This method can be used from {@link #remove()} to remove this {@link Association} from
   * {@link JavaInfo#setAssociation(Association)} if this {@link Association} is "primary", i.e. if
   * it is really used in our {@link JavaInfo}.
   * <p>
   * When {@link CompoundAssociation} is used, it is "primary", and its "sub-associations" are no
   * "primary", so they can not clear association in {@link JavaInfo}. Only when
   * {@link CompoundAssociation} becomes empty, we really clear association.
   */
  protected final void removeFromModelIfPrimary() throws Exception {
    if (m_javaInfo.getAssociation() == this) {
      m_javaInfo.setAssociation(null);
    }
  }
}
