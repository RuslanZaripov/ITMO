#include <iostream>
#include <vector>
#include <cmath>
#include <set>
#include <algorithm>

const int INF = 1e9;
int time = 0;
std::set<int> articulationPoints;
std::vector<std::vector<int>> adj_list;
std::vector<bool> visited;
std::vector<int> entryTime, parentInDFSTree, upNode;

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
            }
            else
            {
                childNumber++;
                parentInDFSTree[next] = node;

                dfs(next);
                upNode[node] = std::min(upNode[node], upNode[next]);
                if (upNode[next] >= entryTime[node] && parentInDFSTree[node] != INF)
                {
                    articulationPoints.insert(node);
                }
            }
        }
    }
    if (childNumber > 1 && parentInDFSTree[node] == INF)
    {
        articulationPoints.insert(node);
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
        adj_list[u - 1].push_back(v - 1);
        adj_list[v - 1].push_back(u - 1);
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
        }
    }

    std::cout << articulationPoints.size() << std::endl;

    std::vector<int> ans;
    for (std::set<int>::iterator it = articulationPoints.begin(); it != articulationPoints.end(); it++)
    {
        ans.push_back((*it) + 1);
    }

    for (int point : ans)
    {
        std::cout << point << " ";
    }
}