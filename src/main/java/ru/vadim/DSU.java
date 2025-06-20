package ru.vadim;

import java.util.Arrays;

public class DSU {
    private int[] parent;

    public DSU(int n) {
        parent = new int[n];
        Arrays.fill(parent, -1);
    }
    public int find(int x) {
        int root = x;
        while (parent[root] >= 0) root = parent[root];
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }
    public void union(int x, int y) {
        x = find(x);
        y = find(y);
        if (x == y) return;
        if (parent[x] <= parent[y]) {
            parent[x] += parent[y];
            parent[y] = x;
        } else {
            parent[y] += parent[x];
            parent[x] = y;
        }
    }
}
