#include <iostream>
#include <vector>
#include <algorithm>

std::vector<std::vector<int>> adj_list;
std::vector<int> status;
std::vector<int> ans;

bool dfs(int vertex)
{
    status[vertex] = 1;
    for (int next : adj_list[vertex])
    {
        if (status[next] == 0)
        {
            if (dfs(next))
            {
                return true;
            }
        }
        else if (status[next] == 1)
        {
            return true;
        }
    }
    status[vertex] = 2;
    ans.push_back(vertex);
    return false;
}

int main()
{
    int n, m; // n - кол-во вершин, m - кол-во ребер;
    std::cin >> n >> m;
    adj_list.resize(n);
    for (int i = 0; i < m; ++i)
    {
        int u, v;
        std::cin >> u >> v;
        adj_list[u - 1].push_back(v - 1);
    }

    status.assign(n, false);
    for (int i = 0; i < n; ++i)
    {
        if (status[i] == 0)
        {
            if (dfs(i))
            {
                std::cout << -1 << std::endl;
                return 0;
            }
        }
    }

    std::reverse(ans.begin(), ans.end());

    for (int node : ans)
    {
        std::cout << node + 1 << " ";
    }
    return 0;
}