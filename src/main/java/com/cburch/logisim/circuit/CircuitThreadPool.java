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
    static class Struct implements Serializable {
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

    private Pipe pipe;
    private Pipe.SinkChannel pipeSink;
    private Pipe.SourceChannel pipeSource;

    public Pipe.SinkChannel getObjectOutputStream() {
        return pipeSink;
    }

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

        try {
            pipe = Pipe.open();
            pipeSink = pipe.sink();
            pipeSource = pipe.source();
            pipeSource.configureBlocking(false);
        } catch (Exception e) {
        }

    }

    private HashMap<CircuitState, ArrayList<Future<HashSet<Component>>>> results = new HashMap<>();
    private HashMap<CircuitState, ArrayList<Future<HashSet<Struct>>>> resultsComponents = new HashMap<>();

    public void propagatePoints(CircuitState state, ArrayList<Location> dirty) {
        if (!dirty.isEmpty()) {
            ArrayList<Future<HashSet<Component>>> list = new ArrayList<>();
            int q = dirty.size() / Runtime.getRuntime().availableProcessors();
            if (q < 15) {
                q = 15;
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
            HashSet<Component> components = new HashSet<>();

            for (Future<HashSet<Component>> future : list) {
                try {
                    components.addAll(future.get());
                } catch (Exception e) {

                }
            }

            state.markComponentsDirty(components);
        }

        results.clear();
    }

    public void propagateComponents(CircuitState state, Object[] toProcess) {
        if (toProcess.length == 0) return;

        ArrayList<Future<HashSet<Struct>>> list = new ArrayList<>();
        int q = toProcess.length / Runtime.getRuntime().availableProcessors();
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

            HashSet<Struct> set = new HashSet<>();
            for (Future<HashSet<Struct>> future : list) {
                try {
                    set.addAll(future.get());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            for (Struct res : set) {
                if (propagator != null) propagator.setValue(res.state, res.pt, res.val, res.cause, res.delay);
            }



            }




        resultsComponents.clear();
    }
}
