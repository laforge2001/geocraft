/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.input;


import java.util.concurrent.Callable;

import org.geocraft.core.rendering.input.InputListener;
import org.geocraft.core.rendering.input.KeyInputEvent;
import org.geocraft.core.rendering.input.MouseInputEvent;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;
import org.geocraft.rendering.jogl.SwtInputAdapter;


/**
 * Main class for initializing and handling mouse interaction with the
 * Layer 1 input system.
 */
public class VolumeMouseLook implements InputListener {

  private final ViewCanvasImplementor _impl;

  private boolean _resetMousePosition;
  private boolean _focusTrigger = false;
  private Callable<?> _focusTriggerCallback;

  private int _lastX, _lastY;
  private boolean _leftDown, _middleDown, _rightDown;
  private boolean _shiftDown, _ctrlDown;

  public VolumeMouseLook(final ViewCanvasImplementor impl) {
    _impl = impl;
  }

  @Override
  public void onMouse(MouseInputEvent e) {
    _shiftDown = e.shift;
    _ctrlDown = e.ctrl;
    switch (e.kind) {
      case PRESS:
        if (e.button == MouseInputEvent.Button.LEFT) _leftDown = true;
        else if (e.button == MouseInputEvent.Button.MIDDLE) _middleDown = true;
        else if (e.button == MouseInputEvent.Button.RIGHT) _rightDown = true;
        _lastX = e.x;
        _lastY = e.y;
        break;
      case RELEASE:
        if (e.button == MouseInputEvent.Button.LEFT) {
          _leftDown = false;
          doClick(false, e.x, e.y);
        } else if (e.button == MouseInputEvent.Button.MIDDLE) _middleDown = false;
        else if (e.button == MouseInputEvent.Button.RIGHT) {
          _rightDown = false;
          doClick(true, e.x, e.y);
        }
        break;
      case DRAG:
        int deltaX = e.x - _lastX;
        int deltaY = e.y - _lastY;
        _lastX = e.x;
        _lastY = e.y;
        if (_resetMousePosition) {
          _resetMousePosition = false;
          break;
        }
        doDrag(deltaX, deltaY);
        break;
      case MOVE:
        _lastX = e.x;
        _lastY = e.y;
        break;
      case WHEEL:
        doWheel(e.wheelDelta, e.x, e.y);
        break;
    }
  }

  @Override
  public void onKey(KeyInputEvent e) {
    _shiftDown = e.shift;
    _ctrlDown = e.ctrl;
  }

  public void doClick(final boolean rightButton, final int x, final int y) {
    if (_focusTrigger) {
      _focusTrigger = false;
      _impl.setViewFocus(x, y);
      if (_focusTriggerCallback != null) {
        try {
          _focusTriggerCallback.call();
        } catch (final Exception e) {
          // ignore
        }
      }
    } else {
      _impl.doPick(x, y, rightButton, null);
    }
  }

  public void doDrag(final int deltaX, final int deltaY) {
    final float scale = 0.01f;

    if (_leftDown && _rightDown) {
      if (deltaY != 0) {
        final float scalar = (_shiftDown ? 0.1f : 1.0f) * (deltaY > 0 ? -1 : 1);
        _impl.zoomCamera(scalar);
      }
    } else if (_middleDown || (_leftDown && _ctrlDown)) {
      if (deltaX != 0 || deltaY != 0) {
        _impl.panCamera(deltaX, -deltaY);
      }
    } else if (_leftDown) {
      if (deltaX != 0 || deltaY != 0) {
        _impl.rotateCamera(scale * -deltaX, scale * deltaY);
      }
    }
  }

  public void doWheel(final int wheelDelta, final int mouseX, final int mouseY) {
    if (wheelDelta != 0) {
      final float scalar = (_shiftDown ? 0.1f : 1.0f) * (wheelDelta > 0 ? -1 : 1);
      if (_ctrlDown) {
        _impl.zoomCamera(scalar);
      } else {
        _impl.zoomCamera(scalar, mouseX, mouseY);
      }
    }
  }

  public void resetMouse() {
    _resetMousePosition = true;
  }

  public void armFocusTrigger(final Callable<?> callback) {
    _focusTrigger = true;
    _focusTriggerCallback = callback;
  }

  /**
   * Attach the mouse-look listener to the given input adapter for the given
   * canvas implementor.
   */
  public static VolumeMouseLook setupTriggers(final SwtInputAdapter inputAdapter,
      final ViewCanvasImplementor impl) {
    final VolumeMouseLook mouseLook = new VolumeMouseLook(impl);
    if (inputAdapter != null) {
      inputAdapter.addListener(mouseLook);
    }
    return mouseLook;
  }

}
