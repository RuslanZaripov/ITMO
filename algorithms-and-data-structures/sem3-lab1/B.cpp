#include <iostream>
#include <vector>
#include <cmath>
#include <map>
#include <algorithm>

const int INF = 1e9;
int time = 0;

std::map<std::pair<int, int>, int> edgeNumber;
std::vector<std::pair<int, int>> ans;
std::vector<std::vector<int>> adj_list;
std::vector<int> entryTime, parentInDFSTree, upNode;
std::vector<bool> visited;

void dfs(int node)
{
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
                parentInDFSTree[next] = node;
                dfs(next);
                upNode[node] = std::min(upNode[node], upNode[next]);
                if (upNode[next] > entryTime[node])
                {
                    ans.push_back({ node + 1, next + 1 });
                }
            }
        }
    }
}

int main()
{
    int n, m;
    std::cin >> n >> m;
    adj_list.resize(n);
    for (int i = 0; i < m; ++i)
    {
        int u, v;
        std::cin >> u >> v;
        edgeNumber[std::make_pair(u, v)] = i + 1;
        edgeNumber[std::make_pair(v, u)] = i + 1;
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

    std::cout << ans.size() << std::endl;
    std::vector<int> bridgesNumbers;
    for (std::pair<int, int> edge : ans)
    {
        bridgesNumbers.push_back(edgeNumber[edge]);
    }

    std::sort(bridgesNumbers.begin(), bridgesNumbers.end());
    for (int bridgeNumber : bridgesNumbers)
    {
        std::cout << bridgeNumber << std::endl;
    }
}