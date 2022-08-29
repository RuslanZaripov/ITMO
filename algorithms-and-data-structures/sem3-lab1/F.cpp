#include <iostream>
#include <vector>
#include <algorithm>
#include <stack>
#include <set>
#include <map>

int component = 0;
std::map<int, int> vertexComponent;

int edgeNumberInCondensation = 0;

std::vector<std::set<int>> transposedAdjList;
std::set<int> connectedWithComponents;
std::vector<std::set<int>> adjList;
std::vector<bool> visited;
std::vector<int> order;

void grapthDFS(int currentNode)
{
    visited[currentNode] = true;
    for (int next : adjList[currentNode])
    {
        if (!visited[next])
        {
            grapthDFS(next);
        }
    }
    order.push_back(currentNode);
}

void transposedGrapthDFS(int node)
{
    visited[node] = true;
    vertexComponent[node] = component;
    for (int next : transposedAdjList[node])
    {
        if (!visited[next])
        {
            transposedGrapthDFS(next);
        }
        else if (vertexComponent[next] != component)
        {
            connectedWithComponents.insert(vertexComponent[next]);
        }
    }
}

int main()
{
    int n, m; // n - кол-во вершин, m - кол-во ребер;
    std::cin >> n >> m;
    adjList.resize(n);
    transposedAdjList.resize(n);
    for (int i = 0; i < m; ++i)
    {
        int u, v;
        std::cin >> u >> v;
        u = u - 1;
        v = v - 1;
        adjList[u].insert(v);
        transposedAdjList[v].insert(u);
    }
    visited.assign(n, false);
    for (int i = 0; i < n; ++i)
    {
        if (!visited[i])
        {
            grapthDFS(i);
        }
    }

    std::reverse(order.begin(), order.end());

    for (int i = 0; i < visited.size(); ++i)
    {
        visited[i] = false;
    }

    for (int node : order)
    {
        if (!visited[node])
        {
            component++;
            transposedGrapthDFS(node);
            edgeNumberInCondensation += connectedWithComponents.size();
            connectedWithComponents.clear();
        }
    }

    std::cout << edgeNumberInCondensation << std::endl;
}