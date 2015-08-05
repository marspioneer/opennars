/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Global;
import nars.Memory;
import nars.premise.Premise;
import nars.task.Task;

import java.util.List;

/**
 * NAL Reasoner Process.  Includes all reasoning process state and common utility methods that utilize it.
 * <p>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operate it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL implements Runnable, Premise {

    public final Memory memory;


    /** derivation queue (this might also work as a Set) */
    protected List<Task> derived = null;


    /**
     * stores the tasks that this process generates, and adds to memory
     */
    //protected SortedSet<Task> newTasks; //lazily instantiated
    public NAL(final Memory m) {
        this.memory = m;
    }


    @Override public void run() {

        beforeDerive();

        derive();

        afterDerive();

    }

    /** implement if necessary in subclasses */
    protected void beforeDerive() {

    }

    /** implement if necessary in subclasses */
    protected void afterDerive() {

    }

    /** run the actual derivation process */
    protected abstract void derive();


    @Override
    public Memory getMemory() {
        return memory;
    }


    public Task getBelief() {
        return null;
    }

    @Override public void queue(Task derivedTask) {
        if (derived == null)
            derived = Global.newArrayList(1);

        derived.add(derivedTask);
    }

}
