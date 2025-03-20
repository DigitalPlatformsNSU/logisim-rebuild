package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.*;

public class CircuitThreadPool {
    static private CircuitThreadPool instance = null;

    private ThreadPoolExecutor threadPool;

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
}
