#include <iostream>
#include <vector>
#include <queue>

int main()
{
    std::ios_base::sync_with_stdio(false);
    std::cin.tie(NULL);

    int n;
    std::cin >> n;

    std::vector<std::vector<int>> adjMatrix(n, std::vector<int>(n, 0));
    for (int i = 1; i < n; ++i)
    {
        std::string input;
        std::cin >> input;
        for (int j = 0; j < input.size(); ++j)
        {
            input[j] == '1' ? adjMatrix[i][j] = 1 : adjMatrix[j][i] = 1;
        }
    }

    std::deque<int> hamiltonPath;

    hamiltonPath.push_back(0);
    for (int node = 1; node < n; ++node)
    {
        int l = -1, r = hamiltonPath.size();
        while (l < r - 1)
        {
            int m = (l + r) >> 1;
            adjMatrix[hamiltonPath[m]][node] ? l = m : r = m;
        }
        hamiltonPath.insert(hamiltonPath.begin() + r, node);
    }

    std::deque<int> hamiltonCycle;

    int fisrtNode = hamiltonPath.front();
    hamiltonCycle.push_back(fisrtNode);
    hamiltonPath.pop_front();
    while (adjMatrix[hamiltonPath.front()][fisrtNode])
    {
        hamiltonCycle.push_back(hamiltonPath.front());
        hamiltonPath.pop_front();
    }

    while (!hamiltonPath.empty())
    {
        for (int j = 0; j < hamiltonPath.size(); ++j)
        {
            for (int i = hamiltonCycle.size() - 1; i >= 0; --i)
            {
                int nodeInCycleBeforePathIndex = i - 1 >= 0 ? i - 1 : hamiltonCycle.size() - 1;
                if (adjMatrix[hamiltonPath[j]][hamiltonCycle[i]] && adjMatrix[hamiltonCycle[nodeInCycleBeforePathIndex]][hamiltonPath[0]])
                {
                    hamiltonCycle.insert(hamiltonCycle.begin() + i, hamiltonPath.begin(), hamiltonPath.begin() + j + 1);
                    while (j >= 0)
                    {
                        hamiltonPath.pop_front();
                        j--;
                    }
                    break;
                }
            }
        }
    }

    for (std::deque<int>::iterator it = hamiltonCycle.begin(); it != hamiltonCycle.end(); ++it)
    {
        std::cout << *it + 1 << " ";
    }
}