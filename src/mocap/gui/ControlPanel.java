package mocap.gui;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.media.j3d.Transform3D;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3d;

import mocap.JMocap;
import mocap.player.PlayerFrameListener;

/**
 * Pane on right hand side to load animations, change perspective,
 * playback etc.
 * 
 * @author Michael Kipp
 */
public class ControlPanel extends JPanel implements PlayerFrameListener 
{

    private static final int DEFAULT_FPS = 120;
    private JMocap _jmocap;
    private JMocapGUI _gui;
    private int _lastRot;
    protected JButton _playButton;
    protected ImageIcon _playIcon, _pauseIcon;
    private JLabel _fpsLabel, _scrubLabel;
    private JSlider _fpsSlider;
    private JSlider _scrubber;

    protected ControlPanel(JMocap app, JMocapGUI gui, ActionListener actionListener)
    {
        _jmocap = app;
        _gui = gui;
        setLayout(new GridLayout(0, 1));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createFilePane(actionListener));
        add(createPlaybackPane());
        add(createFpsPane());
        add(create3DControls());
        add(createCursorControls());
        add(createScrubberPane());
    }

    public void setFps(int n)
    {
        _fpsSlider.setValue(n);
    }

    private JPanel createFilePane(ActionListener actionListener)
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), "File"));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        JButton loadBVH = new JButton(JMocapGUI.LOAD_BVH);
        JButton loadCfg = new JButton(JMocapGUI.LOAD_CFG);
        JButton clearAll = new JButton("Clear");
        p.add(loadBVH);
        p.add(loadCfg);
        p.add(clearAll);
        loadBVH.addActionListener(actionListener);
        loadCfg.addActionListener(actionListener);
        clearAll.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                _jmocap.clearAll();
            }
        });
        return p;
    }

    private ImageIcon getIcon(String path)
    {
        URL url = getClass().getResource("/" + path);
        if (url != null) {
            System.out.println("found url: " + url);
            return new ImageIcon(url);
        } else {
            System.out.println("no url");
            return new ImageIcon(path);
        }
    }

    private JPanel createPlaybackPane()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), "Play"));
        
        // create buttons
        _playIcon = getIcon("img/Play16.gif");
        _pauseIcon = getIcon("img/Pause16.gif");
        _playButton = new JButton(_playIcon);
        JButton stop = new JButton(getIcon("img/Stop16.gif"));
        JButton ffwd = new JButton(getIcon("img/StepForward16.gif"));
        JButton fbwd = new JButton(getIcon("img/StepBack16.gif"));
        JButton reset = new JButton("reset");
        _playButton.setFocusPainted(false);
        _playButton.setRolloverEnabled(false);
        stop.setFocusPainted(false);
        stop.setRolloverEnabled(false);
        
        // add functionality
        _playButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (_playButton.getIcon() == _playIcon) {
                    if (_jmocap.play()) {
                        _playButton.setIcon(_pauseIcon);
                    }
                } else {
                    _jmocap.pause();
                    _playButton.setIcon(_playIcon);
                }
            }
        });
        stop.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (_playButton.getIcon() == _pauseIcon) {
                    _playButton.setIcon(_playIcon);
                }
                _jmocap.getFigureManager().stopAll();
            }
        });
        ffwd.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                _jmocap.pause();
                _jmocap.getFigureManager().frameForwardAll();
            }
        });
        fbwd.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                _jmocap.pause();
                _jmocap.getFigureManager().frameBackwardAll();
            }
        });
        reset.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
            	//TODO: When we have a similar method in our Bone class
                _jmocap.getFigure().getSkeleton().reset();
            }
        });
        
        // layout
        p.add(_playButton);
        p.add(stop);
        p.add(fbwd);
        p.add(ffwd);
        p.add(reset);
        return p;
    }

    /**
     * Panel for 3D controls: rotation and zoom.
     */
    private JPanel create3DControls()
    {
        JPanel p = new JPanel();
        JPanel p1 = new JPanel();
        JLabel lrot = new JLabel("Rotate:");
        lrot.setHorizontalAlignment(JLabel.CENTER);
        JLabel lzoom = new JLabel("Zoom:");
        p.setBorder(BorderFactory.createEtchedBorder());
        final JSlider rotSlider = new JSlider(-180, 180);
        rotSlider.setMajorTickSpacing(180);
        rotSlider.setPaintTicks(true);
        rotSlider.setPaintLabels(true);
        p1.setLayout(new GridLayout(0, 1));
        p1.add(lrot);
        p1.add(rotSlider);
        final JSlider distSlider = new JSlider(JSlider.VERTICAL, 0, 50, (int) JMocapController.CAMERA.z);
        distSlider.setMajorTickSpacing(25);
        distSlider.setPaintLabels(true);
        distSlider.setPaintTicks(true);
        distSlider.setPreferredSize(new Dimension(50, 90));
        rotSlider.setPreferredSize(new Dimension(120, 50));
        _lastRot = 0;
        rotSlider.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                Transform3D tf = new Transform3D();
                Transform3D t2 = new Transform3D();
                t2.rotY(Math.toRadians(_lastRot - rotSlider.getValue()));
                _lastRot = rotSlider.getValue();
                _jmocap.getUniverse().getViewingPlatform().getViewPlatformTransform().getTransform(tf);
                t2.mul(tf);
                _jmocap.getUniverse().getViewingPlatform().getViewPlatformTransform().setTransform(t2);
            }
        });
        distSlider.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                Transform3D tf = new Transform3D();
                _jmocap.getUniverse().getViewingPlatform().getViewPlatformTransform().getTransform(tf);
                Point3d cam = new Point3d(0, 0, 0);
                tf.transform(cam);
                cam.z = distSlider.getValue();
                _jmocap.setCameraView(cam, JMocapController.CAMERA_TARGET);
            }
        });
        p.add(p1);
        p.add(lzoom);
        p.add(distSlider);
        return p;
    }

    /**
     * Panel for 3D controls: rotation and zoom.
     */
    private JPanel createCursorControls()
    {
        final int MAX = 300;
        JPanel p = new JPanel();
        JPanel p1 = new JPanel();
        JLabel lrot = new JLabel("x:");
        lrot.setHorizontalAlignment(JLabel.CENTER);
        JLabel lzoom = new JLabel("z:");
        p.setBorder(BorderFactory.createEtchedBorder());
        final JSlider xSlider = new JSlider(-MAX, MAX);
        xSlider.setMajorTickSpacing(100);
        xSlider.setPaintTicks(true);
        xSlider.setPaintLabels(true);
        p1.setLayout(new GridLayout(0, 1));
        p1.add(lrot);
        p1.add(xSlider);
        final JSlider zSlider = new JSlider(JSlider.VERTICAL, -MAX, MAX, 0);
        zSlider.setMajorTickSpacing(100);
        zSlider.setPaintLabels(true);
        zSlider.setPaintTicks(true);
        xSlider.setPreferredSize(new Dimension(150, 50));
        zSlider.setPreferredSize(new Dimension(50, 120));

        _lastRot = 0;
        ChangeListener ch = new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                _gui.moveCursor(xSlider.getValue() / 10f, -zSlider.getValue() / 10f);
            }
        };
        xSlider.addChangeListener(ch);
        zSlider.addChangeListener(ch);
        p.add(p1);
        p.add(lzoom);
        p.add(zSlider);
        return p;
    }

    private JSlider createFpsSlider()
    {
        final JSlider s = new JSlider(JSlider.HORIZONTAL, 0, 200, DEFAULT_FPS);
        s.setPreferredSize(new Dimension(120, 50));
        s.setMinorTickSpacing(10);
        s.setMajorTickSpacing(50);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setSnapToTicks(true);
        s.addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                int i = s.getValue();
                _fpsLabel.setText("fps: " + i);
                _jmocap.getFigureManager().setFpsAll(s.getValue());
            }
        });
        _fpsSlider = s;
        return s;
    }
    
    private JSlider createScrubber()
    {
        _scrubber = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
        _scrubber.setPreferredSize(new Dimension(120, 50));
       // _scrubber.setMinorTickSpacing(50);
       // _scrubber.setMajorTickSpacing(200);
        _scrubber.setPaintTicks(true);
        _scrubber.setPaintLabels(true);
        _scrubber.setSnapToTicks(true);
        _scrubber.addChangeListener(new ChangeListener()
        {
        	@Override
        	public void stateChanged(ChangeEvent arg0) {
        		
        		// stop the animation if it is playing
        		if (_playButton.getIcon() == _pauseIcon)
        			_playButton.doClick();
        		
        		_jmocap.getFigure().getPlayer().setCurFrame(_scrubber.getValue());
        		_jmocap.getFigureManager().goToFrame(_scrubber.getValue());
        		_scrubLabel.setText("Current Frame: " + _scrubber.getValue());
        		
        		JSlider source = (JSlider)arg0.getSource();
        	    if (!source.getValueIsAdjusting()) 
        	    {
        	    	// if the animation was stopped, start it again
        	    	if (_playButton.getIcon() == _playButton)
        	    		_playButton.doClick();
        	    }
        	}
        });
        return _scrubber;
    }

    private JPanel createFpsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1));
        p.setBorder(BorderFactory.createEtchedBorder());
        _fpsLabel = new JLabel("fps: " + DEFAULT_FPS);
        _fpsLabel.setHorizontalAlignment(JLabel.CENTER);
        p.add(_fpsLabel);
        p.add(createFpsSlider());
        p.setPreferredSize(new Dimension(200, 100));
        return p;
    }
    
    private JPanel createScrubberPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 1));
        p.setBorder(BorderFactory.createEtchedBorder());
        _scrubLabel = new JLabel("Current Frame: 0");
        _scrubLabel.setHorizontalAlignment(JLabel.CENTER);
        p.add(_scrubLabel);
        p.add(createScrubber());
        p.setPreferredSize(new Dimension(200, 100));
        return p;
    }
    
    public void setFrames(int numFrames) {
    	_scrubber.setMaximum(numFrames-1);
    	//This doesn't work. Apparently it's a JSlider bug. WTF?
    	_scrubber.setMajorTickSpacing(numFrames / 5);
    	_scrubber.setMinorTickSpacing(numFrames / 20);
    	//repaint();
    }
    
    public void setScrubberPos(int frame) {
    	_scrubber.setValue(frame);
    	_scrubLabel.setText("Current Frame: " + frame);
    }

	@Override
	public void frameUpdate(int framenumber) {
		setScrubberPos(framenumber);		
	}

//    protected void pause()
//    {
//        if (_playButton.getIcon() == _pauseIcon) {
//            _playButton.setIcon(_playIcon);
//            _jmocap.getFigureManager().pauseAll();
//        }
//    }
}
