package nars.guifx;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import nars.Global;
import nars.NAR;
import nars.io.out.Output;
import nars.io.out.TextOutput;
import nars.task.Task;
import nars.util.data.list.CircularArrayList;

import java.util.Arrays;
import java.util.List;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/2/15.
 */
public class LogPane extends VBox implements Runnable {

    private final Output incoming;
    private final NAR nar;
    final int maxLines = 128;
    CircularArrayList<Node> toShow = new CircularArrayList<>(maxLines);
    List<Node> pending;

    ScrollPane scrollParent = null;

    /* to be run in javafx thread */
    @Override public void run() {

        Node[] c = toShow.toArray(new Node[toShow.size()]);

        getChildren().setAll(c);


        scrollBottom.run();
    }

    final Runnable scrollBottom = () -> scrollParent.setVvalue(1f);

    void updateParent() {
        if (getParent()!=null) {
            if (getParent().getParent() != null)
                if (getParent().getParent().getParent() != null) {
                    Node s = getParent().getParent().getParent();
                    if (s instanceof ScrollPane)
                        scrollParent = (ScrollPane) s;
                    else
                        scrollParent = null;
                }
        }

    }

    public LogPane(NAR nar) {
        super();

        this.nar = nar;

        sceneProperty().addListener((c) -> {
            updateParent();
        });

        nar.onEachFrame( () -> {
            List<Node> p = pending;
            if (p!=null) {
                pending = null;
                //synchronized (nar) {
                    int ps = p.size();
                    int start = ps - Math.min(ps, maxLines);

                    int tr = ((ps - start) + toShow.size()) - maxLines;
                    if (tr > ps) {
                        toShow.clear();
                    }
                    else {
                        //remove first N
                        for (int i = 0; i < tr; i++)
                            toShow.removeFirst();
                    }

                    for (int i = start; i < ps; i++)
                        toShow.add(p.get(i));
                //}

                //if (queueUpdate)
                    runLater(LogPane.this);
            }
        });

        incoming = new Output(nar) {

            @Override
            protected boolean output(Channel channel, Class event, Object... args) {
                Node n = getNode(channel, event, args);
                if (n!=null) {

                    if (pending==null)
                        pending = Global.newArrayList();

                    pending.add(n);

                }
                return false;
            }

        };
    }

    public Node getNode(Output.Channel channel, Class event, Object[] args) {

        if (args[0] instanceof Task) {
            /*TaskLabel tl = new TaskLabel(channel.getLinePrefix(event, args) + ' ',
                    (Task)args[0], nar);*/

            Task t = (Task)args[0];
            /*ItemButton tl = new ItemButton( t, (i) -> i.toString(),
                    (i) -> {
                        NARfx.window(nar, t);
                    }
            );*/
            //AutoLabel tl = new AutoLabel( "", t, nar);
            //tl.enablePopupClickHandler(nar);
            //return tl;

            return new AutoLabel(t, nar);
        }

        StringBuilder sb = TextOutput.append(event, args, false, nar, new StringBuilder());
        final String s;
        if (sb != null)
            s = sb.toString();
        else
            s = "null: " + channel.get(event, args) + " " + event + " " + Arrays.toString(args);

        Text t = new Text(s.toString());
        t.setFill(Color.ORANGE);
        t.setCache(true);
        return t;
    }

}
