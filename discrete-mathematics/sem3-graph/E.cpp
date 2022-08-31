#include <iostream>
#include <vector>
#include <queue>

int main()
{
    int n;
    std::cin >> n;

    std::vector<std::vector<int>> adjList(n);
    for (int i = 0; i < n - 1; ++i)
    {
        int u, v;
        std::cin >> u >> v;
        u -= 1;
        v -= 1;
        adjList[u].push_back(v);
        adjList[v].push_back(u);
    }

    std::vector<int> nodeDegree(n, -1);
    std::vector<bool> removed(n, false);
    std::priority_queue<int, std::vector<int>, std::greater<int>> leavesList;
    for (int i = 0; i < n; ++i)
    {
        nodeDegree[i] = adjList[i].size();
        if (nodeDegree[i] == 1)
        {
            leavesList.push(i);
        }
    }

    std::vector<int> pruferCode;
    for (int i = 0; i < n - 2; ++i)
    {
        int minimalLeaf = leavesList.top();
        leavesList.pop();
        removed[minimalLeaf] = true;

        for (int adjNode : adjList[minimalLeaf])
        {
            if (!removed[adjNode])
            {
                pruferCode.push_back(adjNode + 1);
                nodeDegree[adjNode] -= 1;
                if (nodeDegree[adjNode] == 1)
                {
                    leavesList.push(adjNode);
                }
            }
        }
    }

    for (int number : pruferCode)
    {
        std::cout << number << " ";
    }
}