package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.*;
import java.util.concurrent.*;

public class CircuitThreadPool {
    static class Struct {
        public CircuitState state;
        public Location pt;
        public Value val;
        public Component cause;
        public int delay;

        public Struct(CircuitState state, Location pt, Value val, Component cause, int delay) {
            this.state = state;
            this.pt = pt;
            this.val = val;
            this.cause = cause;
            this.delay = delay;
        }
    }

    static private CircuitThreadPool instance = null;

    private ThreadPoolExecutor threadPool;
    private int countProcessor = Runtime.getRuntime().availableProcessors();

    static public CircuitThreadPool getInstance() {
        if (instance == null) instance = new CircuitThreadPool();

        return instance;
    }

    private CircuitThreadPool() {
        threadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                                        Runtime.getRuntime().availableProcessors(),
                                        0,
                                        TimeUnit.NANOSECONDS,
                                        new LinkedBlockingQueue<>());
    }

    private HashMap<CircuitState, ArrayList<Future<HashSet<Component>>>> results = new HashMap<>();
    private HashMap<CircuitState, ArrayList<Future<HashSet<Struct>>>> resultsComponents = new HashMap<>();

    public void propagatePoints(CircuitState state, ArrayList<Location> dirty) {
        if (!dirty.isEmpty()) {
            ArrayList<Future<HashSet<Component>>> list = new ArrayList<>();
            int q = dirty.size() / countProcessor;
            if (q < 20) {
                q = 20;
            }
            for (int i = 0; i < dirty.size(); i += q) {
                if (i + q >= dirty.size()) {
                    list.add(threadPool.submit(new PointsWorker(state, new HashSet<>(dirty.subList(i, dirty.size())))));
                    continue;
                }
                list.add(threadPool.submit(new PointsWorker(state, new HashSet<>(dirty.subList(i, i + q)))));


            }

            results.put(state, list);
        }
    }

    public void summarize() {
        for (CircuitState state : results.keySet()) {
            ArrayList<Future<HashSet<Component>>> list = results.get(state);

            for (Future<HashSet<Component>> future : list) {
                try {
                    state.markComponentsDirty(future.get());
                } catch (Exception e) {

                }
            }
        }

        results.clear();
    }

    public void propagateComponents(CircuitState state, Object[] toProcess) {
        if (toProcess.length == 0) return;

        ArrayList<Future<HashSet<Struct>>> list = new ArrayList<>();
        int q = toProcess.length / countProcessor;
        if (q < 15) {
            q = 15;
        }

        for (int i = 0; i < toProcess.length; i += q) {
            if (i + q >= toProcess.length) {
                list.add(threadPool.submit(new ComponentWorker(state, Arrays.copyOfRange(toProcess, i, toProcess.length))));
                continue;
            }
            list.add(threadPool.submit(new ComponentWorker(state, Arrays.copyOfRange(toProcess, i, i + q))));
        }

        resultsComponents.put(state, list);
    }

    public void summarizeComponents(Propagator propagator) {
        for (CircuitState state : resultsComponents.keySet()) {
            ArrayList<Future<HashSet<Struct>>> list = resultsComponents.get(state);

            for (Future<HashSet<Struct>> future : list) {
                try {
                    HashSet<Struct> set = future.get();
                    for (Struct res : set) {
                        if (propagator != null) propagator.setValue(res.state, res.pt, res.val, res.cause, res.delay);
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }




        resultsComponents.clear();
    }
}
