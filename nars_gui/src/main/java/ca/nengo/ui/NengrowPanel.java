package ca.nengo.ui;

import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;

import java.util.Iterator;

/**
 Simple panel which can be used to display a
 basic Network view
 */
public class NengrowPanel extends Nengrow {

    /** simulation timestep */
    protected float dt = 0.01f;

    protected float time = 0;

    //protected allows subclasses to access it, private does not
    //protected final UINetwork networkUI;


    public NengrowPanel(Network view) {
        super();

        UINetwork networkUIx;
        NodeViewer windowx;
        try {
            networkUIx = (UINetwork) addNodeModel(view);
            windowx = networkUIx.openViewer(Window.WindowState.MAXIMIZED);
        } catch (ContainerException e) {
            //TODO display error message in the nengo view or statusbar
            e.printStackTrace();
        }


        //networkUI = networkUIx;

    }
    public NengrowPanel() {
        super();

    }

    public void add(Object... x) {
        for (Object n : x) {
            if (n instanceof Node) {
                try {
                    addNodeModel((Node) n);
                } catch (ContainerException e) {
                    e.printStackTrace();
                }
            } else if (n instanceof WorldObject) {
                try {
                    addNodeModel((WorldObject) n, 0d, 0d);
                } catch (ContainerException e) {
                    e.printStackTrace();
                }
            } else
                throw new RuntimeException("How do you want to add " + n + " to this");
        }

    }

    @Override
    public void run() {
        try {
            //System.out.println(time);
            Iterator<Node> n = getNodeModels();
            while (n.hasNext()) {
                n.next().run(time, time + dt);
            }
            time += dt;

        } catch (SimulationException e1) {
            e1.printStackTrace();
        }

    }


    @Override
    public void init() throws Exception {


    }
}
