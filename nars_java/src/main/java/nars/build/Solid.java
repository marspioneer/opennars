package nars.build;

import javolution.util.FastSortedSet;
import javolution.util.function.Equality;
import nars.control.ConceptActivator;
import nars.core.Core;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.TextOutput;
import nars.logic.BudgetFunctions;
import nars.logic.entity.*;
import nars.logic.reason.ConceptProcess;
import nars.logic.reason.DirectProcess;
import nars.util.bag.impl.CacheBag;
import nars.util.bag.impl.CurveBag;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Iterator;

/** processes every concept fairly, according to priority, in each cycle */
public class Solid extends Default {


    private final int maxConcepts, maxSubConcepts;
    private final int maxTasks;
    private final int minTaskLink;
    private final int maxTaskLink;
    private final int minTermLink;
    private final int maxTermLink;
    private final int inputsPerCycle;
    private Memory memory;


    public Solid(int inputsPerCycle, int maxConcepts, int minTaskLink, int maxTaskLink, int minTermLink, int maxTermLink) {
        super();
        this.inputsPerCycle = inputsPerCycle;
        this.maxConcepts = maxConcepts;
        this.maxSubConcepts = maxConcepts * 4;
        this.maxTasks = maxConcepts * maxTaskLink * maxTermLink * 2;
        this.minTaskLink = minTaskLink;
        this.maxTaskLink = maxTaskLink;
        this.minTermLink = minTermLink;
        this.maxTermLink = maxTermLink;
        param.duration.set(1);
    }

    @Override
    public void init(NAR n) {
        super.init(n);
        this.memory = n.memory;

    }




    static final Comparator<Item> budgetComparator = new Comparator<Item>() {
        //almost...
        //> Math.pow(2.0,32.0) * 0.000000000001
        //0.004294967296

        //one further is below 0.001 resolution
        //> Math.pow(2.0,32.0) * 0.0000000000001
        //0.0004294967296

        @Override
        public int compare(final Item o1, final Item o2) {
            if (o1.equals(o2)) return 0; //is this necessary?
            float p1 = o1.getPriority();
            float p2 = o2.getPriority();
            if (p1 == p2) {
                float d1 = o1.getDurability();
                float d2 = o2.getDurability();
                if (d1 == d2) {
                    float q1 = o1.getQuality();
                    float q2 = o2.getQuality();
                    if (q1 == q2) {
                        return Integer.compare(o1.hashCode(), o2.hashCode());
                    }
                    else {
                        return q1 < q2 ? -1 : 1;
                    }
                }
                else {
                    return d1 < d2 ? -1 : 1;
                }
            }
            else {
                return p1 < p2 ? -1 : 1;
            }
        }
    };

    @Override
    public Core newCore() {


            return new Core() {


                public final CacheBag<Term, Concept> subcon = new CacheBag(maxSubConcepts);

                CurveBag<Concept,Term> concepts = new CurveBag(maxConcepts, true);

                final ConceptActivator activator = new ConceptActivator() {
                    @Override
                    public Memory getMemory() {
                        return memory;
                    }

                    @Override
                    public CacheBag<Term, Concept> getSubConcepts() {
                        return subcon;
                    }

                    @Override
                    public ConceptBuilder getConceptBuilder() {
                        return Solid.this.getConceptBuilder();
                    }
                };


                //iterates in reverse, lowest to highest
                FastSortedSet<Task> tasks = new FastSortedSet(new Equality<Task>() {

                    @Override
                    public int hashCodeOf(Task object) {
                        return object.hashCode();
                    }

                    @Override
                    public boolean areEqual(Task left, Task right) {
                        return left.equals(right);
                    }

                    @Override
                    public int compare(Task left, Task right) {
                        return budgetComparator.compare(left, right);
                    }
                });

                @Override
                public Iterator<Concept> iterator() {
                    return concepts.iterator();
                }

                @Override
                public void addTask(Task t) {
                    if (!t.aboveThreshold())
                        return;

                    if (tasks.size() >= maxTasks) {
                        //reject this task if it lower than the lowest
                        Task lowest = tasks.first();
                        if (budgetComparator.compare(lowest, t) == 1) {
                            return;
                        }
                    }

                    tasks.add(t);
                }

                @Override
                public int size() {
                    return 0;
                }

            protected int num(float p, int min, int max) {
                return Math.round((p * (max - min)) + min);
            }



            @Override
            public void cycle() {
                //System.out.println("\ncycle " + memory.time() + " : " + concepts.size() + " concepts");

                getMemory().perceiveNext(inputsPerCycle);

                //1. process all new tasks
                int t = 0;
                for (Task task : tasks) {

                    if (!task.aboveThreshold()) continue; //need to check again because it seems that Task proirity can be modified even after it is in the task queue

                    new DirectProcess(getMemory(), task).run();
                    if (t++ >= maxTasks) break;
                }
                tasks.clear();

                //2. fire all concepts
                for (Concept c : concepts) {

                    float p = c.getPriority();
                    int fires = num(p, minTaskLink, maxTaskLink);
                    if (fires < 1) continue;
                    int termFires = num(p, minTermLink, maxTermLink);
                    if (termFires < 1) continue;

                    //System.out.println("  firing " + c.budget + " " + c + " x " + fires);

                    for (int i = 0; i < fires; i++) {
                        TaskLink tl = c.taskLinks.forgetNext(param.taskLinkForgetDurations, getMemory());
                        if (tl==null) break;
                        new ConceptProcess(c, tl, termFires).run();
                    }
                }

                memory.runNextTasks();
            }

            @Override
            public void reset() {
                tasks.clear();
                concepts.clear();
                subcon.clear();
            }

            @Override
            public Concept concept(Term term) {
                return concepts.get(term);
            }

            @Override
            public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing) {
                synchronized(activator) {
                    activator.set(term, budget, true, getMemory().time());
                    return concepts.update(activator);
                }
            }

            @Override
            @Deprecated public void activate(Concept c, BudgetValue b, BudgetFunctions.Activating mode) {

            }

            @Override
            public Concept nextConcept() {
                return concepts.peekNext();
            }

            @Override
            public void init(Memory m) {
                subcon.setMemory(m);
            }

            @Override
            public void conceptRemoved(Concept c) {
                subcon.add(c);
            }

            @Override
            public Memory getMemory() {
                return memory;
            }
        };
    }


    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {
        //Bag<String, TaskLink> taskLinks = new ChainBag(getConceptTaskLinks());
        //Bag<TermLinkKey, TermLink> termLinks = new ChainBag(getConceptTermLinks());

        //return new Concept(b, t, taskLinks, termLinks, m);
        return super.newConcept(b, t, m);
    }


    public static void main(String[] args) throws FileNotFoundException {

        Parameters.DEBUG = false;

        Solid s = new Solid(2, 256, 0, 9, 0, 3);
        NAR n = new NAR(s);
        n.input(new File("/tmp/h.nal"));

        TextOutput.out(n).setPriorityMin(1.0f);
        n.step(6);

    }

        /*
        n.input("<a --> b>. %1.00;0.90%\n" +
                "<b --> c>. %1.00;0.90%\n"+
                "<c --> d>. %1.00;0.90%\n" +
                "<a --> d>?");*/
    //''outputMustContain('<a --> d>. %1.00;0.27%')


}