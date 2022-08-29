#include <iostream>
#include <vector>
#include <map>
#include <stack>

const int INF = 1e9;
int time = 0;
int component = 0;

std::map<std::pair<int, int>, int> edgesNumber;
std::map<int, int> vertexBelongComponent;
std::stack<int> biconnectedComponent;
std::vector<std::vector<int>> adj_list;
std::vector<bool> visited;
std::vector<int> entryTime, parentInDFSTree, upNode;

void dfs(int node)
{
    biconnectedComponent.push(node);

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
                if ((edgesNumber[std::make_pair(node, next)] == 1 || edgesNumber[std::make_pair(next, node)] == 1) &&
                    entryTime[node] < upNode[next])
                {
                    component++;
                    int top = biconnectedComponent.top();
                    while (top != next)
                    {
                        vertexBelongComponent[top] = component;
                        biconnectedComponent.pop();
                        top = biconnectedComponent.top();
                    }
                    vertexBelongComponent[top] = component;
                    biconnectedComponent.pop();
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
        v = v - 1;
        u = u - 1;
        if (u == v)
        {
            continue;
        }
        if (edgesNumber[std::make_pair(v, u)] >= 1)
        {
            edgesNumber[std::make_pair(v, u)]++;
        }
        else
        {
            edgesNumber[std::make_pair(u, v)]++;
        }
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
            component++;
            while (!biconnectedComponent.empty())
            {
                vertexBelongComponent[biconnectedComponent.top()] = component;
                biconnectedComponent.pop();
            }
        }
    }

    std::cout << component << std::endl;
    for (int i = 0; i < n; i++)
    {
        std::cout << vertexBelongComponent[i] << " ";
    }
}