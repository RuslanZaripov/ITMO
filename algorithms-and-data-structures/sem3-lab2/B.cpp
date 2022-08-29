#include <iostream>
#include <vector>
#include <queue>

const int INF = 1e9;

struct CompareVertexData
{
    bool operator()(std::pair<int, int> v1, std::pair<int, int> v2)
    {
        return v1.second > v2.second;
    }
};

int main()
{
    int n, m;
    std::cin >> n >> m;

    std::vector<std::vector<std::pair<int, int>>> adjList(n);

    for (int i = 0; i < m; ++i)
    {
        int u, v, w;
        std::cin >> u >> v >> w;
        v -= 1;
        u -= 1;
        adjList[u].push_back({ v, w });
        adjList[v].push_back({ u, w });
    }

    std::vector<int> shortestDist(n, INF);
    std::vector<int> visited(n, false);
    std::priority_queue<std::pair<int, int>,
        std::vector<std::pair<int, int>>,
        CompareVertexData>
        vertexQueue;

    vertexQueue.push({ 0, 0 });
    shortestDist[0] = 0;
    while (!vertexQueue.empty())
    {
        int current = vertexQueue.top().first;
        int weight = vertexQueue.top().second;
        vertexQueue.pop();

        if (visited[current])
        {
            continue;
        }
        visited[current] = true;

        for (std::pair<int, int> nextInfo : adjList[current])
        {
            int next = nextInfo.first;
            int weight = nextInfo.second;

            if (shortestDist[current] + weight < shortestDist[next])
            {
                shortestDist[next] = shortestDist[current] + weight;
                vertexQueue.push({ next, shortestDist[next] });
            }
        }
    }

    for (int dist : shortestDist)
    {
        std::cout << dist << " ";
    }
}