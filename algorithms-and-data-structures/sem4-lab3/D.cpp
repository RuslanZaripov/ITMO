#include <iostream>
#include <vector>
#include <set>
#include <algorithm>

const long long INF = INT_MAX;

struct Edge
{
    int from{}, to{}, capacity{}, flow = 0;
    long long weight{};
    Edge* backward = nullptr;

    Edge() = default;

    Edge(int from, int to, int capacity, long long weight)
        : from(from), to(to), capacity(capacity), weight(weight) {}

    ~Edge() { delete backward; }
};

std::vector<std::vector<Edge*>> g;

void addEdges(int _from, int _to, int capacity, long long weight) {
    Edge* edge = new Edge(_from, _to, capacity, weight);
    Edge* reversedEdge = new Edge(_to, _from, 0, -weight);
    edge->backward = reversedEdge;
    reversedEdge->backward = edge;
    g[_from].push_back(edge);
    g[_to].push_back(reversedEdge);
}

std::vector<long long> phi;
std::vector<Edge*> path;
std::vector<long long> dist;

void dijkstra(const int vertexCount, const int s) {
    dist.assign(vertexCount, INF);
    dist[s] = 0;
    std::set<std::pair<long long, int>> Q;
    path.resize(vertexCount, nullptr);
    Q.insert({ dist[s], s });
    while (!Q.empty()) {
        int from = Q.begin()->second;
        Q.erase(Q.begin());

        for (Edge* edge : g[from]) {
            const int to = edge->to;
            long long new_weight = edge->weight + phi[from] - phi[to];

            if (edge->flow < edge->capacity && dist[from] + new_weight < dist[to]) {
                Q.erase({ dist[to], to });
                dist[to] = dist[from] + new_weight;
                path[to] = edge;
                Q.insert({ dist[to], to });
            }
        }
    }
}

int completePath(Edge* edge, const int flow, long long& minCost) {
    if (edge == nullptr) {
        return flow;
    }
    int delta = completePath(path[edge->from], std::min(flow, edge->capacity - edge->flow), minCost);
    minCost += edge->weight;
    edge->flow += delta;
    edge->backward->flow -= delta;
    return delta;
}

long long findMinCost(const int s, const int t) {
    int vertexCount = t - s + 1;

    phi.assign(vertexCount, 0);
    dijkstra(vertexCount, s);
    phi = dist;

    long long minCost = 0;
    while (dist[t] != INF) {
        completePath(path[t], 1, minCost);
        dijkstra(vertexCount, s);
        for (int i = 0; i < vertexCount; i++) {
            phi[i] += dist[i];
        }
    }
    return minCost;
}

std::vector<std::vector<long long>> a;

void calcMinDist(const int n) {
    for (int k = 0; k < n; k++) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = std::min(a[i][j], a[i][k] + a[k][j]);
            }
        }
    }
}

void buildGraph(const int n, int& s, int& t) {
    s = 0;
    t = 2 * n + 1;
    g.resize(t - s + 1);

    for (int i = 1; i <= n; i++) {
        addEdges(s, i, 1, 0);
        addEdges(n + i, t, 1, 0);
    }
    calcMinDist(n);
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            addEdges(1 + i, 1 + j + n, 1, a[i][j]);
        }
    }
}

int main() {
    int n, m;
    std::cin >> n >> m;

    a.resize(n, std::vector<long long>(n, INF));
    for (int i = 0; i < n; i++) {
        std::cin >> a[i][i];
    }
    for (int i = 0; i < m; i++) {
        int u, v, cost;
        std::cin >> u >> v >> cost;
        u -= 1; v -= 1;
        a[u][v] = cost;
    }

    int s, t;
    buildGraph(n, s, t);
    std::cout << findMinCost(s, t) << '\n';
}
