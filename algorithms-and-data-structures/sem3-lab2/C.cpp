#include <iostream>
#include <vector>
#include <set>
#include <algorithm>

const long long INF = 1e11;

struct Edge
{
    int start;
    int end;
    long long weight;

    Edge() = default;
    Edge(int _start, int _end, long long _weight)
    {
        start = _start;
        end = _end;
        weight = _weight;
    }
};

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n;
    std::cin >> n;

    std::vector<Edge> edgeList;

    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < n; ++j)
        {
            long long weight;
            std::cin >> weight;
            if (weight != 100000)
            {
                edgeList.push_back(Edge(i, j, weight));
            }
        }
    }

    std::vector<int> parent(n, -1);
    std::vector<long long> dist(n, INF);
    dist[0] = 0;
    int fixedNode;
    for (int i = 1; i <= n; ++i)
    {
        fixedNode = -1;
        for (Edge edge : edgeList)
        {
            if (dist[edge.end] > dist[edge.start] + edge.weight)
            {
                dist[edge.end] = std::max(-INF, dist[edge.start] + edge.weight);
                parent[edge.end] = edge.start;
                fixedNode = edge.end;
            }
        }
    }

    if (fixedNode == -1)
    {
        std::cout << "NO" << std::endl;
    }
    else
    {
        int node = fixedNode;
        for (int i = 1; i <= n; ++i)
        {
            node = parent[node];
        }

        std::vector<int> cycle;
        std::set<int> visited;
        visited.insert(node);
        cycle.push_back(node);
        while (!visited.count(parent[node]))
        {
            node = parent[node];
            visited.insert(node);
            cycle.push_back(node);
        }
        std::reverse(cycle.begin(), cycle.end());

        std::cout << "YES" << std::endl;
        std::cout << cycle.size() << std::endl;
        for (int cycleNode : cycle)
        {
            std::cout << cycleNode + 1 << " ";
        }
    }
}