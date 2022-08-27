#include <iostream>
#include <vector>
#include <set>
#include <algorithm>

const long long INF = LLONG_MAX;

struct Edge
{
    int from, to, capacity, weight, flow = 0;
    Edge* backward = nullptr;

    Edge() = default;

    Edge(int from, int to, int capacity, int weight)
        : from(from), to(to), capacity(capacity), weight(weight) {}

    ~Edge() { delete backward; }
};

std::vector<std::vector<Edge*>> g;

void addEdges(int from, int to, int capacity, int weight) {
    Edge* edge = new Edge(from, to, capacity, weight);
    Edge* reversedEdge = new Edge(to, from, 0, -weight);
    edge->backward = reversedEdge;
    reversedEdge->backward = edge;
    g[from].push_back(edge);
    g[to].push_back(reversedEdge);
}

std::vector<long long> phi;
std::vector<Edge*> path;
std::vector<long long> dist;

void dijkstra(const int vertexCount, const int source) {
    dist.assign(vertexCount, INF);
    dist[source] = 0;
    std::set<std::pair<long long, int>> Q;
    path.resize(vertexCount, nullptr);
    Q.insert({ dist[source], source });
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

long long findMinCost(int vertexCount, int source, int target) {
    phi.assign(vertexCount, 0);
    dijkstra(vertexCount, source);
    phi = dist;

    long long minCost = 0;
    while (dist[target] != INF) {
        completePath(path[target], 1, minCost);
        dijkstra(vertexCount, source);
        for (int i = 0; i < vertexCount; i++) {
            phi[i] += dist[i];
        }
    }
    return minCost;
}

int main() {
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n;
    std::cin >> n;
    std::vector<std::vector<int>> a(n, std::vector<int>(n));
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            std::cin >> a[i][j];
        }
    }

    const int s = 0;
    const int t = 2 * n + 1;
    const int vertexCount = t - s + 1;
    g.resize(vertexCount);
    for (int i = 1; i <= n; i++) {
        addEdges(s, i, 1, 0);
        addEdges(n + i, t, 1, 0);
    }
    for (int i = 1; i <= n; i++) {
        for (int j = 1; j <= n; j++) {
            addEdges(i, n + j, 1, a[i - 1][j - 1]);
        }
    }

    std::cout << findMinCost(vertexCount, s, t) << '\n';

    for (int i = 1; i <= n; i++) {
        for (Edge* edge : g[i]) {
            if (edge->flow == 1) {
                std::cout << edge->from << ' ' << edge->to - n << '\n';
            }
        }
    }
}