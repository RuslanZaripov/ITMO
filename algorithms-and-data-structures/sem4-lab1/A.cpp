#include <algorithm>
#include <iostream>
#include <fstream>
#include <vector>

std::vector<std::vector<int>> adjList;
std::vector<int> l, r;
std::vector<bool> used;

bool dfs(int vertex)
{
    if (used[vertex])
    {
        return false;
    }
    used[vertex] = true;
    for (int next : adjList[vertex])
    {
        if (r[next] == -1 || dfs(r[next]))
        {
            r[next] = vertex;
            l[vertex] = next;
            return true;
        }
    }
    return false;
}

int main()
{
    std::ios::sync_with_stdio(false);
    std::cin.tie(nullptr);

    int n, m;
    std::cin >> n >> m;

    adjList.resize(n);
    for (int v_A = 0; v_A < n; ++v_A)
    {
        int v_B = -1;
        std::cin >> v_B;
        while (v_B != 0)
        {
            adjList[v_A].push_back(v_B - 1);
            std::cin >> v_B;
        }
    }

    l.resize(n, -1);
    r.resize(m, -1);
    for (int v_A = 0; v_A < n; ++v_A)
    {
        used.assign(n, false);
        dfs(v_A);
    }

    int size = 0;
    for (int v_A = 0; v_A < n; ++v_A)
    {
        if (l[v_A] != -1)
        {
            size += 1;
        }
    }

    std::cout << size << std::endl;
    for (int v_A = 0; v_A < n; ++v_A)
    {
        if (l[v_A] != -1)
        {
            std::cout << v_A + 1 << ' ' << l[v_A] + 1 << std::endl;
        }
    }
}