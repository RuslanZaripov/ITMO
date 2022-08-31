#include <iostream>
#include <vector>
#include <cmath>
#include <set>

const int INF = 1e9;
int colorsCount = -INF;

void DFS(int currentNode, std::vector<std::vector<int>>& adjList, std::vector<bool>& visited, std::vector<int>& color)
{
    visited[currentNode] = true;

    std::set<int> adjColors;
    for (int nextNode : adjList[currentNode])
    {
        adjColors.insert(color[nextNode]);
    }

    for (int possibleCol = 1; possibleCol <= colorsCount; ++possibleCol)
    {
        if (!adjColors.count(possibleCol))
        {
            color[currentNode] = possibleCol;
            break;
        }
    }

    for (int nextNode : adjList[currentNode])
    {
        if (!visited[nextNode])
        {
            DFS(nextNode, adjList, visited, color);
        }
    }
}

int main()
{
    int n, m;
    std::cin >> n >> m;

    std::vector<std::vector<int>> adjList(n);
    std::vector<bool> visited(n, false);
    std::vector<int> color(n, 0);

    for (int i = 0; i < m; ++i)
    {
        int u, v;
        std::cin >> u >> v;
        u -= 1;
        v -= 1;
        adjList[u].push_back(v);
        adjList[v].push_back(u);
    }

    for (int i = 0; i < n; ++i)
    {
        colorsCount = std::max(colorsCount, static_cast<int>(adjList[i].size()));
    }

    if (colorsCount % 2 == 0)
    {
        colorsCount += 1;
    }

    DFS(0, adjList, visited, color);

    std::cout << colorsCount << std::endl;
    for (int nodeColor : color)
    {
        std::cout << nodeColor << std::endl;
    }
}