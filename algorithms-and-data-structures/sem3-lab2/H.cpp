#include <iostream>
#include <vector>
#include <algorithm>

std::vector<std::vector<int>> adjList;
std::vector<bool> used;
std::vector<int> order;

void topsort(int s)
{
    used[s] = true;
    for (int nextNode : adjList[s])
    {
        if (!used[nextNode])
        {
            topsort(nextNode);
        }
    }
    order.push_back(s);
}

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n, m, s;
    std::cin >> n >> m >> s;
    adjList.resize(n);
    std::vector<bool> wins(n, false);
    for (int i = 0; i < m; ++i)
    {
        int start, end;
        std::cin >> start >> end;
        adjList[start - 1].push_back(end - 1);
    }

    used.assign(n, false);
    topsort(s - 1);

    wins[order[0]] = false;
    for (int i = 1; i < order.size(); ++i)
    {
        for (int nextNode : adjList[order[i]])
        {
            if (!wins[nextNode])
            {
                wins[order[i]] = true;
            }
        }
    }

    std::cout << (wins[s - 1] ? "First" : "Second") << " player wins" << std::endl;
}