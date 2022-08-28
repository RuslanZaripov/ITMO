#include <iostream>
#include <vector>

const long long INF = 1e9;
int source = 0;
int sink = -1;

struct Edge
{
    int from, to;
    long long flow, maxFlow;
    Edge* backward = nullptr;

    Edge() = default;

    Edge(int from, int to, long long flow, long long maxFlow)
        : from(from), to(to), flow(flow), maxFlow(maxFlow) {}

    ~Edge() { delete backward; }
};

std::vector<std::vector<Edge*>> g;
std::vector<Edge*> edges;
std::vector<bool> visited;

void addEdge(int u, int v, int c) {
    Edge* edge = new Edge(u, v, 0, c);
    Edge* reversedEdge = new Edge(v, u, 0, c);
    edge->backward = reversedEdge;
    reversedEdge->backward = edge;
    g[u].push_back(edge);
    g[v].push_back(reversedEdge);
    edges.push_back(edge);
    edges.push_back(reversedEdge);
}


long long dfs(int node, long long minC)
{
    if (node == sink)
    {
        return minC;
    }
    visited[node] = true;
    for (Edge* edge : g[node])
    {
        if (!visited[edge->to] && edge->flow < edge->maxFlow)
        {
            long long delta = dfs(edge->to, std::min(minC, edge->maxFlow - edge->flow));
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

void dfs(int node)
{
    if (visited[node])
    {
        return;
    }
    visited[node] = true;
    for (Edge* edge : g[node])
    {
        if (!visited[edge->to] && edge->flow < edge->maxFlow)
        {
            dfs(edge->to);
        }
    }
}

int main()
{
    int n, m;
    std::cin >> n >> m;
    sink = n - 1;
    g.resize(n);

    for (int i = 0; i < m; i++)
    {
        int u, v;
        long long c;
        std::cin >> u >> v >> c;
        u -= 1;
        v -= 1;
        addEdge(u, v, c);
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

    visited.assign(n, false);
    dfs(source);

    std::vector<int> ans;
    for (int i = 0; i < edges.size(); i += 2)
    {
        bool isFromVisited = visited[edges[i]->from];
        bool isToVisited = visited[edges[i]->to];
        if ((isFromVisited && !isToVisited) || (!isFromVisited && isToVisited))
        {
            ans.push_back(i / 2);
        }
    }

    std::cout << ans.size() << ' ' << maxFlow << std::endl;
    for (int edgeIndex : ans)
    {
        std::cout << edgeIndex + 1 << ' ';
    }
    std::cout << std::endl;
}