#include <iostream>
#include <vector>

const long long INF = 1e9;

std::vector<bool> visited;

void DFS(int currentNode, std::vector<std::vector<std::pair<int, long long>>>& adjList, long long cost)
{
    visited[currentNode] = true;
    for (std::pair<int, long long> next : adjList[currentNode])
    {
        int nextNode = next.first;
        long long edgeWeight = next.second;

        if (!visited[nextNode] && edgeWeight <= cost)
        {
            DFS(nextNode, adjList, cost);
        }
    }
}

int main()
{
    int n; // n - кол-во вершин;
    std::cin >> n;
    std::vector<std::vector<std::pair<int, long long>>> adjList(n);
    std::vector<std::vector<std::pair<int, long long>>> transposedAdjList(n);

    long long maxCost = -INF;
    for (int i = 0; i < n; ++i)
    {
        for (int j = 0; j < n; ++j)
        {
            long long cost;
            std::cin >> cost;
            maxCost = std::max(maxCost, cost);
            adjList[i].push_back({ j, cost });
            transposedAdjList[j].push_back({ i, cost });
        }
    }

    int componentCount = 0;
    int componentCountTransposed = 0;

    visited.assign(n, false);
    long long l = -1, r = maxCost;
    while (r - l > 1)
    {
        componentCount = 0;
        componentCountTransposed = 0;

        long long m = (l + r) >> 1;

        for (int i = 0; i < n; ++i)
        {
            if (!visited[i])
            {
                componentCount++;
                DFS(i, adjList, m);
            }
        }

        visited.assign(n, false);

        for (int i = 0; i < n; ++i)
        {
            if (!visited[i])
            {
                componentCountTransposed++;
                DFS(i, transposedAdjList, m);
            }
        }

        visited.assign(n, false);

        (componentCount == 1 && componentCountTransposed == 1) ? r = m : l = m;
    }

    std::cout << r << std::endl;
}