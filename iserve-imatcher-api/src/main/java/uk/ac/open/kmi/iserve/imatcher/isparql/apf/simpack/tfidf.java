/*
 * $Id: tfidf.java 448 2007-12-10 17:01:19Z kiefer $
 *
 * Created by Christoph Kiefer, kiefer@ifi.uzh.ch
 *
 * See LICENSE for more information about licensing and warranties.
 */
package uk.ac.open.kmi.iserve.imatcher.isparql.apf.simpack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import uk.ac.open.kmi.iserve.imatcher.IServeIMatcher;
import uk.ac.open.kmi.iserve.imatcher.tool.MITPHv1CorpusForIServe;

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

public class tfidf extends PropertyFunctionBase {

//	@SuppressWarnings("unchecked")
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
			a = MITPHv1CorpusForIServe.clean(arg1.getLiteralLexicalForm());
			b = MITPHv1CorpusForIServe.clean(arg2.getLiteralLexicalForm());
		} else {
			System.out.println("Error: Node types unequal.");
			System.out.println(arg1.toString());
			System.out.println(arg2.toString());
		}

		double sim = IServeIMatcher.getInstance().TFIDF.getSimilarity(a, b);

		if ( Double.isNaN(sim) ) {
			sim = 0.0;
		}

		Locale.setDefault(Locale.US);
		NumberFormat formatter = new DecimalFormat("0.000000000");
		String sim2 = formatter.format(sim);
		NodeValue nv = NodeValue.makeDouble(Double.valueOf(sim2));
		Binding bind = new Binding1(binding, Var.alloc(s), nv.asNode());

		return new QueryIterSingleton(bind, execCxt);
	}
}
