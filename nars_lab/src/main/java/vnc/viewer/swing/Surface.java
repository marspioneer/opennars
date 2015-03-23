// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package vnc.viewer.swing;

import ca.nengo.ui.lib.NengoStyle;
import org.piccolo2d.PLayer;
import org.piccolo2d.POffscreenCanvas;
import vnc.OCR;
import vnc.core.SettingsChangedEvent;
import vnc.rfb.IChangeSettingsListener;
import vnc.rfb.IRepaintController;
import vnc.rfb.client.KeyEventMessage;
import vnc.rfb.encoding.PixelFormat;
import vnc.rfb.encoding.decoder.FramebufferUpdateRectangle;
import vnc.rfb.protocol.ProtocolContext;
import vnc.rfb.protocol.ProtocolSettings;
import vnc.transport.Reader;
import vnc.viewer.UiSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Deque;
import java.util.Iterator;

@SuppressWarnings("serial")
public class Surface extends JPanel implements IRepaintController, IChangeSettingsListener {

    private POffscreenCanvas sky;
	private int width;
	private int height;
	private SoftCursorImpl cursor;
	RendererImpl renderer;
	public final MouseEventListener mouse;
	public final KeyEventListener key;
	private boolean showCursor;
	private ModifierButtonEventListener modifierButtonListener;
	private boolean isUserInputEnabled = false;
	private final ProtocolContext context;
    private SwingViewerWindow viewerWindow;
    private double scaleFactor;
	public Dimension oldSize;
    private BufferedImage skyImage;
    //private BufferedImage skyImage;


    public RendererImpl getRenderer() {
        return renderer;
    }

    @Override
	public boolean isDoubleBuffered() {
		// TODO returning false in some reason may speed ups drawing, but may
		// not. Needed in challenging.
		return false;
	}

	public Surface(ProtocolContext context, double scaleFactor, LocalMouseCursorShape mouseCursorShape) {
        super();

        setIgnoreRepaint(true);
        setOpaque(false);




        key = new KeyEventListener(context) {
            @Override
            protected void onKeyEvent(KeyEventMessage ee) {
                Surface.this.onKeyEvent(ee);
            }
        };
        mouse = new MouseEventListener(this, context, scaleFactor) {
            @Override
            protected void onMouseEvent(MouseEvent mouseEvent, MouseWheelEvent mouseWheelEvent, boolean moved) {
                Surface.this.onMouseEvent(mouseEvent, mouseWheelEvent, moved);
            }
        };

        this.context = context;
        this.scaleFactor = scaleFactor;
        init(context.getFbWidth(), context.getFbHeight());
        oldSize = getPreferredSize();

        if (!context.getSettings().isViewOnly()) {
            setUserInputEnabled(true, context.getSettings().isConvertToAscii());
        }
        showCursor = context.getSettings().isShowRemoteCursor();
        setLocalCursorShape(mouseCursorShape);

    }

    private void onMouseEvent(MouseEvent mouseEvent, MouseWheelEvent mouseWheelEvent, boolean moved) {
        /* for overriding */
    }

    protected void onKeyEvent(KeyEventMessage ee) {
        /* for overriding */
    }


    // TODO Extract abstract/interface ViewerWindow from SwingViewerWindow
    public void setViewerWindow(SwingViewerWindow viewerWindow) {
        this.viewerWindow = viewerWindow;
    }

    private void setUserInputEnabled(boolean enable, boolean convertToAscii) {
		if (enable == isUserInputEnabled) return;
		isUserInputEnabled = enable;
		if (enable) {
			addMouseListener(mouse);
			addMouseMotionListener(mouse);
			addMouseWheelListener(mouse);

			setFocusTraversalKeysEnabled(false);
			if (null == key) {
				if (modifierButtonListener != null) {
					key.addModifierListener(modifierButtonListener);
				}
			}
			key.setConvertToAscii(convertToAscii);
			addKeyListener(key);
			enableInputMethods(false);
		} else {
			removeMouseListener(mouse);
			removeMouseMotionListener(mouse);
			removeMouseWheelListener(mouse);
			removeKeyListener(key);
		}
	}

	@Override
	public vnc.drawing.Renderer createRenderer(Reader reader, int width, int height, PixelFormat pixelFormat) {
		renderer = new RendererImpl(reader, width, height, pixelFormat);
		synchronized (renderer.getLock()) {
			cursor = renderer.getCursor();
		}
		init(renderer.getWidth(), renderer.getHeight());
		updateFrameSize();

        sky = new POffscreenCanvas(renderer.getWidth(), renderer.getHeight());
        sky.setInteracting(false);


		return renderer;
	}

    public POffscreenCanvas getSky() {
        return sky;
    }

    private void init(int width, int height) {
		this.width = width;
		this.height = height;
		setSize(getPreferredSize());
	}

