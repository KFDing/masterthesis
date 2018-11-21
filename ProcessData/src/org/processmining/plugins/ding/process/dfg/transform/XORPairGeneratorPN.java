package org.processmining.plugins.ding.process.dfg.transform;
/**
 * this class is used to deal with Petri net as input to get the xor pairs 
 * Input: Petri net
 * Output: xor pair lists
 * 
 * Then we transfer those into the long term dependency detector to discover the long-term dependency
 * The main idea is still:: 
 *    -- get all xor block list :: also branches
 *    -- create xor cluster :: Or maybe we don't need to do this ?? 
 *       ++ we need to prove if necessary, if in sequential relation, we have one xor block,
 *       ++ then we test the xor block before it.. 
 *       
 * But the decision is to leave this out at first, focus on process tree at first, 
 * because the model generated from inductive miner is like this.. I need to check the paper of Inductive Miner
 * and use the concepts
 * @author dkf
 *
 */
public class XORPairGeneratorPN {

}
