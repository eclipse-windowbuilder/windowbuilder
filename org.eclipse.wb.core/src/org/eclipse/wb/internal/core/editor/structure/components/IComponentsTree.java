package org.eclipse.wb.internal.core.editor.structure.components;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * Provider of information about visualizer for components tree.
 *
 * @author scheglov_ke
 */
public interface IComponentsTree {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Providers
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ITreeContentProvider} implementation.
   */
  ITreeContentProvider getContentProvider();

  /**
   * @return the {@link ISelectionProvider} implementation.
   */
  ISelectionProvider getSelectionProvider();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expanded
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of expanded components.
   */
  Object[] getExpandedElements();

  /**
   * Sets the array of expanded components.
   */
  void setExpandedElements(Object[] elements);

  /**
   * Sets listener for expanded state.
   */
  void setExpandListener(Runnable listener);
}
