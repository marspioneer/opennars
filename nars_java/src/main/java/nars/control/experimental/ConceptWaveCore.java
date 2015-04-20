/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control.experimental;

import nars.Core;
import nars.Memory;
import nars.Memory.MemoryAware;
import nars.nal.BudgetFunctions;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.term.Term;
import nars.budget.bag.experimental.DelayBag;
import nars.budget.bag.experimental.FairDelayBag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Uses DelayBag to emulate a massively parallel spiking neural network of concept activation
 * 
 * Designed for use in parallel processing
 * 
 * Named "Wave" core because its concept-firing timing resembles spiking
 * brainwaves 
 */
abstract public class ConceptWaveCore implements Core {
    

    public DelayBag<Term, Concept> concepts;
    //public final CacheBag<Term, Concept> subcon;
    
    Memory memory;
    List<Runnable> run = new ArrayList();

    private final int maxConcepts;
               
    public ConceptWaveCore(int maxConcepts) {
        this.maxConcepts = maxConcepts;
        //this.subcon = subcon
    }    

    @Override
    abstract public void cycle();


    @Override
    public void reset() {
        concepts.clear();
    }

    @Override
    public Concept concept(Term term) {
        return concepts.get(term);
    }

    @Override
    public int size() {
        return concepts.size();
    }

    @Override
    public Concept conceptualize(Budget budget, Term term, boolean createIfMissing) {
        Concept c = concept(term);
        if (c!=null) {
            //existing
            BudgetFunctions.activate(c.budget, budget, BudgetFunctions.Activating.Max);
        }
        else {
            if (createIfMissing)
                c = memory.newConcept(budget, term);
            if (c == null)
                return null;
            concepts.put(c);
        }
        return c;
    }

    @Override
    public void activate(Concept c, Budget b, BudgetFunctions.Activating mode) {
        conceptualize(b, c.term, false);
    }

    @Override
    public Concept nextConcept() {
        return concepts.peekNext();
    }

    @Override
    public void init(Memory m) {
        this.memory = m;
        
        this.concepts = new FairDelayBag(memory.param.conceptForgetDurations, maxConcepts);      
        
        if (concepts instanceof MemoryAware)
            concepts.setMemory(m);
        if (concepts instanceof CoreAware)
            concepts.setCore(this);
    }

    @Override
    public void conceptRemoved(Concept c) {
    
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }


    @Override
    public String toString() {
        return super.toString() + "[" + concepts.toString() + "]";
    }

    @Override
    public Memory getMemory() {
        return memory;
    }
    
}