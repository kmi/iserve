/*
 * $Id: levenshtein.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.isparql.apf.simpack;

import simpack.accessor.string.StringAccessor;
import simpack.measure.sequence.Levenshtein;
import simpack.util.conversion.WorstCaseDistanceConversion;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.Binding1;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.expr.NodeValue;
import com.hp.hpl.jena.query.pfunction.PropFuncArg;
import com.hp.hpl.jena.query.pfunction.PropertyFunctionBase;

public class levenshtein extends PropertyFunctionBase {

	@Override
	public QueryIterator exec(Binding binding, PropFuncArg subject,
			Node predicate, PropFuncArg object, ExecutionContext execCxt) {

		subject = subject.evalIfExists(binding);
		Node s = subject.getArg();

		object = object.evalIfExists(binding);
		Node arg1 = object.getArg(0);
		Node arg2 = object.getArg(1);

		String a = null, b = null;
		if (arg1.isLiteral() && arg2.isLiteral()) {
			a = arg1.getLiteralLexicalForm();
			b = arg2.getLiteralLexicalForm();
		} else if (arg1.isURI() && arg2.isURI()) {
			a = arg1.getLocalName();
			b = arg2.getLocalName();
		} else {
			System.out.println("Error: Node types unequal.");
			System.out.println(arg1.toString());
			System.out.println(arg2.toString());
		}

		StringAccessor sa1 = new StringAccessor(a);
		StringAccessor sa2 = new StringAccessor(b);

		Levenshtein<String> levensteinMeasure;
		levensteinMeasure = new Levenshtein<String>(sa1, sa2,
				new WorstCaseDistanceConversion());
		double sim = levensteinMeasure.getSimilarity();

		NodeValue nv = NodeValue.makeDouble(sim);
		Binding bind = new Binding1(binding, Var.alloc(s), nv.asNode());

		return new QueryIterSingleton(bind, execCxt);
	}
}
