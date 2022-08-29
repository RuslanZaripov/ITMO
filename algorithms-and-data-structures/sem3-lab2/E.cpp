#include <iostream>
#include <vector>

const long long INF = 7e18;

std::vector<bool> cycleNode;
std::vector<std::vector<int>> from;

struct Edge
{
    int start, end;
    long long weight;

    Edge() = default;
    Edge(int _start, int _end, long long _weight)
        : start(_start), end(_end), weight(_weight) {}
};

void dfs(int node)
{
    cycleNode[node] = true;
    for (int nextNode : from[node])
    {
        if (!cycleNode[nextNode])
        {
            dfs(nextNode);
        }
    }
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n, m, start;
    std::cin >> n >> m >> start;
    from.resize(n);
    std::vector<Edge> edgeList;
    for (int i = 0; i < m; ++i)
    {
        int a, b;
        long long weight;
        std::cin >> a >> b >> weight;
        a -= 1;
        b -= 1;
        from[a].push_back(b);
        edgeList.push_back(Edge(a, b, weight));
    }

    std::vector<long long> dist(n, INF);
    cycleNode.assign(n, false);

    dist[start - 1] = 0;
    for (int i = 1; i <= n; ++i)
    {
        for (Edge edge : edgeList)
        {
            if (dist[edge.start] >= INF)
            {
                continue;
            }
            if (dist[edge.end] > dist[edge.start] + edge.weight)
            {
                dist[edge.end] = std::max(-INF, dist[edge.start] + edge.weight);
                if (i == n)
                {
                    dfs(edge.end);
                }
            }
        }
    }

    for (int i = 0; i < n; ++i)
    {
        if (dist[i] == INF)
        {
            std::cout << "*" << std::endl;
        }
        else if (cycleNode[i])
        {
            std::cout << "-" << std::endl;
        }
        else
        {
            std::cout << dist[i] << std::endl;
        }
    }
}