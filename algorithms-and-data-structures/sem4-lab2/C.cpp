#include <iostream>
#include <vector>
#include <sstream>

const long long INF = 1e9;
int source = -1;
int sink = -1;

struct Edge
{
    int from, to;
    long long maxFlow, flow;
    bool isResidual;
    Edge* backward = nullptr;

    Edge() = default;

    Edge(int from, int to, long long flow, long long maxFlow, bool isResidual)
        : from(from), to(to), flow(flow), maxFlow(maxFlow), isResidual(isResidual) {}

    ~Edge() { delete backward; }
};

std::vector<std::vector<Edge*>> g;
std::vector<bool> visited;
std::vector<int> path;

void addEdge(int u, int v) {
    Edge* edge = new Edge(u, v, 0, 1, false);
    Edge* reversedEdge = new Edge(v, u, 0, 0, true);
    edge->backward = reversedEdge;
    reversedEdge->backward = edge;
    g[u].push_back(edge);
    g[v].push_back(reversedEdge);
}

long long dfs(int node, long long minFlow)
{
    if (node == sink) return minFlow;

    visited[node] = true;
    for (Edge* edge : g[node])
    {
        if (!visited[edge->to] && edge->flow < edge->maxFlow)
        {
            long long delta = dfs(edge->to, std::min(minFlow, edge->maxFlow - edge->flow));
            if (delta > 0)
            {
                edge->flow += delta;
                edge->backward->flow -= delta;
                return delta;
            }
        }
    }
    return 0;
}

bool isFound = false;
void dfs(int node)
{
    if (node == sink)
    {
        isFound = true;
        return;
    }
    for (Edge* edge : g[node])
    {
        if (isFound) return;
        if (edge->isResidual) continue;
        if (edge->flow == 1)
        {
            edge->flow = 0;
            path.push_back(edge->to);
            dfs(edge->to);
        }
    }
}

std::stringstream paths;
bool findPath()
{
    path.push_back(source);
    dfs(source);

    for (int node : path) {
        paths << node + 1 << ' ';
    }
    paths << '\n';

    return isFound;
}

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(0);

    int n, m;
    std::cin >> n >> m >> source >> sink;
    source -= 1;
    sink -= 1;

    g.resize(n);

    for (int i = 0; i < m; i++)
    {
        int u, v;
        std::cin >> u >> v;
        u -= 1;
        v -= 1;
        if (u == v) continue;
        addEdge(u, v);
    }

    do
    {
        visited.assign(n, false);
    } while (dfs(source, INF));

    long long maxFlow = 0;
    for (Edge* edge : g[sink])
    {
        maxFlow += std::abs(edge->flow);
    }

    int pathCount = 2;
    if (maxFlow < pathCount) {
        std::cout << "NO\n";
        return 0;
    }
    while (pathCount--) {
        if (!findPath()) {
            std::cout << "NO\n";
            return 0;
        }
        isFound = false;
        path.clear();
    }
    std::cout << "YES\n";
    std::cout << paths.str() << '\n';
}