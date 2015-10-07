package nars.guifx.demo;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import nars.Global;
import nars.NAR;
import nars.guifx.IOPane;
import nars.guifx.NARide;
import nars.guifx.graph2.DefaultNARGraph;
import nars.guifx.graph2.NARGraph;
import nars.guifx.util.TabX;
import nars.nal.DerivationRules;
import nars.nar.Default;
import za.co.knonchalant.builder.POJONode;
import za.co.knonchalant.builder.TaggedParameters;

import static nars.guifx.NARfx.scrolled;

/**
 * Created by me on 8/15/15.
 */
public class NARGraph1Test {

    static {
        DerivationRules.maxVarArgsToMatch = 2;
    }

    public static NARGraph newGraph(NAR n) {
        Global.CONCEPT_FORGETTING_EXTRA_DEPTH = 0.5f;


        n.memory.conceptForgetDurations.set(5);
        n.memory.termLinkForgetDurations.set(2);
        n.memory.taskLinkForgetDurations.set(2);

        //n.input(new File("/tmp/h.nal"));
        n.input("<hydochloric --> acid>.");
        n.input("<#x-->base>. %0.65%");
        n.input("<neutralization --> (acid,base)>. %0.75;0.90%");
        n.input("<(&&, <#x --> hydochloric>, eat:#x) --> nice>. %0.75;0.90%");
        //n.input("<(&&,a,b,ca)-->#x>?");

        //n.frame(5);

        NARGraph g = new DefaultNARGraph(n,32);

        GenericControlPane c = new GenericControlPane(g);
        c.getStyleClass().add("graphpopup");
        g.getChildren().add(scrolled(c));

        return g;
    }

    public static void main(String[] args)  {


        NAR n = new Default(512, 2,3,5);

        NARide.show(n.loop(), ide -> {

            ide.content.getTabs().setAll(new TabX("Graph", newGraph(n)));
            ide.addView(new IOPane(n));


            //n.frame(5);

        });

//        NARfx.run((a,b)-> {
//            b.setScene(
//                new Scene(newGraph(n), 600, 600)
//            );
//            b.show();
//
//            n.spawnThread(250, x -> {
//
//            });
//        });





//        TextOutput.out(n);
//        new Thread(() -> n.loop(185)).start();


    }

    private static class GenericControlPane<X> extends BorderPane {

        public final X obj;

        public GenericControlPane(X obj) {
            super();
            this.obj = obj;



            TaggedParameters taggedParameters = new TaggedParameters();


            Node ctl = POJONode.valueToNode(obj, taggedParameters, this); //new VBox();



            setCenter(ctl);
            setTop(new Button("X"));



        }

    }
}
