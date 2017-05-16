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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.IMouseMoveListener;
import org.eclipse.wb.draw2d.events.IMouseTrackListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.gef.core.CancelOperationError;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class EventManager implements MouseListener, MouseMoveListener, MouseTrackListener {
  public static final int ANY_BUTTON = SWT.BUTTON1 | SWT.BUTTON2 | SWT.BUTTON3;
  //
  private final FigureCanvas m_canvas;
  private final RootFigure m_root;
  private Figure m_cursorFigure;
  private Figure m_captureFigure;
  private Cursor m_cursor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventManager(FigureCanvas canvas) {
    m_canvas = canvas;
    m_root = m_canvas.getRootFigure();
    // custom tooltip
    new CustomTooltipManager(canvas, this);
    // add listeners
    Object listener =
        createListenerProxy(this, new Class[]{
            MouseListener.class,
            MouseMoveListener.class,
            MouseTrackListener.class});
    m_canvas.addMouseListener((MouseListener) listener);
    m_canvas.addMouseMoveListener((MouseMoveListener) listener);
    m_canvas.addMouseTrackListener((MouseTrackListener) listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cursor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates the Cursor.
   */
  public void updateCursor() {
    if (m_cursorFigure == null) {
      setCursor(null);
    } else {
      setCursor(m_cursorFigure.getCursor());
    }
  }

  /**
   * Set the Cursor.
   */
  public void setCursor(Cursor cursor) {
    if (m_cursor == null) {
      if (cursor == null) {
        return;
      }
    } else if (m_cursor == cursor || m_cursor.equals(cursor)) {
      return;
    }
    //
    m_cursor = cursor;
    m_canvas.setCursor(m_cursor);
  }

  protected void updateFigureToolTipText() {
    if (m_cursorFigure == null) {
      m_canvas.setToolTipText(null);
    } else {
      m_canvas.setToolTipText(m_cursorFigure.getToolTipText());
    }
  }

  private void setFigureUnderCursor(Figure figure, org.eclipse.swt.events.MouseEvent event) {
    if (m_cursorFigure != figure) {
      sendEvent(MOUSE_EXIT_INVOKER, IMouseTrackListener.class, event);
      //
      m_cursorFigure = figure;
      sendEvent(MOUSE_ENTER_INVOKER, IMouseTrackListener.class, event);
      // finish
      updateCursor();
      updateFigureToolTipText();
    }
  }

  public final Figure getCursorFigure() {
    return m_cursorFigure;
  }

  /**
   * Update the {@link Figure} located at the given location which will accept mouse events.
   */
  protected final void updateFigureUnderCursor(org.eclipse.swt.events.MouseEvent event) {
    TargetFigureFindVisitor visitor = new TargetFigureFindVisitor(m_canvas, event.x, event.y);
    m_root.accept(visitor, false);
    setFigureUnderCursor(visitor.getTargetFigure(), event);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Capture
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets capture to the given figure. All subsequent events will be sent to the given figure until
   * {@link #setCapture(null)} is called.
   */
  public void setCapture(Figure captureFigure) {
    m_captureFigure = captureFigure;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Consume
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_eventConsumed;

  /**
   * Return whether this event has been consumed.
   */
  protected boolean isEventConsumed() {
    return m_eventConsumed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseEvent listener's
  //
  ////////////////////////////////////////////////////////////////////////////
  public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_DOUBLE_CLICK_INVOKER, IMouseListener.class, event);
  }

  public void mouseDown(org.eclipse.swt.events.MouseEvent event) {
    if (m_canvas.getToolTipText() != null) {
      m_canvas.setToolTipText(null);
    }
    handleMouseEvent(MOUSE_DOWN_INVOKER, IMouseListener.class, event);
  }

  public void mouseUp(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_UP_INVOKER, IMouseListener.class, event);
  }

  public void mouseMove(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_MOVE_INVOKER, IMouseMoveListener.class, event);
  }

  private void handleMouseEvent(IListenerInvoker invoker,
      Class<?> listenerClass,
      org.eclipse.swt.events.MouseEvent event) {
    updateFigureUnderCursor(event);
    sendEvent(invoker, listenerClass, event);
  }

  private <T extends Object> void sendEvent(IListenerInvoker invoker,
      Class<T> listenerClass,
      org.eclipse.swt.events.MouseEvent e) {
    m_eventConsumed = false;
    Figure figure = m_captureFigure == null ? m_cursorFigure : m_captureFigure;
    //
    if (figure != null) {
      List<T> listeners = figure.getListeners(listenerClass);
      if (listeners != null && !listeners.isEmpty()) {
        MouseEvent event = new MouseEvent(m_canvas, e, figure);
        for (Iterator<T> I = listeners.iterator(); !event.isConsumed() && I.hasNext();) {
          invoker.invokeListener(I.next(), event);
        }
        m_eventConsumed = event.isConsumed();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseTrackListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void mouseEnter(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_ENTER_INVOKER, IMouseTrackListener.class, event);
  }

  public void mouseExit(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_EXIT_INVOKER, IMouseTrackListener.class, event);
  }

  public void mouseHover(org.eclipse.swt.events.MouseEvent event) {
    handleMouseEvent(MOUSE_HOVER_INVOKER, IMouseTrackListener.class, event);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invoke
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke <code>mouseDown()</code>.
   */
  private static final IListenerInvoker MOUSE_DOWN_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseListener mouseListener = (IMouseListener) listener;
      mouseListener.mouseDown(event);
    }
  };
  /**
   * Invoke <code>mouseUp()</code>.
   */
  private static final IListenerInvoker MOUSE_UP_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseListener mouseListener = (IMouseListener) listener;
      mouseListener.mouseUp(event);
    }
  };
  /**
   * Invoke <code>mouseDoubleClick()</code>.
   */
  private static final IListenerInvoker MOUSE_DOUBLE_CLICK_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseListener mouseListener = (IMouseListener) listener;
      mouseListener.mouseDoubleClick(event);
    }
  };
  /**
   * Invoke <code>mouseMove()</code>.
   */
  private static final IListenerInvoker MOUSE_MOVE_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseMoveListener mouseListener = (IMouseMoveListener) listener;
      mouseListener.mouseMove(event);
    }
  };
  /**
   * Invoke <code>mouseEnter()</code>.
   */
  private static final IListenerInvoker MOUSE_ENTER_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseTrackListener mouseListener = (IMouseTrackListener) listener;
      mouseListener.mouseEnter(event);
    }
  };
  /**
   * Invoke <code>mouseExit()</code>.
   */
  private static final IListenerInvoker MOUSE_EXIT_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseTrackListener mouseListener = (IMouseTrackListener) listener;
      mouseListener.mouseExit(event);
    }
  };
  /**
   * Invoke <code>mouseHover()</code>.
   */
  private static final IListenerInvoker MOUSE_HOVER_INVOKER = new IListenerInvoker() {
    public void invokeListener(Object listener, MouseEvent event) {
      IMouseTrackListener mouseListener = (IMouseTrackListener) listener;
      mouseListener.mouseHover(event);
    }
  };

  private static interface IListenerInvoker {
    /**
     * This method use to invoke any listeners.
     */
    void invokeListener(Object listener, MouseEvent event);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Event listener helper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the dynamic proxy implementation for given interfaces.
   */
  public static final Object createListenerProxy(final Object owner, Class<?>[] interfaces) {
    return Proxy.newProxyInstance(
        owner.getClass().getClassLoader(),
        interfaces,
        new InvocationHandler() {
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // check for delay
            if (delayEvent(this, proxy, method, args)) {
              return null;
            }
            // process event now
            try {
              return method.invoke(owner, args);
            } catch (InvocationTargetException e) {
              if (e.getCause() instanceof CancelOperationError) {
                // ignore
              } else {
                throw e;
              }
            }
            // no return expected
            return null;
          }
        });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events queue
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String FLAG_DELAY_EVENTS = "Flag that events to this Control should be delayed";
  private static String KEY_DELAYED_EVENTS = "List of delayed events";

  /**
   * Specifies if events to given {@link Control} should be delayed or not.
   */
  public static void delayEvents(Control control, boolean delay) {
    if (delay) {
      control.setData(FLAG_DELAY_EVENTS, Boolean.TRUE);
    } else {
      control.setData(FLAG_DELAY_EVENTS, null);
    }
  }

  /**
   * If arguments contain {@link TypedEvent} and target {@link Control} is disabled, then puts this
   * event into {@link List} with key {@link #KEY_DELAYED_EVENTS}.
   *
   * @return <code>true</code> if event was delayed.
   */
  private static boolean delayEvent(InvocationHandler handler,
      Object proxy,
      Method method,
      Object args[]) {
    if (args[0] instanceof TypedEvent) {
      TypedEvent event = (TypedEvent) args[0];
      if (event.widget instanceof Control) {
        Control control = (Control) event.widget;
        if (control.getData(FLAG_DELAY_EVENTS) != null) {
          // prepare delay queue
          @SuppressWarnings("unchecked")
          List<DelayedEvent> eventQueue = (List<DelayedEvent>) control.getData(KEY_DELAYED_EVENTS);
          if (eventQueue == null) {
            eventQueue = new ArrayList<DelayedEvent>();
            control.setData(KEY_DELAYED_EVENTS, eventQueue);
          }
          // put event into queue
          eventQueue.add(new DelayedEvent(handler, proxy, method, args));
          // event was delayed
          return true;
        }
      }
    }
    // no delay
    return false;
  }

  /**
   * Runs events delayed before because given {@link Control} was disabled.
   */
  public static void runDelayedEvents(Control control) {
    // prepare delay queue
    @SuppressWarnings("unchecked")
    List<DelayedEvent> eventQueue = (List<DelayedEvent>) control.getData(KEY_DELAYED_EVENTS);
    control.setData(KEY_DELAYED_EVENTS, null);
    // run all events
    if (eventQueue != null) {
      for (DelayedEvent event : eventQueue) {
        event.run();
      }
    }
  }

  /**
   * Container for information about single event that was delayed because of disabled target.
   */
  private static final class DelayedEvent {
    private final InvocationHandler m_handler;
    private final Object m_proxy;
    private final Method m_method;
    private final Object[] m_args;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    DelayedEvent(InvocationHandler handler, Object proxy, Method method, Object[] args) {
      m_handler = handler;
      m_proxy = proxy;
      m_method = method;
      m_args = args;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Invokes same event listener as was used when target of this event was disabled.
     */
    void run() {
      try {
        m_handler.invoke(m_proxy, m_method, m_args);
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
    }
  }
}