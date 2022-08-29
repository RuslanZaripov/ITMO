#include <iostream>
#include <vector>
#include <algorithm>

std::vector<std::vector<int>> adjList;
std::vector<bool> visited;
std::vector<int> grandy;

int calcGrandy(int s)
{
    std::vector<bool> sieve(adjList.size(), false);
    for (int nextNode : adjList[s])
    {
        sieve[grandy[nextNode]] = true;
    }
    for (int i = 0; i < sieve.size(); ++i)
    {
        if (!sieve[i])
        {
            return i;
        }
    }
    return sieve.size();
}

void dfs(int s)
{
    visited[s] = true;
    for (int nextNode : adjList[s])
    {
        if (!visited[nextNode])
        {
            dfs(nextNode);
        }
    }
    grandy[s] = calcGrandy(s);
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
        int start, end;
        std::cin >> start >> end;
        adjList[start - 1].push_back(end - 1);
    }

    visited.assign(n, false);
    grandy.assign(n, 0);

    for (int i = 0; i < n; ++i)
    {
        if (!visited[i])
        {
            dfs(i);
        }
    }

    for (int value : grandy)
    {
        std::cout << value << std::endl;
    }
}