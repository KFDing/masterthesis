package org.processmining.plugins.ding.baseline;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.PetrinetSemantics;
import org.processmining.models.semantics.petrinet.impl.PetrinetSemanticsFactory;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.etconformance.ETCException;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.etconformance.data.PrefixAutomaton;
import org.processmining.plugins.etconformance.data.PrefixAutomatonEdge;
import org.processmining.plugins.etconformance.data.PrefixAutomatonNode;
import org.processmining.plugins.etconformance.data.PrefixAutomatonNodeType;

import edu.uci.ics.jung.graph.DelegateTree;

public class ETChecker {
	DelegateTree<PrefixAutomatonNode,PrefixAutomatonEdge> tree;
	
	public ETChecker( XLog log, Petrinet net, Marking marking, TransEvClassMapping mapping , ETCResults res){
		

		PrefixAutomaton pA = new PrefixAutomaton(log, res, mapping);
		
		tree = pA.getTree();
		
		enrich( net, marking, mapping, res);
		
	}
	
	public void enrich(Petrinet net, Marking marking, TransEvClassMapping mapping, ETCResults res) {
		PetrinetSemantics sem = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);
		Collection<Transition> transAll = net.getTransitions();
		sem.initialize(transAll, marking);
		
		try {
			enrichRec( net,marking, mapping,tree.getRoot(),sem, res);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void enrichRec(Petrinet net, Marking marking, TransEvClassMapping mapping, PrefixAutomatonNode node, PetrinetSemantics sem, ETCResults res) throws Exception {
		
		//Get the enable transitions for the marking of the given node.
		MarksTasks enableTasks = computeEnableTasks( net, marking, mapping, sem, res);
		// = tree.getRoot();
		
		//For each child edge of Node
		//** node is one trace?? 
		
		for (PrefixAutomatonEdge edge :tree.getChildEdges(node)){
			XEventClass task = edge.getEvent();
			PrefixAutomatonNode childNode = tree.getOpposite(node, edge);
			int ix = enableTasks.tasks.indexOf(task);
			
			//Check if the task is in the available tasks
			if(ix == -1){
				//NO FITNESS
				//** how to get trace is has and assign label to it ?? So we could know that it not fit ??
				//** 
				childNode.setType(PrefixAutomatonNodeType.NON_FIT);
				res.setNonFitStates(res.getNonFitStates()+1);
				res.setnNonFitTraces(res.getnNonFitTraces()+childNode.getInstances());
				
			}else{
				//Follow the extension of the automaton for the child
				/**I leave the indeterminism undealt.. */
				enrichRec(net,enableTasks.marks.get(ix),mapping,childNode, sem, res);
				
				//Remove this tasks from the available tasks
				enableTasks.marks.remove(ix);
				enableTasks.tasks.remove(ix);
			}
		}
		
		//For the remaining available tasks, create the extended nodes
		for(XEventClass enrichedTask : enableTasks.tasks){
			PrefixAutomatonNode enrichedNode = new PrefixAutomatonNode(0);
			tree.addChild(new PrefixAutomatonEdge(enrichedTask), node, enrichedNode);
		}
	}
	
	public MarksTasks computeEnableTasks( Petrinet net, Marking marking, 
			TransEvClassMapping mapping, PetrinetSemantics sem, ETCResults res) throws Exception{
		
		//Create the result object
		MarksTasks mt = new MarksTasks();
		
		//Set the current marking in the semantic
		sem.setCurrentState(marking);
		
		//Get the executable transitions
		Collection<Transition> enableTrans = sem.getExecutableTransitions();
		
		//EventClass of invisible/skip/dummy tasks
		XEventClass dummy = EvClassLogPetrinetConnectionFactoryUI.DUMMY;
		
		//First the Visible ones
		for(Transition trans : enableTrans){
			//Get the task of the transition
			XEventClass task = mapping.get(trans);
			if(task != dummy){ //Visible task
				computeEnableTasksVisible(trans, task, mt, marking, sem, res);
			}
		}
		
		//Second the invisible ones
		for(Transition trans : enableTrans){
			//Get the task of the transition
			XEventClass task = mapping.get(trans);
			if(task == dummy){ //Invisible task
				// computeEnableTasksInvisible(trans, marking, net, sem, mapping, mt, res);
				computeEnableTasksVisible(trans, task, mt, marking, sem, res);
			}
		}
	
		return mt;
	}
	
	private void computeEnableTasksVisible(Transition trans, 
			XEventClass task, MarksTasks mt, Marking marking, 
			PetrinetSemantics sem, ETCResults res) throws IllegalTransitionException, ETCException{	
		
		//Check if it exists another enable transitions with same task
		if(mt.tasks.contains(task)){
			//INDETERMINISM
			//Because the visible are the first, if there is another task
			//with the same name, means indeterminism.
			if(res.isRandomIndet()){
				//Not do anything (we choose the first marking)
			}
			else{
				//throw new ETCException(ETCException.DETERMINISM_TXT+"Task: "+
				//		task.toString()+"Markings: "+marking.toString()+" - "+
				//		mt.marks.get(mt.tasks.indexOf(task)));
				mt.tasks.add(task);
				mt.marks.add(null);
				mt.direct.add(true);
			}
			
		}
		
		//Compute the reached marking after firing the transition
		sem.setCurrentState(marking);
		sem.executeExecutableTransition(trans);
		Marking mark = sem.getCurrentState();
		
		//Add the task and the mark to the result
		mt.tasks.add(task);
		mt.marks.add(mark);
		mt.direct.add(true);
	}
	public class MarksTasks{
		
		public List<Marking> marks;
		public List<XEventClass> tasks;
		public List<Boolean> direct;
		
		public MarksTasks(){
			marks = new LinkedList<Marking>();
			tasks = new LinkedList<XEventClass>();
			direct = new LinkedList<Boolean>();
		}
	}
	
	private Object[] cancel(PluginContext context, String msg) {
		context.log(msg);
		context.getFutureResult(0).cancel(true);
		context.getFutureResult(1).cancel(true);
		return new Object[] { null, null };
	}
}
