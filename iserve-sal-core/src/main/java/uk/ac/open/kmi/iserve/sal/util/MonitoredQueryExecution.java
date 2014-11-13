package uk.ac.open.kmi.iserve.sal.util;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;
import uk.ac.open.kmi.iserve.sal.util.metrics.Metrics;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Decorator for Jena QueryExecution to register the number of execution
 * of queries.
 *
 * @author Pablo Rodríguez Mier
 */
public class MonitoredQueryExecution implements QueryExecution {
    private QueryExecution queryExecution;

    public MonitoredQueryExecution(QueryExecution queryExecution) {
        this.queryExecution = queryExecution;
    }

    @Override
    public void setFileManager(FileManager fileManager) {
        queryExecution.setFileManager(fileManager);
    }

    @Override
    public void setInitialBinding(QuerySolution querySolution) {
        queryExecution.setInitialBinding(querySolution);
    }

    @Override
    public Dataset getDataset() {
        return queryExecution.getDataset();
    }

    @Override
    public Context getContext() {
        return queryExecution.getContext();
    }

    @Override
    public Query getQuery() {
        return queryExecution.getQuery();
    }

    @Override
    public ResultSet execSelect() {
        Metrics.get().increment("SPARQL-ExecSelect");
        return queryExecution.execSelect();
    }

    @Override
    public Model execConstruct() {
        Metrics.get().increment("SPARQL-ExecConstruct");
        return queryExecution.execConstruct();
    }

    @Override
    public Model execConstruct(Model model) {
        Metrics.get().increment("SPARQL-ExecConstruct(model)");
        return queryExecution.execConstruct(model);
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        Metrics.get().increment("SPARQL-ExecConstructTriples");
        return queryExecution.execConstructTriples();
    }

    @Override
    public Model execDescribe() {
        Metrics.get().increment("SPARQL-ExecDescribe");
        return queryExecution.execDescribe();
    }

    @Override
    public Model execDescribe(Model model) {
        Metrics.get().increment("SPARQL-ExecDescribe");
        return queryExecution.execDescribe(model);
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        Metrics.get().increment("SPARQL-ExecDescribeTriples");
        return queryExecution.execDescribeTriples();
    }

    @Override
    public boolean execAsk() {
        return queryExecution.execAsk();
    }

    @Override
    public void abort() {
        queryExecution.abort();
    }

    @Override
    public void close() {
        queryExecution.close();
    }

    @Override
    public void setTimeout(long l, TimeUnit timeUnit) {
        queryExecution.setTimeout(l, timeUnit);
    }

    @Override
    public void setTimeout(long l) {
        queryExecution.setTimeout(l);
    }

    @Override
    public void setTimeout(long l, TimeUnit timeUnit, long l2, TimeUnit timeUnit2) {
        queryExecution.setTimeout(l, timeUnit, l2, timeUnit2);
    }

    @Override
    public void setTimeout(long l, long l2) {
        queryExecution.setTimeout(l, l2);
    }

    @Override
    public long getTimeout1() {
        return queryExecution.getTimeout1();
    }

    @Override
    public long getTimeout2() {
        return queryExecution.getTimeout2();
    }
}
