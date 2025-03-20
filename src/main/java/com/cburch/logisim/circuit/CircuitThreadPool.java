package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
                                        new SynchronousQueue<>());
    }

    public HashSet<Component> propagatePoints(CircuitState state, ArrayList<Location> dirty) {
        HashSet<Component> ret = new HashSet<>();
        if (!dirty.isEmpty()) {
            ArrayList<Future<HashSet<Component>>> list = new ArrayList<>();
            int q = dirty.size() / Runtime.getRuntime().availableProcessors();
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
                try {
                    if (i + 1 >= Runtime.getRuntime().availableProcessors()) {
                        list.add(threadPool.submit(new PointsWorker(state, new HashSet<>(dirty.subList(q * i, dirty.size())))));
                        continue;
                    }
                    list.add(threadPool.submit(new PointsWorker(state, new HashSet<>(dirty.subList(q * i, q * (i+1))))));
                } catch (Exception e) {
                    i--;
                }

            }


            for (Future<HashSet<Component>> future : list) {
                try {
                    HashSet<Component> result = future.get();
                    ret.addAll(result);
                } catch (Exception e) {

                }
            }
        }

        return ret;
    }
}