	private void updateFrameSize() {
		setSize(getPreferredSize());
		viewerWindow.pack();
		requestFocus();
	}

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
    }


    public synchronized void renderSky(long now, Deque<OCR.BufferUpdate> ocrResults) {
        if (sky == null) return;


        //if (skyImage==null) {
        BufferedImage skyImage = new BufferedImage((int) sky.getCamera().getWidth(), (int) sky.getCamera().getHeight(), BufferedImage.TYPE_INT_ARGB);
            sky.setOpaque(false);

            //sky.setBackground(NengoStyle.COLOR_TRANSPARENT);
            //sky.setRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
            //sky.getCamera().validateFullPaint();



        Graphics2D g = (Graphics2D) skyImage.getGraphics();



        if (Math.random() < 1) {
            sky.setBackground(null);
            getSky().render(g);

        }

        else {
            sky.setBackground(NengoStyle.COLOR_TRANSPARENT);

            //((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            //
            //        g.setPaint(NengoStyle.COLOR_TRANSPARENT);
            //        g.fillRect(0,0,(int)sky.getCamera().getWidth(), (int)sky.getCamera().getHeight());


            g.setStroke(new BasicStroke(4));

            if (!ocrResults.isEmpty()) {


                float sx = 1;
                float sy = 1;

                Iterator<OCR.BufferUpdate> ib = ocrResults.iterator();

                while (ib.hasNext()) {
                    OCR.BufferUpdate bu = ib.next();

                    float text = Math.min(bu.getText().length(), 10) / 10.0f;
                    float opacity = 0.5f;
                    g.setPaint(new Color(bu.getConfidence(), text, 0, bu.getConfidence() * opacity));

                    g.drawRect(
                            (int) (bu.rect.x * sx),
                            (int) (bu.rect.y * sy),
                            (int) (bu.rect.width * sx),
                            (int) (bu.rect.height * sy));
                    final int maxAge = 5000;

                    if (now - bu.getInputTime() > maxAge)
                        ib.remove();
                }


            }

        }

        g.dispose();

        this.skyImage = skyImage;

    }

    @Override
	public void paintComponent(Graphics g) {


        if (null != renderer) {


            ((Graphics2D) g).scale(scaleFactor, scaleFactor);
            //((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            synchronized (renderer.getLock()) {
                Image offscreenImage = renderer.getFrame();



                if (offscreenImage != null) {
                    g.drawImage(offscreenImage, 0, 0, null);
                }


                //sky.getCamera().repaint();









            }
            synchronized (cursor.getLock()) {

                Image cursorImage = cursor.getImage();
                if (showCursor && cursorImage != null &&
                        (scaleFactor != 1 ||
                                g.getClipBounds().intersects(cursor.rX, cursor.rY, cursor.width, cursor.height))) {
                    g.drawImage(cursorImage, cursor.rX, cursor.rY, null);
                }

                if (skyImage!=null) {
                    // Get and install an AlphaComposite to do transparent drawing
                    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

                    double skyScale = 0.5f;
                    g.drawImage(skyImage, 0, 0, (int) (renderer.getWidth() * skyScale), (int)( renderer.getHeight() * skyScale),
                            0, 0, (int) sky.getCamera().getWidth(), (int) sky.getCamera().getHeight(), (ImageObserver) null);

                    //((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

                }

            }
        }



    }


    @Override
	public Dimension getPreferredSize() {
		return new Dimension((int)(this.width * scaleFactor), (int)(this.height * scaleFactor));
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	/**
	 * Saves context and simply invokes native JPanel repaint method which
	 * asyncroniously register repaint request using invokeLater to repaint be
	 * runned in Swing event dispatcher thread. So may be called from other
	 * threads.
	 */
	@Override
	public void repaintBitmap(FramebufferUpdateRectangle rect) {
		repaintBitmap(rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public void repaintBitmap(int x, int y, int width, int height) {
		repaint((int)(x * scaleFactor), (int)(y * scaleFactor),
                (int)Math.ceil(width * scaleFactor), (int)Math.ceil(height * scaleFactor));
	}

	@Override
	public void repaintCursor() {
		synchronized (cursor.getLock()) {
			repaint((int)(cursor.oldRX * scaleFactor), (int)(cursor.oldRY * scaleFactor),
					(int)Math.ceil(cursor.oldWidth * scaleFactor) + 1, (int)Math.ceil(cursor.oldHeight * scaleFactor) + 1);
			repaint((int)(cursor.rX * scaleFactor), (int)(cursor.rY * scaleFactor),
					(int)Math.ceil(cursor.width * scaleFactor) + 1, (int)Math.ceil(cursor.height * scaleFactor) + 1);
		}
	}

	@Override
	public void updateCursorPosition(short x, short y) {
		synchronized (cursor.getLock()) {
			cursor.updatePosition(x, y);
			repaintCursor();
		}
	}

	private void showCursor(boolean show) {
		synchronized (cursor.getLock()) {
			showCursor = show;
		}
	}

	public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
		this.modifierButtonListener = modifierButtonListener;
		if (key != null) {
			key.addModifierListener(modifierButtonListener);
		}
	}

	@Override
	public void settingsChanged(SettingsChangedEvent e) {
		if (ProtocolSettings.isRfbSettingsChangedFired(e)) {
			ProtocolSettings settings = (ProtocolSettings) e.getSource();
			setUserInputEnabled( ! settings.isViewOnly(), settings.isConvertToAscii());
			showCursor(settings.isShowRemoteCursor());
		} else if (UiSettings.isUiSettingsChangedFired(e)) {
			UiSettings uiSettings = (UiSettings) e.getSource();
			oldSize = getPreferredSize();
			scaleFactor = uiSettings.getScaleFactor();
            if (uiSettings.isChangedMouseCursorShape()) {
                setLocalCursorShape(uiSettings.getMouseCursorShape());
            }
		}
		mouse.setScaleFactor(scaleFactor);
		updateFrameSize();
	}

    public void setLocalCursorShape(LocalMouseCursorShape cursorShape) {
        if (LocalMouseCursorShape.SYSTEM_DEFAULT == cursorShape) {
            setCursor(Cursor.getDefaultCursor());
        } else {
            setCursor(Utils.getCursor(cursorShape));
        }
    }

    @Override
	public void setPixelFormat(PixelFormat pixelFormat) {
		if (renderer != null) {
			renderer.initPixelFormat(pixelFormat);
		}
	}

    public PLayer getSkyLayer() {
        if (getSky()==null) return null;
        return getSky().getCamera().getLayer(0);
    }
}