package com.mercury.platform.ui.adr.components;

import com.mercury.platform.shared.config.descriptor.adr.AdrGroupDescriptor;
import com.mercury.platform.shared.config.descriptor.adr.AdrIconDescriptor;
import com.mercury.platform.shared.store.MercuryStoreCore;
import com.mercury.platform.ui.adr.components.panel.AdrComponentPanel;
import com.mercury.platform.ui.adr.components.panel.AdrIconCellPanel;
import com.mercury.platform.ui.misc.AppThemeColor;
import com.mercury.platform.ui.misc.MercuryStoreUI;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class AdrGroupFrame extends AbstractAdrFrame {
    private List<AdrComponentPanel> cells;

    private int x;
    private int y;

    private DraggedFrameMouseListener mouseListener;
    private DraggedFrameMotionListener motionListener;
    private MouseAdapter mouseOverListener;


    public AdrGroupFrame(@NonNull AdrGroupDescriptor descriptor) {
        super(descriptor);
        this.cells = new ArrayList<>();
        this.mouseListener = new DraggedFrameMouseListener();
        this.motionListener = new DraggedFrameMotionListener();
        this.mouseOverListener = getMouseOverListener();

    }

    @Override
    protected void initialize() {
        this.setLocation(descriptor.getLocation());
        this.setOpacity(descriptor.getOpacity());
        this.componentsFactory.setScale(descriptor.getScale());
        this.add(getCellsPanel(),BorderLayout.CENTER);
        this.pack();
    }

    private JPanel getCellsPanel(){
        int cellCount = ((AdrGroupDescriptor) descriptor).getCells().size();
        JPanel root = componentsFactory.getTransparentPanel(new GridLayout(cellCount, 1));
        ((AdrGroupDescriptor)descriptor).getCells().forEach(cellDescriptor -> {
            switch (cellDescriptor.getType()){
                case ICON: {
                    AdrIconCellPanel adrIconCellPanel = new AdrIconCellPanel((AdrIconDescriptor) cellDescriptor,this.componentsFactory);
                    root.add(adrIconCellPanel);
                    cells.add(adrIconCellPanel);
                    break;
                }
            }
        });
        return root;
    }
    private MouseAdapter getMouseOverListener(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getRootPane().setBorder(BorderFactory.createLineBorder(AppThemeColor.TEXT_MESSAGE));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ((JPanel)e.getSource()).setBorder(BorderFactory.createLineBorder(AppThemeColor.TEXT_MESSAGE));
                repaint();
                pack();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((JPanel)e.getSource()).setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppThemeColor.BORDER));
                repaint();
                pack();
            }
        };
    }
    @Override
    public void subscribe() {
        MercuryStoreUI.adrRepaintSubject.subscribe(state -> {
            this.repaint();
            this.pack();
        });
    }

    @Override
    public void enableSettings() {
        super.enableSettings();
        this.setBackground(AppThemeColor.FRAME);
        this.getRootPane().setBorder(BorderFactory.createMatteBorder(1,1,0,1,AppThemeColor.BORDER));
        cells.forEach(it -> {
            it.enableSettings();
            it.setBorder(BorderFactory.createMatteBorder(0,0,1,0,AppThemeColor.BORDER));
            it.addMouseListener(this.mouseListener);
            it.addMouseListener(this.mouseOverListener);
            it.addMouseMotionListener(this.motionListener);
        });
        this.addMouseListener(this.mouseListener);
        this.addMouseMotionListener(this.motionListener);
    }

    @Override
    public void disableSettings() {
        super.disableSettings();
        this.setBackground(AppThemeColor.TRANSPARENT);
        this.getRootPane().setBorder(BorderFactory.createEmptyBorder(1,1,1,1));

        cells.forEach(it -> {
            it.disableSettings();
            it.removeMouseListener(this.mouseListener);
            it.removeMouseMotionListener(this.motionListener);
            it.removeMouseListener(this.mouseOverListener);
            it.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
        });

        this.removeMouseListener(this.mouseListener);
        this.removeMouseMotionListener(this.motionListener);
        this.pack();
        this.repaint();
    }

    @Override
    protected LayoutManager getFrameLayout() {
        return new BorderLayout();
    }

    public class DraggedFrameMotionListener extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                e.translatePoint(AdrGroupFrame.this.getLocation().x - x, AdrGroupFrame.this.getLocation().y - y);
                AdrGroupFrame.this.setLocation(e.getPoint());
            }
        }
    }
    public class DraggedFrameMouseListener extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                x = e.getX();
                y = e.getY();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(SwingUtilities.isLeftMouseButton(e)) {
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                if (getLocationOnScreen().y + getSize().height > dimension.height) {
                    setLocation(getLocationOnScreen().x, dimension.height - getSize().height);
                    descriptor.setLocation(
                            new Point(getLocationOnScreen().x, dimension.height - getSize().height));
                    MercuryStoreCore.saveConfigSubject.onNext(true);
                }
            }
        }
    }
}