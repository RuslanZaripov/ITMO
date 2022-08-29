#include <iostream>
#include <vector>

const int INF = 1e9;

int main()
{
    int n, m, k, s;
    std::cin >> n >> m >> k >> s;
    std::vector<std::vector<std::pair<int, long long>>> adjList(n);
    for (int i = 0; i < m; ++i)
    {
        int a, b;
        long long w;
        std::cin >> a >> b >> w;
        adjList[b - 1].push_back({ a - 1, w });
    }

    std::vector<std::vector<long long>> dist(k + 1, std::vector<long long>(n, INF));
    dist[0][s - 1] = 0;
    for (int i = 1; i <= k; ++i)
    {
        for (int u = 0; u < n; ++u)
        {
            for (std::pair<int, long long> inEdge : adjList[u])
            {
                int fromVertex = inEdge.first;
                long long weight = inEdge.second;

                if (dist[i - 1][fromVertex] == INF)
                {
                    continue;
                }

                dist[i][u] = std::min(dist[i][u], dist[i - 1][fromVertex] + weight);
            }
        }
    }

    for (int i = 0; i < n; ++i)
    {
        long long ans = dist[k][i];
        std::cout << ((ans != INF) ? ans : -1) << std::endl;
    }
}