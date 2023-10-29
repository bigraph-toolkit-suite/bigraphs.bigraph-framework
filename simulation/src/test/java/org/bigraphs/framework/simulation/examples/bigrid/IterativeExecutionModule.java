package org.bigraphs.framework.simulation.examples.bigrid;

//TODO: this class represents an "executable behavior" based on an algorithm
//maybe only an execute method is sufficient... not yet clear what is common or not.
// or how algos need the params in which way.
//every subclass may accept different combinations
public class IterativeExecutionModule {

    //All are iterative algorithms

//    DEFAULT:
    //Here Convergence criteria and movementstrategies are taken
    //E:G: for each criteria execute the resp. MvmtStrat.

    //OR: A* related

    //Downhill-Simplex Verfahren

    //params: the length of the seq of convergence criteria determine the iteration count for MC
    //when finished execute a new MC on new BRS with best states

}
