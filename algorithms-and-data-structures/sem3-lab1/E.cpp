#include <algorithm>
#include <iostream>
#include <vector>
#include <stack>
#include <map>
#include <set>

const int INF = 1e9;
int time = 0;
int component = 0;

std::map<int, std::pair<int, int>> edgeNumber;
std::stack<std::pair<int, int>> vertexBiconnectedComponent;
std::map<std::pair<int, int>, int> edgeBelongsComponent;
std::stack<std::pair<int, int>> edgesFromRoot;
std::vector<std::vector<int>> adj_list;
std::vector<bool> visited;
std::vector<int> entryTime, parentInDFSTree, upNode;

bool equals(std::pair<int, int>& a, std::pair<int, int>& b)
{
    if (a.first == b.first && a.second == b.second)
    {
        return true;
    }
    return false;
}

void collectBiconnectedComponent(std::pair<int, int>& untilNotEqualEdge)
{
    component++;
    std::pair<int, int> topEdge = vertexBiconnectedComponent.top();
    while (!equals(topEdge, untilNotEqualEdge))
    {
        edgeBelongsComponent[topEdge] = component;
        vertexBiconnectedComponent.pop();
        topEdge = vertexBiconnectedComponent.top();
    }
    edgeBelongsComponent[topEdge] = component;
    vertexBiconnectedComponent.pop();
}

void dfs(int node)
{
    int childNumber = 0;
    upNode[node] = time;
    entryTime[node] = time++;
    visited[node] = true;
    for (int next : adj_list[node])
    {
        if (next != parentInDFSTree[node])
        {
            if (visited[next])
            {
                upNode[node] = std::min(upNode[node], entryTime[next]);
                if (entryTime[next] < entryTime[node])
                {
                    vertexBiconnectedComponent.push({ node, next });
                    // добавление кратных ребер
                }
            }
            else
            {
                vertexBiconnectedComponent.push({ node, next }); // кратные ребра в компоненту не попадут
                childNumber++;

                if (parentInDFSTree[node] == INF)
                {
                    edgesFromRoot.push({ node, next });
                }

                parentInDFSTree[next] = node;

                dfs(next);

                upNode[node] = std::min(upNode[node], upNode[next]);

                if (upNode[next] >= entryTime[node] && parentInDFSTree[node] != INF)
                {
                    std::pair<int, int> edgeFromArticulationPoints = { node, next };
                    collectBiconnectedComponent(edgeFromArticulationPoints);
                }
            }
        }
    }
    if (childNumber > 1 && parentInDFSTree[node] == INF)
    {
        while (!edgesFromRoot.empty())
        {
            std::pair<int, int> edgeFromRoot = edgesFromRoot.top();
            edgesFromRoot.pop();
            if (!vertexBiconnectedComponent.empty())
            {
                collectBiconnectedComponent(edgeFromRoot);
            }
        }
    }
}

int main()
{
    int n, m;
    std::cin >> n >> m;
    adj_list.resize(n);
    for (int i = 0; i < m; i++)
    {
        int u, v;
        std::cin >> u >> v;
        u = u - 1;
        v = v - 1;
        edgeNumber[i] = { u, v };
        adj_list[u].push_back(v);
        adj_list[v].push_back(u);
    }

    visited.assign(n, false);
    entryTime.assign(n, INF);
    parentInDFSTree.assign(n, INF);
    upNode.assign(n, INF);

    for (int i = 0; i < n; ++i)
    {
        if (!visited[i])
        {
            dfs(i);

            if (!vertexBiconnectedComponent.empty())
            {
                component++;
                while (!vertexBiconnectedComponent.empty())
                {
                    edgeBelongsComponent[vertexBiconnectedComponent.top()] = component;
                    vertexBiconnectedComponent.pop();
                }
            }
        }
    }

    std::cout << component << std::endl;
    for (int i = 0; i < m; i++)
    {
        std::pair<int, int> edge1 = edgeNumber[i];
        std::pair<int, int> edge2 = { edge1.second, edge1.first };
        if (edgeBelongsComponent.count(edge1))
        {
            std::cout << edgeBelongsComponent[edge1] << " ";
        }
        else if (edgeBelongsComponent.count(edge2))
        {
            std::cout << edgeBelongsComponent[edge2] << " ";
        }
    }
}