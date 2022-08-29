#include <algorithm>
#include <iostream>
#include <vector>
#include <queue>
#include <map>

const long long INF = 1e17;

std::vector<std::vector<std::pair<int, long long>>> adjList;

struct CompareVertexData
{
    bool operator()(std::pair<int, long long> v1, std::pair<int, long long> v2)
    {
        return v1.second > v2.second;
    }
};

std::vector<long long> dijkstra(int start)
{
    int n = adjList.size();

    std::vector<long long> shortestDist(n, INF);
    std::vector<int> visited(n, false);
    std::priority_queue<std::pair<int, long long>,
        std::vector<std::pair<int, long long>>,
        CompareVertexData>
        vertexQueue;

    vertexQueue.push({ start, 0 });
    shortestDist[start] = 0;
    while (!vertexQueue.empty())
    {
        int current = vertexQueue.top().first;
        long long weight = vertexQueue.top().second;
        vertexQueue.pop();

        if (visited[current])
        {
            continue;
        }
        visited[current] = true;

        for (std::pair<int, long long> nextInfo : adjList[current])
        {
            int next = nextInfo.first;
            long long weight = nextInfo.second;

            if (shortestDist[current] + weight < shortestDist[next])
            {
                shortestDist[next] = shortestDist[current] + weight;
                vertexQueue.push({ next, shortestDist[next] });
            }
        }
    }

    return shortestDist;
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n, m;
    std::cin >> n >> m;

    adjList.resize(n);

    for (int i = 0; i < m; ++i)
    {
        int u, v;
        long long w;
        std::cin >> u >> v >> w;
        v -= 1;
        u -= 1;
        adjList[u].push_back({ v, w });
        adjList[v].push_back({ u, w });
    }

    std::map<int, std::vector<long long>> shortPathFromVertex;
    std::vector<int> houses;
    for (int i = 0; i < 3; ++i)
    {
        int house;
        std::cin >> house;
        house -= 1;
        houses.push_back(house);
        shortPathFromVertex[house] = dijkstra(house);
    }

    long long result = INF;

    std::sort(houses.begin(), houses.end());
    do
    {
        result = std::min(result, shortPathFromVertex[houses[0]][houses[1]] + shortPathFromVertex[houses[1]][houses[2]]);

    } while (std::next_permutation(houses.begin(), houses.end()));

    std::cout << (result < INF ? result : -1) << std::endl;
}